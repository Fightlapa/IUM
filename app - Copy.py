from flask import Flask, jsonify
from flask_restful import Resource, Api, reqparse
from sqlalchemy import create_engine, Column, Integer, String, Float
from sqlalchemy.orm import scoped_session, sessionmaker
from sqlalchemy.ext.declarative import declarative_base
import pickle
import os


app = Flask(__name__)
engine = create_engine('sqlite:///IUM.db')
db_session = scoped_session(sessionmaker(autocommit=False,
                                         autoflush=False,
                                         bind=engine))
Base = declarative_base()
Base.query = db_session.query_property()
api = Api(app)

parser = reqparse.RequestParser()
parser.add_argument('manufacturer_name', type=str)
parser.add_argument('model_name', type=str)
parser.add_argument('price', type=float)
parser.add_argument('quantity', type=int)
parser.add_argument('id', type=int)

class Product(Base):
    __tablename__ = 'product'
    id = Column(Integer, primary_key=True)
    manufacturer_name = Column(String(50))
    model_name = Column(String(120), unique=True)
    quantity = Column(Integer)
    price = Column(Float)

    def __init__(self, manufacturer_name=None, model_name=None, price = None):
        self.manufacturer_name = manufacturer_name
        self.model_name = model_name
        self.price = price
        self.quantity = 0

    def __repr__(self):
        return '<Product %r>' % (self.manufacturer_name + ' ' + self.model_name)

def init_db():
    # import all modules here that might define models so that
    # they will be registered properly on the metadata.  Otherwise
    # you will have to import them first before calling init_db()
    import yourapplication.models
    Base.metadata.create_all(bind=engine)

def get_current_id():
    if len(db) == 0:
        return 0
    return db[-1]['id']

def get_database():
    if not os.path.isfile('database.pk'):
        return []
    with open('database.pk', 'rb') as db_file:
        return pickle.load(db_file)

def get_product(product_id):
    for product in db:
        if product['id'] == product_id:
            return product
    return None

def save_db():
    if os.path.isfile('database.pk'):
        os.remove('database.pk')
    with open('database.pk', 'wb') as db_file:
        pickle.dump(db, db_file)

init_db()

conn = db_connect.connect()
    
class Product(Resource):
    def get(self, product_id):
        # return get_product(product_id)
        return Product.query.filter(Product.id == product_id).first()

    def put(self, product_id):
        args = parser.parse_args()
        product = get_product(product_id)
        product['quantity'] = product['quantity'] + args['quantity']
        save_db()

    def delete(self, product_id):
        product = get_product(product_id)
        if product is not None:
            db.remove(product)
            save_db()

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
        db_session.add(Product(args['manufacturer_name'], args['model_name'], args['price']))
        db_session.commit()

class ProductList(Resource):
    def get(self):
        return Product.query.all()
        # return get_database()

api.add_resource(ProductList, '/packages/')
api.add_resource(Product, '/package/<int:product_id>')
api.add_resource(ProductCreate, '/package/')

if __name__ == '__main__':
    app.run(debug=True, port=5000, threaded=False, processes=1)