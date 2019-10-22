package packages.products;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static packages.products.BackEndRequestMaker.makeCall;


public class MainActivity extends AppCompatActivity {

    public static ProductDatabase ProductDatabase;



    private List<Product> ProductList = new ArrayList<Product>();

    ArrayAdapter<Product> arrayAdapter;

    public ArrayAdapter<Product> getArrayAdapter() {
        return arrayAdapter;
    }

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView = findViewById(R.id.Productlist);
        arrayAdapter = new ArrayAdapter<Product>
                (this, android.R.layout.simple_list_item_1, ProductList);
        listView.setAdapter(arrayAdapter);

        final MainActivity thisActivity = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Product item = (Product)adapterView.getItemAtPosition(position);

                Intent intent = new Intent(thisActivity, ModifyProductActivity.class);
                //based on item add info to intent
                intent.putExtra("Product", item);
                startActivity(intent);
            }

        });

        findViewById(R.id.addProductButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartAddProductActivity();
            }
        });

        //InitializeLocalDatabase();

    }

    private void StartAddProductActivity() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        new ProductAsyncTask(this).execute();
    }

    private static class ProductAsyncTask extends AsyncTask<Void, Void, List<Product>> {

        //Prevent leak
        private WeakReference<MainActivity> weakActivity;

        public ProductAsyncTask(MainActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected List<Product> doInBackground(Void... params) {
            // WHEN LOCAL DB WILL BE ENABLED
//            ProductDao ProductDao = MainActivity.ProductDatabase.ProductDao();
//            return ProductDao.getAll();

            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/products", "GET", "");
            ArrayList<Product> list = new ArrayList<Product>();
            if (result.code == 200) {
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
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Product> list) {
            Activity activity = weakActivity.get();
            if (activity == null) {
                return;
            }
            ((MainActivity) activity).getArrayAdapter().clear();
            ((MainActivity) activity).getArrayAdapter().addAll(list);
        }
    }

//    private void InitializeLocalDatabase() {
//        getApplicationContext().deleteDatabase("Product-database");
//
//        ProductDatabase = Room.databaseBuilder(getApplicationContext(),
//                ProductDatabase.class, "Product-database").build();
//    }

}
