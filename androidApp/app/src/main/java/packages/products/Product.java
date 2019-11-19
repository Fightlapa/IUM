package packages.products;
import android.graphics.ColorSpace;

import androidx.room.Entity;

import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.io.Serializable;

@Entity(tableName = "products")
public class Product implements Serializable {
    @PrimaryKey()
    public int uid;

    @ColumnInfo(name = "Manufacturer")
    public String manufacturer = null;

    @ColumnInfo(name = "Model")
    public String model = null;

    @ColumnInfo(name = "Quantity")
    public int quantity = 0;

    @ColumnInfo(name = "Price")
    public double price = 0;

    @Override
    public String toString()
    {
        return manufacturer + ", " + model + ", " + quantity + ", " + price + "$";
    }
}

