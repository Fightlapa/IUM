package packages.products;


import androidx.room.RoomDatabase;
import androidx.room.Database;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Product.class, Request.class}, version = 6, exportSchema = false)
public abstract class ProductDatabase extends RoomDatabase {
    public abstract ProductDao ProductDao();
    public abstract RequestDao RequestDao();


    static final Migration MIGRATION_5_6 = new Migration(4, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE products "
                    + " ADD COLUMN Height INTEGER NOT NULL DEFAULT 0 ");

            database.execSQL("ALTER TABLE products "
                    + " ADD COLUMN Width INTEGER NOT NULL DEFAULT 0 ");
        }
    };
}
