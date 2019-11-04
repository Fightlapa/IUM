package packages.products;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static packages.products.BackEndRequestMaker.makeCall;

public class ModifyProductActivity extends AppCompatActivity {

    private Product product;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_product);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyProduct();
            }
        });

        findViewById(R.id.submitQuantityChange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddQuantity();
            }
        });

        findViewById(R.id.deleteProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteProduct();
            }
        });

        Intent i = getIntent();
        product = (Product)i.getSerializableExtra("Product");

        ((EditText) findViewById(R.id.manufacturerInput)).setText(product.manufacturer);
        ((EditText) findViewById(R.id.modelInput)).setText(product.model);
        ((EditText) findViewById(R.id.priceInput)).setText(Double.toString(product.price));
        String quantityText = ((TextView) findViewById(R.id.quantityText)).getText().toString();
        ((TextView) findViewById(R.id.quantityText)).setText(quantityText + " " + product.quantity);
    }

    private void ModifyProduct() {
        final String manufacturer = ((EditText) findViewById(R.id.manufacturerInput)).getText().toString();
        final String model = ((EditText) findViewById(R.id.modelInput)).getText().toString();
        final String price = ((EditText) findViewById(R.id.priceInput)).getText().toString();

        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put("manufacturer_name", manufacturer)
                    .put("model_name", model)
                    .put("price", price)
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.uid, "PUT", jsonString);
        if (result.code == HttpURLConnection.HTTP_OK) {
            finish();
        }
    }

    private void AddQuantity() {
        final String quantity = ((EditText) findViewById(R.id.quantityChangeInput)).getText().toString();
        final String currentQuantity = ((EditText) findViewById(R.id.quantityText)).getText().toString();

        if (Integer.parseInt(currentQuantity) + Integer.parseInt(quantity) < 0)
        {
            Toast.makeText(this, "Quantity will be lower than 0!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String jsonString = null;
            try {
                jsonString = new JSONObject()
                        .put("quantity", quantity)
                        .toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.uid, "PUT", jsonString);
            if (result.code == HttpURLConnection.HTTP_OK) {
                finish();
            }
        }
    }

    private void DeleteProduct() {
        BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/product/" + product.uid, "DELETE", "");
        if (result.code == HttpURLConnection.HTTP_OK) {
            finish();
        }
    }
}
