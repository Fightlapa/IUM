package packages.products;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface RequestDao {
    @Query("SELECT * FROM requests ORDER BY uid")
    List<Request> getAll();

    @Insert
    long insert(Request Product);

    @Delete
    void delete(Request Product);
}