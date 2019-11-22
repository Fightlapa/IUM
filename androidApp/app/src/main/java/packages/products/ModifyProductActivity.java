package packages.products;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static packages.products.GoogleSignIn.loggedUser;

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

        if (loggedUser == GoogleSignIn.LoggedUser.Manager)
        {
            findViewById(R.id.deleteProduct).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeleteProduct();
                }
            });
        }
        else
        {
            findViewById(R.id.deleteProduct).setEnabled(false);
        }


        Intent i = getIntent();
        product = (Product)i.getSerializableExtra("Product");

        ((EditText) findViewById(R.id.manufacturerInput)).setText(product.manufacturer);
        ((EditText) findViewById(R.id.modelInput)).setText(product.model);
        ((EditText) findViewById(R.id.priceInput)).setText(Double.toString(product.price));
        String quantityText = ((TextView) findViewById(R.id.quantityText)).getText().toString();
        ((TextView) findViewById(R.id.quantityText)).setText(quantityText + " " + product.quantity);
        ((TextView) findViewById(R.id.localID)).setText(Integer.toString(product.getId()));
        ((TextView) findViewById(R.id.serverID)).setText(Integer.toString(product.serverProductId));
    }

    private void ModifyProduct() {
        product.manufacturer = ((EditText) findViewById(R.id.manufacturerInput)).getText().toString();
        product.model = ((EditText) findViewById(R.id.modelInput)).getText().toString();
        product.price = Double.valueOf(((EditText) findViewById(R.id.priceInput)).getText().toString());

        ProductRepository.modifyNonQuantityData(product);

        finish();
    }

    private void AddQuantity() {
        product.quantity += Integer.valueOf(((EditText) findViewById(R.id.quantityChangeInput)).getText().toString());

        ProductRepository.modifyQuantity(product);

        finish();
    }

    private void DeleteProduct() {
        ProductRepository.delete(product);
        finish();
    }
}
