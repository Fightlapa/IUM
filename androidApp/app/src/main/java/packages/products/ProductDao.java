package packages.products;

import java.lang.annotation.Target;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public interface ProductDao {
    @Query("SELECT * FROM products")
    List<Product> getAll();

    @Insert
    long insert(Product Product);

    @Query("SELECT * from products where id = :id LIMIT 1")
    Product getByLocalId(int id);

    @Update
    void update(Product product);

    @Delete
    void delete(Product Product);

    @Query("DELETE FROM products")
    void deleteAll();
}