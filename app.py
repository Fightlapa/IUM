from datetime import timedelta
from flask import Flask, jsonify, session, redirect, request
from flask_restful import Resource, Api, reqparse
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from sqlalchemy.dialects.postgresql import UUID
from flask_marshmallow import Marshmallow
import hashlib
import binascii
from urllib.parse import parse_qs
import urllib.parse as urlparse
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
migrate = Migrate(app, db)

# with app.test_request_context():

product_parser = reqparse.RequestParser()
product_parser.add_argument('manufacturer_name', type=str)
product_parser.add_argument('model_name', type=str)
product_parser.add_argument('price', type=float)
product_parser.add_argument('quantity', type=int)
product_parser.add_argument('id', type=int)
product_parser.add_argument('width', type=int)
product_parser.add_argument('height', type=int)
product_parser.add_argument('guid', type=str)

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
    width = db.Column(db.Integer, default=0)
    height = db.Column(db.Integer, default=0)

class Request(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    uuid = db.Column(db.String(36), unique=True, nullable=False)

    def __init__(self, uuid):
        self.uuid = uuid

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String, unique=True)
    role = db.Column(db.String)

    def __init__(self, email, username=None, password_hash=None):
        self.email = email
        self.role = "Maintainer"

class ProductSchema(ma.Schema):
    class Meta:
        fields = ('id', 'manufacturer_name', 'model_name', 'price', 'quantity', 'width', 'height')

class RequestSchema(ma.Schema):
    class Meta:
        fields = ('id', 'uuid')

# Init schema
product_schema = ProductSchema()
products_schema = ProductSchema(many=True)

request_schema = RequestSchema()
requests_schema = RequestSchema(many=True)

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

class GoogleLoginEndpoint(Resource):
    def post(self):
        try:
            result = requests.get(f"https://oauth2.googleapis.com/tokeninfo?id_token={request.json['token']}").json()

            if result['iss'] not in ['accounts.google.com', 'https://accounts.google.com']:
                raise ValueError('Wrong issuer.')

            user_email = result['email']

            session.permanent = True
            #user_email = "googrle@gmail.com"
            session['authed_user'] = user_email
            if user_email == "googrle@gmail.com":
                return "Manager"
            # return user.role
            return "Manager"
        except ValueError:
            # Invalid token
            pass


class ProductEndpoint(Resource):

    def get(self, product_id):
        if not is_logged(session):
            return return_unauthorized()
        product = get_product(product_id)
        if product:
            return product_schema.jsonify(product)

    def put(self, product_id):
        if not is_logged(session):
            return return_unauthorized()
        args = product_parser.parse_args()

        database_guid = Request.query.filter_by(uuid=args['guid']).scalar()
        if database_guid is not None:
            return

        product = get_product(product_id)
        if product:
            try:
                if args['quantity'] is not None:
                    product.quantity += args['quantity']
                elif args['model_name'] is not None:
                    product.model_name = args['model_name']
                    product.manufacturer_name = args['manufacturer_name']
                    product.price = args['price']
                    if args['height'] is not None:
                        product.height = args['height']
                    if args['width'] is not None:
                        product.width = args['width']
                if args['guid'] is not None:
                    request = Request(args['guid'])
                    db.session.add(request)
                db.session.commit()
            except:
                db.session.rollback()

    def delete(self, product_id):
        if not is_logged(session):
            return return_unauthorized()
        args = product_parser.parse_args()
        database_guid = Product.query.filter_by(uuid=args['guid']).scalar()
        if database_guid is not None:
            return
        if 'authed_user' not in session:
            return redirect('/products')
        product = get_product(product_id)
        if product:
            db.session.delete(product)
            request = Request(args['guid'])
            db.session.add(request)
            db.session.commit()


class ProductCreate(Resource):
    def post(self):
        if not is_logged(session):
            return return_unauthorized()
        args = product_parser.parse_args()
        if args['guid'] is not None:
            database_guid = Product.query.filter_by(uuid=args['guid']).scalar()
            if database_guid is not None:
                return
        product = Product()
        if args['manufacturer_name'] is not None:
            product.manufacturer_name = args['manufacturer_name']
        if args['model_name'] is not None:
            product.model_name = args['model_name']
        if args['price'] is not None:
            product.price = args['price']
        if args['width'] is not None:
            product.width = args['width']
        if args['height'] is not None:
            product.height = args['height']
        product.quantity = 0
        db.session.add(product)
        if args['guid'] != None:
            request = Request(args['guid'])
            db.session.add(request)
        db.session.commit()
        db.session.refresh(product)
        return product.id

class ProductList(Resource):
    def get(self):
        if not is_logged(session):
            return return_unauthorized()
        if request.args.__len__() == 0:
            expand = ['id', 'manufacturer_name', 'model_name', 'quantity', 'price']
        else:
            expand = request.args['expand'].split(',')
        all_produts = Product.query.all()
        #filtered_products_schema = ProductSchema(many=True, only=expand).dump(Product.query.all())
        #dumped_products = filtered_products_schema.dump(all_produts)
        dumped_products = products_schema.dump(all_produts)
        if len(request.args) is 0:
            for product in dumped_products:
                product['model_name'] = product['model_name'] + f"({product['height']}x{product['width']})"
                keys_to_delete = []
                for key, value in product.items():
                    if key not in expand:
                        keys_to_delete.append(key)
                for key in keys_to_delete:
                    del product[key]
        return jsonify(dumped_products)

api.add_resource(ProductList, '/products')
api.add_resource(ProductEndpoint, '/product/<int:product_id>')
api.add_resource(ProductCreate, '/product')
api.add_resource(GoogleLoginEndpoint, '/logingoogle')

# --cert=certificate.crt --key=privateKey.key
if __name__ == '__main__':
    app.run(debug=True, port=5000)