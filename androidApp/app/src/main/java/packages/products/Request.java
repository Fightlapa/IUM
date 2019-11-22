package packages.products;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "requests")
public class Request implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "Url")
    public String url = null;

    @ColumnInfo(name = "Method")
    public String method = null;

    @ColumnInfo(name = "JsonString")
    public String jsonString = "";

    @ColumnInfo(name = "LocalProductId")
    public Integer localProductId = -1;

    @ColumnInfo(name = "ServerProductId")
    public Integer serverProductId = -1;
}

