python -m pip install flask flask-jsonpify flask-sqlalchemy flask-restful
python -m pip freeze
set FLASK_ENV=development
set FLASK_APP=app.py
py -m flask run