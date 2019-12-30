package packages.products;
import android.graphics.ColorSpace;

import androidx.room.Entity;

import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.io.Serializable;

@Entity(tableName = "products")
public class Product implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "ServerProductId")
    public int serverProductId = -1;

    @ColumnInfo(name = "Manufacturer")
    public String manufacturer = null;

    @ColumnInfo(name = "Model")
    public String model = null;

    @ColumnInfo(name = "Quantity")
    public int quantity = 0;

    @ColumnInfo(name = "Price")
    public double price = 0;

    @ColumnInfo(name = "Width")
    public int width = 0;

    @ColumnInfo(name = "Height")
    public int height = 0;

    @Override
    public String toString()
    {
        return manufacturer + ", " + model + ", " + quantity + ", " + price + "$";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}

