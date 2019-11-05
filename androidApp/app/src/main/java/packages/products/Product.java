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
    public String manufacturer;

    @ColumnInfo(name = "Model")
    public String model;

    @ColumnInfo(name = "Quantity")
    public int quantity;

    @ColumnInfo(name = "Price")
    public double price;

    @Override
    public String toString()
    {
        return manufacturer + ", " + model + ", " + quantity + ", " + price + "$";
    }
}

