from flask import Flask, jsonify, session, redirect, request
from flask_restful import Resource, Api, reqparse
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow
from requests_oauthlib import OAuth2Session
from passlib.hash import bcrypt
from random import SystemRandom
from jose import jwt
import requests
import os

# Init app
app = Flask(__name__)
url = 'localhost'

random = SystemRandom()
keys = requests.get('https://www.googleapis.com/oauth2/v1/certs').json()

CONFIG = {
    'client_id': "1043665606163-fip7gfqqj0v0iqbopt8tpcrc0888ftcs.apps.googleusercontent.com",
    'client_secret': "XwhgSmIgvEeurq0BKtc1rDqp",
    'auth_url': "https://accounts.google.com/o/oauth2/auth",
    'token_url': "https://accounts.google.com/o/oauth2/token",
    'scope': [],
    'redirect_url': f"{url}/products"
}

OIDC_CONFIG = {
    'jwt_pubkeys': keys,
    'scope': ['openid'],
    'expected_issuer': 'localhost',
    'algorithm': 'RS512'
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

def check_access_token(session):
    if 'access_token' not in session:
        return redirect('/login')

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
    username = db.Column(db.String, unique=True)
    password_hash = db.Column(db.String)

    def __init__(self, username, password_hash):
        self.username = username
        self.password_hash = password_hash

class ProductSchema(ma.Schema):
    class Meta:
        fields = ('id', 'manufacturer_name', 'model_name', 'price', 'quantity')

# Init schema
product_schema = ProductSchema()
products_schema = ProductSchema(many=True)

db.init_app(app)

db.create_all()

api = Api(app)

def get_product(product_id):
    return Product.query.get(product_id)

class UserCreate(Resource):
    def post(self):
        args = user_parser.parse_args()
        un = args['username']
        password = args['password']
        pwd_hash = bcrypt.generate_password_hash(password)
        user = User(un, pwd_hash)
        db.session.add(user)
        db.session.commit()

class UserLoginEndpoint(Resource):
    def post(self):
        args = user_parser.parse_args()
        un = args['username']
        user = User.query.filter_by(username=un).one()
        password = args['password']
        if bcrypt.verify(password, user.password_hash):
            session['authed_user'] = user.username
            return redirect('/products')
        else:
            return {"Login error"}

class GoogleLoginEndpoint(Resource):
    def post(self):
        provider = OAuth2Session(
            client_id=CONFIG['client_id'],
            scope=CONFIG['scope'],
            redirect_uri=CONFIG['redirect_url'])
        nonce = str(random.randint(0, 1e10))
        url, state = provider.authorization_url(CONFIG['auth_url'],
                                                nonce=nonce)
        session['oauth2_state'] = state
        session['nonce'] = nonce
        return redirect(url)

# CALLBACK
    def get(self):
        provider = OAuth2Session(
            client_id=CONFIG['client_id'],
            scope=CONFIG['scope'],
            redirect_uri=CONFIG['redirect_url'])
        token_response = provider.fetch_token(
            token_url=CONFIG['token_url'],
            client_secret=CONFIG['client_secret'],
            authorization_response=request.url
        )
        session['access_token'] = token_response['access_token']
        session['access_token_expires'] = token_response['expires_at']
        id_token = token_response['id_token']
        claims = jwt.decode(id_token,
                            key=CONFIG['jwt_pubkeys'],
                            issuer=CONFIG['issuer'],
                            audience=CONFIG['client_id'],
                            algorithms=CONFIG['algorithm'],
                            access_token=response['access_token'])

        assert session['nonce'] == claims['nonce']
        session['user_id'] = claims['sub']
        session['user_email'] = claims['email']
        session['user_name'] = claims['name']

        api_url = f"{url}/packages"
        transfer = provider.get(api_url)
        return redirect('/packages')

class ProductEndpoint(Resource):
    def get(self, product_id):
        check_access_token(session)
        return product_schema.jsonify(get_product(product_id))

    def put(self, product_id):
        check_access_token(session)
        args = product_parser.parse_args()
        product = get_product(product_id)
        product.quantity += args['quantity']
        db.session.commit()

    def delete(self, product_id):
        check_access_token(session)
        if 'authed_user' not in session:
            return redirect('/products')
        product = get_product(product_id)
        db.session.delete(product)
        db.session.commit()


class ProductCreate(Resource):
    def post(self):
        check_access_token(session)
        args = product_parser.parse_args()
        product = Product(args['manufacturer_name'], args['model_name'], args['price'])
        db.session.add(product)
        db.session.commit()

class ProductList(Resource):
    def get(self):
        return check_access_token(session)
        return jsonify(products_schema.dump(Product.query.all()))

api.add_resource(ProductList, '/products/')
api.add_resource(ProductEndpoint, '/product/<int:product_id>')
api.add_resource(ProductCreate, '/product/')
api.add_resource(UserLoginEndpoint, '/login')
api.add_resource(GoogleLoginEndpoint, '/logingoogle')
api.add_resource(UserCreate, '/create')

if __name__ == '__main__':
    app.run(debug=True, port=5000, threaded=False, processes=1)