package packages.products;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import static packages.products.BackEndRequestMaker.makeCall;
import static packages.products.BackEndRequestMaker.saveCall;
import static packages.products.MainActivity.productDatabase;

public class ProductRepository {

    public static List<Product> getAll()
    {
        Thread thread;
        final ArrayList<Product> list = new ArrayList<Product>();
        if (BackEndRequestMaker.isOnline())
        {
            BackEndRequestMaker.sendPendingRequests();
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/products", "GET", null);

            try {
                JSONArray jsonArray = new JSONArray(result.body.trim());
                thread = new Thread(() -> {
                    productDatabase.ProductDao().deleteAll();
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    Product product = new Product();
                    JSONObject obj = jsonArray.getJSONObject(i);
                    product.manufacturer = obj.getString("manufacturer_name");
                    product.model = obj.getString("model_name");
                    product.price = obj.getDouble("price");
                    product.quantity = obj.getInt("quantity");
                    product.serverProductId = obj.getInt("id");
                    final long[] localId = new long[1];
                    thread = new Thread(() -> {
                        localId[0] = productDatabase.ProductDao().insert(product);
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    product.setId((int)localId[0]);
                    list.add(product);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            thread = new Thread(() -> {
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
            JSONObject jsonObject = new JSONObject()
                    .put("manufacturer_name", product.manufacturer)
                    .put("model_name", product.model)
                    .put("price", product.price);

            if (BackEndRequestMaker.isOnline())
            {
                BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product", "POST", jsonObject.toString());
            }
            else
            {
                Thread thread = new Thread(() -> {
                    long productId = productDatabase.ProductDao().insert(product);
                    saveCall("http://10.0.2.2:5000/product", "POST", jsonObject, productId, -1);
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

    public static void modifyQuantity(Product product)
    {
        JSONObject jsonObject = null;
        try {
                jsonObject = new JSONObject()
                        .put("quantity", product.quantity);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (BackEndRequestMaker.isOnline()) {
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.serverProductId, "PUT", jsonObject.toString());
        }
        else
        {
            JSONObject finalJsonString = jsonObject;
            Thread thread = new Thread(() -> {
                productDatabase.ProductDao().update(product);
                saveCall("http://10.0.2.2:5000/product/", "PUT", finalJsonString, product.getId(), -1);
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void modifyNonQuantityData(Product product)
    {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject()
                    .put("manufacturer_name", product.manufacturer)
                    .put("model_name", product.model)
                    .put("price", product.price);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (BackEndRequestMaker.isOnline()) {
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.serverProductId, "PUT", jsonObject.toString());
        }
        else
        {
            JSONObject finalJsonString = jsonObject;
            Thread thread = new Thread(() -> {
                productDatabase.ProductDao().update(product);
                saveCall("http://10.0.2.2:5000/product/", "PUT", finalJsonString, product.getId(), -1);
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
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.serverProductId, "DELETE", "");
        }
        else
        {
            Thread thread = new Thread(() -> {
                saveCall("http://10.0.2.2:5000/product/", "DELETE", null, product.getId(), product.serverProductId);
                productDatabase.ProductDao().delete(product);
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static Product getByLocalID(int localId)
    {
        final Product[] product = new Product[1];
        Thread thread = new Thread(() -> {
            product[0] = productDatabase.ProductDao().getByLocalId(localId);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return product[0];
    }

    public static void updateLocalProduct(Product product)
    {
        Thread thread = new Thread(() -> {
            productDatabase.ProductDao().update(product);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Queue<Request> getAllRequests()
    {
        Queue<Request> queue = new LinkedList<>();
        Thread thread = new Thread(() -> {
            queue.addAll((ArrayList)productDatabase.RequestDao().getAll());
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return queue;
    }

    public static void deleteRequest(Request request)
    {
        Thread thread = new Thread(() -> {
            productDatabase.RequestDao().delete(request);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
