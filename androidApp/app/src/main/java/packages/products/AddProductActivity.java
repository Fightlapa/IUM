package packages.products;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static packages.products.BackEndRequestMaker.makeCall;

public class AddProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddProduct();
            }
        });
    }

    private void AddProduct() {
        final String manufacturer = ((EditText) findViewById(R.id.manufacturerInput)).getText().toString();
        final String model = ((EditText) findViewById(R.id.modelInput)).getText().toString();
        final String price = ((EditText) findViewById(R.id.priceInput)).getText().toString();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
        try {
            String jsonString = new JSONObject()
                    .put("manufacturer_name", manufacturer)
                    .put("model_name", model)
                    .put("price", price)
                    .toString();

            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product", "POST", jsonString);
            if (result.code == HttpURLConnection.HTTP_OK) {
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        }});
        thread.start();
    }
}
