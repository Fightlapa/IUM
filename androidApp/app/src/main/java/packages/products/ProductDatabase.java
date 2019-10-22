package packages.products;


import androidx.room.RoomDatabase;
import androidx.room.Database;

@Database(entities = {Product.class}, version = 1, exportSchema = false)
public abstract class ProductDatabase extends RoomDatabase {
    public abstract ProductDao ProductDao();
}
