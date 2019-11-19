package packages.products;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    public static ProductDatabase productDatabase;

    private List<Product> ProductList = new ArrayList<Product>();

    ArrayAdapter<Product> arrayAdapter;

    public ArrayAdapter<Product> getArrayAdapter() {
        return arrayAdapter;
    }

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productDatabase = Room.databaseBuilder(getApplicationContext(), ProductDatabase.class, "products-db").build();

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

        findViewById(R.id.changeOnlineStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackEndRequestMaker.changeOnlineStatus();
                ((Button)findViewById(R.id.changeOnlineStatus)).setText(BackEndRequestMaker.getOnlineStatusString());
            }
        });

        //InitializeLocalDatabase();

    }

    public ProductDatabase getDatabase() {
        return productDatabase;
    }

    private void StartAddProductActivity() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getProducts();
    }

    private void getProducts()
    {
        new ProductAsyncTask(this, BackEndRequestMaker.isOnline()).execute();
    }

    private static class ProductAsyncTask extends AsyncTask<Void, Void, List<Product>> {

        //Prevent leak
        private WeakReference<MainActivity> weakActivity;
        private boolean isOnline;

        public ProductAsyncTask(MainActivity activity, boolean isOnline) {
            weakActivity = new WeakReference<>(activity);
            this.isOnline = isOnline;
        }

        @Override
        protected List<Product> doInBackground(Void... params) {
            return ProductRepository.getAll();
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

}
