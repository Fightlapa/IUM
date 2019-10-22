package packages.products;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Query;


@Dao
public interface ProductDao {
    @Query("SELECT * FROM products")
    List<Product> getAll();

    @Insert
    void insert(Product Product);

    @Delete
    void delete(Product Product);
}