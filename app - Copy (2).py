from flask import Flask, jsonify
from flask_restful import Resource, Api, reqparse
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow
from sqlalchemy import create_engine, Column, Integer, String, Float
from sqlalchemy.orm import scoped_session, sessionmaker
from sqlalchemy.ext.declarative import declarative_base


# Init app
app = Flask(__name__)
basedir = os.path.abspath(os.path.dirname(__file__))
# Database
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + os.path.join(basedir, 'db.sqlite')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
# Init db
db = SQLAlchemy(app)
# Init ma
ma = Marshmallow(app)

api = Api(app)

parser = reqparse.RequestParser()
parser.add_argument('manufacturer_name', type=str)
parser.add_argument('model_name', type=str)
parser.add_argument('price', type=float)
parser.add_argument('quantity', type=int)
parser.add_argument('id', type=int)

# Product Class/Model
class Product(db.Model):
  id = db.Column(db.Integer, primary_key=True)
  manufacturer_name = db.Column(db.String(100), unique=True)
  model_name = db.Column(db.String(200))
  price = db.Column(db.Float)
  quantity = db.Column(db.Integer)

  def __init__(self, manufacturer_name, model_name, price):
    self.manufacturer_name = manufacturer_name
    self.model_name = model_name
    self.price = price
    self.quantity = 0

class ProductSchema(ma.Schema):
  class Meta:
    fields = ('id', 'manufacturer_name', 'model_name', 'price', 'quantity')

# Init schema
product_schema = ProductSchema(strict=True)
products_schema = ProductSchema(many=True, strict=True)

def init_db():
    # import all modules here that might define models so that
    # they will be registered properly on the metadata.  Otherwise
    # you will have to import them first before calling init_db()
    Base.metadata.create_all(bind=engine)


def get_product(product_id):
    return jsonify(Product.query.filter(Product.id == product_id).first())

init_db()

class ProductEndpoint(Resource):
    def get(self, product_id):
        return get_product(product_id)

    def put(self, product_id):
        args = parser.parse_args()
        product = get_product(product_id)
        product.quantity += args['quantity']
        db_session.commit()

    def delete(self, product_id):
        product = get_product(product_id)
        if product is not None:
            db_session.delete(product)

class ProductCreate(Resource):
    def post(self):
        args = parser.parse_args()
        # product = {'id': get_current_id() + 1,
        #            'manufacturer_name': args['manufacturer_name'],
        #            'model_name': args['model_name'],
        #            'price': args['price'],
        #            'quantity': 0}
        # db.append(product)
        # save_db()
        product = Product(args['manufacturer_name'], args['model_name'], args['price'])
        db_session.add(product)
        db_session.commit()

class ProductList(Resource):
    def get(self):
        return jsonify(json_list = Product.query.all())
        # return get_database()

api.add_resource(ProductList, '/packages/')
api.add_resource(ProductEndpoint, '/package/<int:product_id>')
api.add_resource(ProductCreate, '/package/')

if __name__ == '__main__':
    app.run(debug=True, port=5000, threaded=False, processes=1)