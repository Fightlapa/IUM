package packages.products;

import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static packages.products.BackEndRequestMaker.makeCall;
import static packages.products.MainActivity.productDatabase;

public class ProductRepository {

    public static List<Product> getAll()
    {
        BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/products", "GET", "");
        ArrayList<Product> list = new ArrayList<Product>();
        if (result.code == 200) {
            productDatabase.clearAllTables();
            try {
                JSONArray jsonArray = new JSONArray(result.body.trim());
                for (int i = 0; i < jsonArray.length(); i++) {
                    Product product = new Product();
                    JSONObject obj = jsonArray.getJSONObject(i);
                    product.manufacturer = obj.getString("manufacturer_name");
                    product.model = obj.getString("model_name");
                    product.price = obj.getDouble("price");
                    product.quantity = obj.getInt("quantity");
                    product.uid = obj.getInt("id");
                    list.add(product);
                    productDatabase.ProductDao().insert(product);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            list = (ArrayList)productDatabase.ProductDao().getAll();
        }
        return list;
    }

    public static void insert(Product product)
    {
        try
        {
            String jsonString = new JSONObject()
                    .put("manufacturer_name", product.manufacturer)
                    .put("model_name", product.model)
                    .put("price", product.price)
                    .toString();

            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product", "POST", jsonString);
            if (result.code != HttpURLConnection.HTTP_OK) {
                productDatabase.ProductDao().insert(product);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void modify(Product product)
    {
        String jsonString = null;
        if (product.model != null)
        {
            try {
                jsonString = new JSONObject()
                        .put("manufacturer_name", product.manufacturer)
                        .put("model_name", product.model)
                        .put("price", product.price)
                        .toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        else
        {
            try {
                jsonString = new JSONObject()
                        .put("quantity", product.quantity)
                        .toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.uid, "PUT", jsonString);
        if (result.code != HttpURLConnection.HTTP_OK) {
            // TODO
        }
    }

    public static void delete(Product product)
    {
        BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.uid, "DELETE", "");
        if (result.code == HttpURLConnection.HTTP_OK) {
            //TODO
        }
    }

}
