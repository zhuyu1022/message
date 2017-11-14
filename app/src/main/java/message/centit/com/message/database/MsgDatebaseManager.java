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
        dbHelper=new MapDatabaseHelper(context,"Message.db",null,2);
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
            db.execSQL("insert into messageTable(number ,time,body,reason,type) values(?,?,?,?,?)",
                    new Object[]{failMesage.number,failMesage.time,failMesage.body,failMesage.reason,failMesage.type });

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

            Cursor cursor=db.rawQuery("select * from messageTable ",null);
        ArrayList<MyMessage> list=new ArrayList<>();
        if (cursor.moveToFirst()) {


            do {
                MyMessage failMesage =new MyMessage();
                failMesage.number=cursor.getString(1);
                failMesage.time=cursor.getString(2);
                failMesage.body=cursor.getString(3);
                failMesage.reason=cursor.getString(4);
                failMesage.type=cursor.getString(5);

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
    public ArrayList<MyMessage> queryFailMsg(String number,String  type){

        Cursor cursor=db.rawQuery("select * from messageTable where type = '"+type+"' and  number='"+number+"'",null);
        ArrayList<MyMessage> list=new ArrayList<>();
        if (cursor.moveToFirst()) {

            do {
                MyMessage failMesage =new MyMessage();
                failMesage.number=cursor.getString(1);
                failMesage.time=cursor.getString(2);
                failMesage.body=cursor.getString(3);
                failMesage.reason=cursor.getString(4);
                failMesage.type=cursor.getString(5);

                list.add(failMesage);
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;
    }
    /**
     * 查询数据库中该手机号码的最后一条记录的time
     * @return
     */
    public String   querylastTime(String number){

        Cursor cursor=db.rawQuery("select * from messageTable where number='"+number+"'order by time desc limit 1",null);
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
     * 查询数据库最后一条记录的time
     * @return
     */
    public String   querylastTime(){

        Cursor cursor=db.rawQuery("select * from messageTable order by time desc limit 1",null);
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
            db.execSQL("delete from messageTable");

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

    /****************************************************以下是统计表表中的方法*******************************************************/







    public boolean  addStatistics(MessageStatistics msgStatistics){

        if(queryMsgStatistics(msgStatistics.number)!=null){
            return false;
        }
        // Date date=new Date();
        db.beginTransaction(); // 开始事务
        try
        {
           db.execSQL("insert into messageStatistics(number ,total,sucAccept,sucSend,failAccept,failSend) values(?,?,?,?,?,?) ",
                    new Object[]{msgStatistics.number,msgStatistics.total,msgStatistics.sucAccept,msgStatistics.sucSend,msgStatistics.failAccept,msgStatistics.failSend });
             /*  db.execSQL("insert into messageStatistics(number ,total,sucAccept,sucSend,failAccept,failSend) values((case len ( select number from messageStatistics where number ='"+msgStatistics.number+"') >1 then '' else '"+msgStatistics.number+"' end )?,?,?,?,?,?)  ",
                    new Object[]{msgStatistics.number,msgStatistics.total,msgStatistics.sucAccept,msgStatistics.sucSend,msgStatistics.failAccept,msgStatistics.failSend });
                */

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


    public ArrayList<MessageStatistics> queryMsgStatistics(){

        Cursor cursor=db.rawQuery("select * from messageStatistics ",null);
        ArrayList<MessageStatistics> list=new ArrayList<>();
        if (cursor.moveToFirst()) {


            do {
                MessageStatistics msgStatistics=new MessageStatistics();
                msgStatistics.number=cursor.getString(1);
                msgStatistics.total=cursor.getInt(2);
                msgStatistics.sucAccept=cursor.getInt(3);
                msgStatistics.sucSend=cursor.getInt(4);
                msgStatistics.failAccept=cursor.getInt(5);
                msgStatistics.failSend=cursor.getInt(6);

                list.add(msgStatistics);
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;

    }

    public MessageStatistics   queryMsgStatistics(String number){

        Cursor cursor=db.rawQuery("select * from messageStatistics  where number =  '"+number+"'",null);
        MessageStatistics msgStatistics=null;
        if (cursor.moveToFirst()) {
            do {
                msgStatistics=new MessageStatistics();
                msgStatistics.number=cursor.getString(1);
                msgStatistics.total=cursor.getInt(2);
                msgStatistics.sucAccept=cursor.getInt(3);
                msgStatistics.sucSend=cursor.getInt(4);
                msgStatistics.failAccept=cursor.getInt(5);
                msgStatistics.failSend=cursor.getInt(6);

            }while(cursor.moveToNext());

        }
        cursor.close();
        return msgStatistics;

    }




    public boolean  addTotal(String number ){
        // Date date=new Date();
        db.beginTransaction(); // 开始事务
        try
        {
            db.execSQL("update messageStatistics set total=total+1 where number= '"+number +"'");

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
    public boolean  addSucAccept(String number ){
        // Date date=new Date();
        db.beginTransaction(); // 开始事务
        try
        {
            db.execSQL("update messageStatistics set sucAccept=sucAccept+1 where number= '"+number +"'");

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
    public boolean  addSucSend(String number ){
        // Date date=new Date();
        db.beginTransaction(); // 开始事务
        try
        {
            db.execSQL("update messageStatistics set sucSend=sucSend+1 where number= '"+number +"'");

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
    public boolean  addFailAccept(String number ){
        // Date date=new Date();
        db.beginTransaction(); // 开始事务
        try
        {
            db.execSQL("update messageStatistics set failAccept=failAccept+1 where number= '"+number +"'");

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


    public boolean  addFailSend(String number ){
        // Date date=new Date();
        db.beginTransaction(); // 开始事务
        try
        {
            db.execSQL("update messageStatistics set failSend=failSend+1 where number= '"+number +"'");

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
