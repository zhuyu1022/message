package message.centit.com.message.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhu_yu on 2017/10/10.
 */

public class MapDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_TABLE= "create table messageTable(" +
            "id integer primary key autoincrement," +
            "number text," +
            "time text," +
            "body text," +
            "reason text," +
            "type text)";

    public static final String CREATE_TABLE2= "create table messageStatistics(" +
            "id integer primary key autoincrement," +
            "number text," +
            "total int," +
            "sucAccept int," +
            "sucSend int," +
            "failAccept int," +
            "failSend int)";


    public MapDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 创建数据库
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
        sqLiteDatabase.execSQL(CREATE_TABLE2);
    }

    /**
     * 更新数据库
     * @param sqLiteDatabase
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists messageTable");
        sqLiteDatabase.execSQL("drop table if exists messageStatistics");
        onCreate(sqLiteDatabase);
    }


}
