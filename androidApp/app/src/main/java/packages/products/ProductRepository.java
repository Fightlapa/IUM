package packages.products;

import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static packages.products.BackEndRequestMaker.makeCall;
import static packages.products.BackEndRequestMaker.saveCall;
import static packages.products.MainActivity.productDatabase;

public class ProductRepository {

    public static List<Product> getAll()
    {

        final ArrayList<Product> list = new ArrayList<Product>();
        if (BackEndRequestMaker.isOnline())
        {
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/products", "GET", null);
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
            Thread thread = new Thread(() -> {
                list.addAll((ArrayList)productDatabase.ProductDao().getAll());
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void insert(Product product)
    {
        try
        {
            JSONObject jsonString = new JSONObject()
                    .put("manufacturer_name", product.manufacturer)
                    .put("model_name", product.model)
                    .put("price", product.price);

            if (BackEndRequestMaker.isOnline())
            {
                BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product", "POST", jsonString);
            }
            else
            {
                Thread thread = new Thread(() -> {
                    long productId = productDatabase.ProductDao().insert(product);
                    saveCall("http://10.0.2.2:5000/product", "POST", jsonString, productId);
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void modify(Product product)
    {
        JSONObject jsonString = null;
        try {
            if (product.model != null)
            {
                jsonString = new JSONObject()
                        .put("manufacturer_name", product.manufacturer)
                        .put("model_name", product.model)
                        .put("price", product.price);
            }
            else
            {
                jsonString = new JSONObject()
                        .put("quantity", product.quantity);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (BackEndRequestMaker.isOnline()) {
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.uid, "PUT", jsonString);
        }
        else
        {
            JSONObject finalJsonString = jsonString;
            Thread thread = new Thread(() -> {
                productDatabase.ProductDao().update(product);
                saveCall("http://10.0.2.2:5000/product/", "PUT", finalJsonString, product.uid);
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(Product product)
    {
        if (BackEndRequestMaker.isOnline()) {
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.uid, "DELETE", null);
        }
        else
        {
            Thread thread = new Thread(() -> {
                productDatabase.ProductDao().delete(product);
                saveCall("http://10.0.2.2:5000/product/", "DELETE", null, product.uid);
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
