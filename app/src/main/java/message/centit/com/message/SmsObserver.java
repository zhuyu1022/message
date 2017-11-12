package message.centit.com.message;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.centit.GlobalState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import message.centit.com.message.database.MyMessage;
import message.centit.com.message.service.UpLoadService;
import message.centit.com.message.util.LogUtil;

/**
 * Created by zhuyu on 2017/11/12.
 */

public class SmsObserver extends ContentObserver {
    Context mContext;
    Handler mHandler;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    public SmsObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        //每当有新短信到来时，使用我们获取短消息的方法
        getSmsFromPhone(mContext);
    }

    //之查询收件箱
    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    public void getSmsFromPhone(Context context) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
        Cursor c = cr.query(SMS_INBOX, projection, null, null, "date desc");
        if (null == c) {
            Log.i("ooc", "************cur == null");
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (c.moveToNext()) {

            String smsId = c.getString(c.getColumnIndex("_id"));//短信序号
            String number = c.getString(c.getColumnIndex("address"));//手机号
            //String name = c.getString(c.getColumnIndex("person"));//联系人姓名列表
            String body = c.getString(c.getColumnIndex("body"));//短信内容
            // String receiveTime = c.getString(c.getColumnIndex("date"));//日期
            //时间
            long time = c.getLong(c.getColumnIndex("date"));
            String receiveTime = df.format(new Date(time));
            //LogUtil.d( "\n接收时间："+receiveTime+"返回的时间信息："+receiveTime);
            UpLoadService.actionStart(mContext,  receiveTime, body, number);
            sb.append("短信id：" + smsId + "\n发件人手机号码: " + c.getInt(c.getColumnIndex("address")))
                    .append("\n信息内容: " + c.getString(c.getColumnIndex("body")))
                    .append("\n接收时间：" + receiveTime);
        }

        c.close();
        mHandler.obtainMessage(MainActivity.MSG_OUTBOXCONTENT, sb).sendToTarget();

    }


    /**
     * 获取数据库中d大于某个时间的所有短信
     *
     * @param context
     */
    public List<MyMessage> getSmsByTime(Context context,String phoneNumber ,long lastTime) {

       //lastTime=0;
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
        LogUtil.d(phoneNumber);
        LogUtil.d("时间："+lastTime);
       //String where="address = '"+phoneNumber+"'";
        String where="date > '"+lastTime+"'";
        //Cursor c = cr.query(SMS_INBOX, projection, null, null, "date desc");
        Cursor c = cr.query(SMS_INBOX, projection, where, null, "date desc");
        if (null == c) {
            Log.i("ooc", "************cur == null");
            return null;
        }
        List<MyMessage> messageList=new ArrayList<>();
        while (c.moveToNext()) {


            String smsId = c.getString(c.getColumnIndex("_id"));//短信序号
            String number = c.getString(c.getColumnIndex("address"));//手机号

            String body = c.getString(c.getColumnIndex("body"));//短信内容
            // String receiveTime = c.getString(c.getColumnIndex("date"));//日期
            //时间
            long time = c.getLong(c.getColumnIndex("date"));
            String receiveTime = df.format(new Date(time));
            //LogUtil.d( "\n接收时间："+receiveTime+"返回的时间信息："+receiveTime);


            MyMessage message=new MyMessage(number,receiveTime,body,"","");
            messageList.add(message);


        }
        c.close();
        return messageList;
    }


}
