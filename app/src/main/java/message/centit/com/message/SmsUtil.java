package message.centit.com.message;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhuyu on 2017/11/12.
 */

public class SmsUtil {

    private Uri SMS_INBOX = Uri.parse("content://sms/");

    public void getSmsFromPhone(Context context) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
        if (null == cur) {
            Log.i("ooc", "************cur == null");
            return;
        }
        while (cur.moveToNext()) {

            String smsId = cur.getString(cur.getColumnIndex("_id"));//短信序号
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            //String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));//短信内容
            String receiveTime = cur.getString(cur.getColumnIndex("date"));//日期

            //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。

        }
    }
}
/*
sms主要结构：
        　　
        　　_id：短信序号，如100
        　　
        　　thread_id：对话的序号，如100，与同一个手机号互发的短信，其序号是相同的
        　　
        　　address：发件人地址，即手机号，如+8613811810000
        　　
        　　person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null
        　　
        　　date：日期，long型，如1256539465022，可以对日期显示格式进行设置
        　　
        　　protocol：协议0SMS_RPOTO短信，1MMS_PROTO彩信
        　　
        　　read：是否阅读0未读，1已读
        　　
        　　status：短信状态-1接收，0complete,64pending,128failed
        　　
        　　type：短信类型1是接收到的，2是已发出
        　　
        　　body：短信具体内容
        　　
        　　service_center：短信服务中心号码编号，如+8613800755500

        */
