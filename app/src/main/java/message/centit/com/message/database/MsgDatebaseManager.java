package message.centit.com.message.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by zhu_yu on 2017/10/10.
 */

public class MsgDatebaseManager {
    private MapDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public MsgDatebaseManager(Context context) {
        //打开、创建数据库
        dbHelper=new MapDatabaseHelper(context,"Message.db",null,1);
        //获取数据库的实例
        db=dbHelper.getWritableDatabase();
    }

    /**
     * 添加数据
     * @param failMesage
     * @return
     */
    public boolean  add(MyMessage failMesage){
       // Date date=new Date();
       db.beginTransaction(); // 开始事务
        try
        {
            db.execSQL("insert into failMessageTable(no ,time,content,reason,failtype) values(?,?,?,?,?)",
                    new Object[]{failMesage.no,failMesage.time,failMesage.content,failMesage.reason,failMesage.failtype });

            db.setTransactionSuccessful(); // 设置事务成功完成
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            db.endTransaction(); // 结束事务
        }
        return true;
    }


    /**
     * 查询所有数据
     * @return
     */
    public ArrayList<MyMessage> query(){

            Cursor cursor=db.rawQuery("select * from failMessageTable ",null);
        ArrayList<MyMessage> list=new ArrayList<>();
        if (cursor.moveToFirst()) {


            do {
                MyMessage failMesage =new MyMessage();
                failMesage.no=cursor.getString(1);
                failMesage.time=cursor.getString(2);
                failMesage.content=cursor.getString(3);
                failMesage.reason=cursor.getString(4);
                failMesage.failtype=cursor.getString(5);

                list.add(failMesage);
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;

    }

    /**
     * 查询所有失败的短信
     * @return
     */
    public ArrayList<MyMessage> queryFailMsg(){

        Cursor cursor=db.rawQuery("select * from failMessageTable where failtype <> '2'",null);
        ArrayList<MyMessage> list=new ArrayList<>();
        if (cursor.moveToFirst()) {


            do {
                MyMessage failMesage =new MyMessage();
                failMesage.no=cursor.getString(1);
                failMesage.time=cursor.getString(2);
                failMesage.content=cursor.getString(3);
                failMesage.reason=cursor.getString(4);
                failMesage.failtype=cursor.getString(5);

                list.add(failMesage);
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;

    }
    /**
     * 查询数据库最后一条记录的time
     * @return
     */
    public String   querylastTime(){

        Cursor cursor=db.rawQuery("select * from failMessageTable order by time desc limit 1",null);
        String time ="";
        if (cursor.moveToFirst()) {
            do {

                time =cursor.getString(2);

            }while(cursor.moveToNext());

        }
        cursor.close();
        return time;

    }


    /**
     * 清空数据库数据
     * @return
     */
    public boolean clear(){
        db.beginTransaction(); // 开始事务
        try
        {
            db.execSQL("delete from failMessageTable");

            db.setTransactionSuccessful(); // 设置事务成功完成

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            db.endTransaction(); // 结束事务
        }
        return true;
    }



}
