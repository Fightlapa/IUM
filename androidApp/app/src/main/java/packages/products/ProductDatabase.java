package packages.products;


import androidx.room.RoomDatabase;
import androidx.room.Database;

@Database(entities = {Product.class, Request.class}, version = 4, exportSchema = false)
public abstract class ProductDatabase extends RoomDatabase {
    public abstract ProductDao ProductDao();
    public abstract RequestDao RequestDao();
}
