package packages.products;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;


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
        Product product = new Product();
        product.manufacturer = ((EditText) findViewById(R.id.manufacturerInput)).getText().toString();
        product.model = ((EditText) findViewById(R.id.modelInput)).getText().toString();
        product.price = Double.valueOf(((EditText) findViewById(R.id.priceInput)).getText().toString());
        product.width = Integer.valueOf(((EditText) findViewById(R.id.widthInput)).getText().toString());
        product.height = Integer.valueOf(((EditText) findViewById(R.id.heightInput)).getText().toString());

        ProductRepository.insert(product);

        finish();

    }
}
