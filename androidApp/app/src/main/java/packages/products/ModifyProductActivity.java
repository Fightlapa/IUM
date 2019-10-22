package packages.products;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ModifyProductActivity extends AppCompatActivity {

    private Product product;
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

        Intent i = getIntent();
        product = (Product)i.getSerializableExtra("Product");
    }

    private void AddProduct() {
        final String manufacturer = ((EditText) findViewById(R.id.manufacturerInput)).getText().toString();
        final String model = ((EditText) findViewById(R.id.modelInput)).getText().toString();
        final String price = ((EditText) findViewById(R.id.priceInput)).getText().toString();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
        try {
            URL url = new URL("http://10.0.2.2:5000/product");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            //con.setRequestProperty("Cookie", "name=value");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();

            String jsonString = new JSONObject()
                    .put("manufacturer_name", manufacturer)
                    .put("model_name", model)
                    .put("price", price)
                    .toString();


            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.write(jsonString.getBytes());
            out.flush();
            out.close();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                finish();
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        }});
        thread.start();
    }
}
