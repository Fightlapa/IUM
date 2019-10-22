//package packages.products;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.app.Activity;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//
//
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.lang.ref.WeakReference;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.ArrayList;
//import java.util.List;
//
//public class Products extends AppCompatActivity {
//
//    private List<Product> ProductList = new ArrayList<Product>();
//
//    ArrayAdapter<Product> arrayAdapter;
//
//    public ArrayAdapter<Product> getArrayAdapter() {
//        return arrayAdapter;
//    }
//
//    ListView listView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_packages);
//        listView = findViewById(R.id.Productlist);
//        arrayAdapter = new ArrayAdapter<Product>
//                (this, android.R.layout.simple_list_item_1, ProductList);
//        listView.setAdapter(arrayAdapter);
//    }
//
//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        new ProductAsyncTask(this).execute();
//    }
//
//
//
//    private static class ProductAsyncTask extends AsyncTask<Void, Void, List<Product>> {
//
//        //Prevent leak
//        private WeakReference<Products> weakActivity;
//
//        public ProductAsyncTask(Products activity) {
//            weakActivity = new WeakReference<>(activity);
//        }
//
//        @Override
//        protected List<Product> doInBackground(Void... params) {
//            // WHEN LOCAL DB WILL BE ENABLED
////            ProductDao ProductDao = MainActivity.ProductDatabase.ProductDao();
////            return ProductDao.getAll();
//            URL url = null;
//            try {
//                url = new URL("https://127.0.0.1/products/");
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//            String postData = "foo bar baz";
//
//            URLConnection con = null;
//            try {
//                con = url.openConnection();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            con.setDoOutput(true);
//            con.setRequestProperty("Cookie", "name=value");
//            con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
//            try {
//                con.connect();
//                OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
//                out.write(postData);
//                out.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return new ArrayList<Product>();
//        }
//
//        @Override
//        protected void onPostExecute(List<Product> list) {
//            Activity activity = weakActivity.get();
//            if (activity == null) {
//                return;
//            }
//            ((Products) activity).getArrayAdapter().addAll(list);
//        }
//    }
//}
