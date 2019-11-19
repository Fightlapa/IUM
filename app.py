from datetime import timedelta
from flask import Flask, jsonify, session, redirect, request
from flask_restful import Resource, Api, reqparse
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow
import hashlib
import binascii
from random import SystemRandom
import requests
import os

# Init app
app = Flask(__name__)
os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'
flask_url = 'https://127.0.0.1:5000'

random = SystemRandom()
keys = requests.get('https://www.googleapis.com/oauth2/v1/certs').json()

CONFIG = {
    'client_id': "1043665606163-fip7gfqqj0v0iqbopt8tpcrc0888ftcs.apps.googleusercontent.com",
    'client_secret': "XwhgSmIgvEeurq0BKtc1rDqp",
    'auth_url': "https://accounts.google.com/o/oauth2/auth",
    'token_url': "https://accounts.google.com/o/oauth2/token",
    'scope': [],
    'redirect_url': f"{flask_url}/logingoogle"
}

OIDC_CONFIG = {
    'jwt_pubkeys': keys,
    'scope': ['openid', 'https://www.googleapis.com/auth/userinfo.email'],
    'expected_issuer': "accounts.google.com",
    'algorithm': 'RS256'
}

CONFIG.update(OIDC_CONFIG)

app.secret_key = 'so_secret'
basedir = os.path.abspath(os.path.dirname(__file__))
# Database
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + os.path.join(basedir, 'db.sqlite')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
# Init db
db = SQLAlchemy(app)
# Init ma
ma = Marshmallow(app)

# with app.test_request_context():

product_parser = reqparse.RequestParser()
product_parser.add_argument('manufacturer_name', type=str)
product_parser.add_argument('model_name', type=str)
product_parser.add_argument('price', type=float)
product_parser.add_argument('quantity', type=int)
product_parser.add_argument('id', type=int)

user_parser = reqparse.RequestParser()
user_parser.add_argument('username', type=str)
user_parser.add_argument('password', type=str)
user_parser.add_argument('email', type=str)

google_login_parser = reqparse.RequestParser()
user_parser.add_argument('token', type=str)
user_parser.add_argument('email', type=str)

@app.before_request
def make_session_permanent():
    session.permanent = True
    app.permanent_session_lifetime = timedelta(minutes=5)

def is_logged(session):
    if 'authed_user' not in session:
        return False
    return True

def return_unauthorized():
    response = jsonify({'message': 'Unauthorized'})
    response.status_code = 401
    return response

def hash_password(password):
    """Hash a password for storing."""
    salt = hashlib.sha256(os.urandom(60)).hexdigest().encode('ascii')
    pwdhash = hashlib.pbkdf2_hmac('sha512', password.encode('utf-8'),
                                salt, 100000)
    pwdhash = binascii.hexlify(pwdhash)
    return (salt + pwdhash).decode('ascii')

def verify_password(stored_password, provided_password):
    """Verify a stored password against one provided by user"""
    salt = stored_password[:64]
    stored_password = stored_password[64:]
    pwdhash = hashlib.pbkdf2_hmac('sha512',
                                  provided_password.encode('utf-8'),
                                  salt.encode('ascii'),
                                  100000)
    pwdhash = binascii.hexlify(pwdhash).decode('ascii')
    return pwdhash == stored_password

# Product Class/Model
class Product(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    manufacturer_name = db.Column(db.String(100))
    model_name = db.Column(db.String(200))
    price = db.Column(db.Float)
    quantity = db.Column(db.Integer)

    def __init__(self, manufacturer_name, model_name, price):
        self.manufacturer_name = manufacturer_name
        self.model_name = model_name
        self.price = price
        self.quantity = 0

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String, unique=True, nullable=True)
    password_hash = db.Column(db.String, nullable=True)
    email = db.Column(db.String, unique=True)
    role = db.Column(db.String)

    def __init__(self, email, username=None, password_hash=None):
        self.username = username
        self.password_hash = password_hash
        self.email = email
        self.role = "Maintainer"

class ProductSchema(ma.Schema):
    class Meta:
        fields = ('id', 'manufacturer_name', 'model_name', 'price', 'quantity')

# Init schema
product_schema = ProductSchema()
products_schema = ProductSchema(many=True)

db.init_app(app)

db.create_all()

api = Api(app)

user = User.query.filter_by(email="googrle@gmail.com").scalar()

if user is None:
    user = User("googrle@gmail.com")
    user.role = "Manager"
    db.session.add(user)
    db.session.commit()

def get_product(product_id):
    return Product.query.get(product_id)

class UserCreate(Resource):
    def post(self):
        args = user_parser.parse_args()
        un = args['username']
        password = args['password']
        pwd_hash = hash_password(password)
        user = User(un, pwd_hash, args['email'])
        db.session.add(user)
        db.session.commit()

class UserLoginEndpoint(Resource):
    def post(self):
        args = user_parser.parse_args()
        un = args['username']
        user = User.query.filter_by(username=un).one()
        password = args['password']
        if verify_password(user.password_hash, password):
            session['authed_user'] = user.email
            return redirect('/products')
        else:
            return {"Login error"}

class GoogleLoginEndpoint(Resource):
    def post(self):
        try:
            # Specify the CLIENT_ID of the app that accesses the backend:
            result = requests.get(f"https://oauth2.googleapis.com/tokeninfo?id_token={request.json['token']}").json()

            if result['iss'] not in ['accounts.google.com', 'https://accounts.google.com']:
                raise ValueError('Wrong issuer.')

            # ID token is valid. Get the user's Google Account ID from the decoded token.
            user_email = result['email']
            user = User.query.filter_by(email=user_email).scalar()

            if user is None:
                user = User(user_email)
                db.session.add(user)
                db.session.commit()
            session.permanent = True
            session['authed_user'] = user_email
            if user_email == "googrle@gmail.com":
                return "Manager"
            return user.role
        except ValueError:
            # Invalid token
            pass


class ProductEndpoint(Resource):
    def get(self, product_id):
        if not is_logged(session):
            return return_unauthorized()
        return product_schema.jsonify(get_product(product_id))

    def put(self, product_id):
        if not is_logged(session):
            return return_unauthorized()
        args = product_parser.parse_args()
        product = get_product(product_id)
        if args['quantity'] is None:
            product.model_name = args['model_name']
            product.manufacturer_name = args['manufacturer_name']
            product.price = args['price']
        else:
            product.quantity += args['quantity']
        db.session.commit()

    def delete(self, product_id):
        if not is_logged(session):
            return return_unauthorized()
        if 'authed_user' not in session:
            return redirect('/products')
        product = get_product(product_id)
        db.session.delete(product)
        db.session.commit()


class ProductCreate(Resource):
    def post(self):
        if not is_logged(session):
            return return_unauthorized()
        args = product_parser.parse_args()
        product = Product(args['manufacturer_name'], args['model_name'], args['price'])
        db.session.add(product)
        db.session.commit()

class ProductList(Resource):
    def get(self):
        if not is_logged(session):
            return return_unauthorized()
        return jsonify(products_schema.dump(Product.query.all()))

api.add_resource(ProductList, '/products')
api.add_resource(ProductEndpoint, '/product/<int:product_id>')
api.add_resource(ProductCreate, '/product')
api.add_resource(UserLoginEndpoint, '/login')
api.add_resource(GoogleLoginEndpoint, '/logingoogle')
api.add_resource(UserCreate, '/create')

# --cert=certificate.crt --key=privateKey.key
if __name__ == '__main__':
    app.run(debug=True, port=5000)