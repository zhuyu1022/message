package message.centit.com.message;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.centit.GlobalState;
import com.centit.app.cmipConstant.Constant_Mgr;
import com.centit.core.baseView.baseUI.MIPBaseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import message.centit.com.message.database.FailMesage;
import message.centit.com.message.database.MsgDatebaseManager;
import message.centit.com.message.net.ServiceImpl;
import message.centit.com.message.util.LogUtil;
import message.centit.com.message.util.SharedUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpLoadService extends MIPBaseService {
    public static final String EXTRA_TIME = "receiveTime";
    public static final String EXTRA_MSG = "msgBody";
    public static final String EXTRA_SENDER = "sender";

        private static final String MSG_START="*";
    private static final String MSG_END="#";
    /**   失败类型    1  失败发送 **/
    private static final String TYPE_FAILSEND="0";
    /**   失败类型    0   失败接收  **/
    private static final String TYPE_FAILACCEPT="1";

    private MsgDatebaseManager dbManager;
    String phoneNoStr;
    String webAddress;
    private String[] phoneList;

    /**
     * 封装供外部启动服务
     *
     * @param context
     * @param receiveTime
     * @param msgBody
     * @param sender
     */
    public static void actionStart(Context context, String receiveTime, String msgBody, String sender) {
        Intent intent = new Intent(context, UpLoadService.class);
        intent.putExtra(EXTRA_TIME, receiveTime);
        intent.putExtra(EXTRA_MSG, msgBody);
        intent.putExtra(EXTRA_SENDER, sender);
        context.startService(intent);
    }

    public UpLoadService() {
    }
    /**   短信接收时间**/
    String receiveTime="";
    /**短信内容**/
    String msgBody="" ;
    /**发送方**/
    String sender="";
    int total=0;
    int  sucAccept=0;
    int  sucSend=0;
    int  failAccept=0;
    int  failSend=0;
    UpLoadBinder mBinder=new UpLoadBinder();
    //临时存放短信内容
    String tempMsg="";
    //记录上传服务器之前的短信临时内容
String msgStrBeforeUpload="";



    //用于获取service实例
    public class UpLoadBinder extends Binder {
        public UpLoadService getService() {
            return UpLoadService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
       return mBinder;
    }

    @Override
    public void onCreate() {
        LogUtil.d("");
        super.onCreate();
        startForeground();
        dbManager=new MsgDatebaseManager(this);

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent!=null){
             receiveTime = intent.getStringExtra(EXTRA_TIME);
            msgBody = intent.getStringExtra(EXTRA_MSG);
             sender = intent.getStringExtra(EXTRA_SENDER);

            total= (int) SharedUtil.getValue(this,SharedUtil.total,0);
            sucAccept=(int) SharedUtil.getValue(this,SharedUtil.sucAccept,0);
            sucSend=(int) SharedUtil.getValue(this,SharedUtil.sucSend,0);
            failAccept=(int) SharedUtil.getValue(this,SharedUtil.failAccept,0);
            failSend=(int) SharedUtil.getValue(this,SharedUtil.failSend,0);

            phoneNoStr = GlobalState.getInstance().getPhoneStrs();
            webAddress = GlobalState.getInstance().getmIPAddr();

            if (!TextUtils.isEmpty(phoneNoStr) && !TextUtils.isEmpty(webAddress)) {
                if (phoneNoStr.contains(",")) {
                    phoneList = phoneNoStr.split(",");
                } else {
                    phoneList = new String[]{phoneNoStr};
                }

                for (int i = 0; i < phoneList.length; i++) {      //如果是用户输入的其中的一个号码，就上传服务器
                    if (sender.equals(phoneList[i].trim())) {

                        //到这说明成功接收到一条指定号码的短信
                        total++;

                        //保存
                        SharedUtil.putValue(this,SharedUtil.total,total);
                        if (listener!=null){
                            listener.onResult();
                        }
                        //当短信内容以结束标志结束时才上传服务器
                        if (msgBody.endsWith(MSG_END)){

                            //截取"#"之前的短信内容，并存入临时变量
                            tempMsg=msgBody.substring(0,msgBody.lastIndexOf(MSG_END));

                            //上传服务器之前先判断json格式是否正确,笨方法，但是能解决问题
                            try {
                                new JSONObject(tempMsg);
                                //如果没有异常 说明json格式正确，接收成功！
                                sucAccept++;
                                //保存
                                SharedUtil.putValue(this,SharedUtil.sucAccept,sucAccept);
                                if (listener!=null){
                                    listener.onResult();
                                }

                                msgStrBeforeUpload=tempMsg;
                                uploadMessage(tempMsg);
                                //上传之后清空

                            } catch (JSONException e) {
                                e.printStackTrace();
                                //出现异常 说明接收失败，传过来的json格式有问题
                                failAccept++;
                                SharedUtil.putValue(this,SharedUtil.failAccept,failAccept);
                                FailMesage   failMesage=new FailMesage(sender,receiveTime,tempMsg,"json格式错误",TYPE_FAILACCEPT);
                                //只有失败的情况才要加入数据库
                                dbManager.add(failMesage);
                                if (listener!=null){
                                    listener.onResult();
                                }

                            }

                            tempMsg="";
                        }else{
                            tempMsg=msgBody;
                        }
                        break;
                    }
                }


            }

            LogUtil.d("uploadservice服务启动");
            LogUtil.d("receiveTime:" + receiveTime + "msgBody:" + msgBody + "sender:" + sender);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    //将service变为前台进程
    private void startForeground() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("短信监听")
                .setContentText("正在运行，请勿关闭")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.icon_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_logo))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
    }

    /**
     * 上传短信内容
     *
     * @param msgBody
     */
    private void uploadMessage(String msgBody) {

       /* {
            "messageid": "消息id",
                "content": "消息内容",
                "lng": "经度",
                "lat": "纬度",
                "useridList": [{
            "userid": "钉钉人员id",
                    "username": "钉钉人员名称"
        }]
        }*/
        // ServiceImpl.acceptMessage(null,mHandler,ServiceImpl.TYPE_AcceptMessage,msgBody);
        ServiceImpl.acceptMessage(ServiceImpl.TYPE_AcceptMessage, msgBody, callback);
    }

    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

            //网络异常 说明发送失败
            failSend++;
            SharedUtil.putValue(UpLoadService.this,SharedUtil.failSend,failSend);
            FailMesage failMesage=new FailMesage(sender,receiveTime,msgStrBeforeUpload,"网络链接异常",TYPE_FAILSEND);
            //只有失败的情况才要加入数据库
            dbManager.add(failMesage);

            if (listener!=null){
                listener.onResult();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            if (response.code()==200){
                sucSend++;
                SharedUtil.putValue(UpLoadService.this,SharedUtil.sucSend,sucSend);
                if (listener!=null){
                    listener.onResult();
                }
            }else{
                //网络异常 说明发送失败
                failSend++;
                SharedUtil.putValue(UpLoadService.this,SharedUtil.failSend,failSend);
                FailMesage failMesage=new FailMesage(sender,receiveTime,msgStrBeforeUpload,"链接服务器接口异常",TYPE_FAILSEND);
                //只有失败的情况才要加入数据库
                dbManager.add(failMesage);
            }
            String result = response.body().string();
            Log.d("result", result);
            try {

                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj != null) {
                    String retCode = jsonObj.optString("retCode");
                    if (retCode != null && retCode.equals("0")) {

                             if (listener!=null){
                                 listener.onResult();
                             }

                        return;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
     OnUploadListener listener;
public interface  OnUploadListener{
        void onResult();
    }

    public void setOnUploadListener( OnUploadListener listener){
         this. listener=listener;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPostHandle(int requestType, Object objHeader, Object objBody, boolean error, int errorCode) {
        if (error) {
            switch (requestType) {
                case ServiceImpl.TYPE_AcceptMessage:

                    if (objBody != null && objBody instanceof String) {
                        try {
                            Log.d("返回的数据为：", objBody.toString());
                            JSONObject jsonObj = new JSONObject((String) objBody);
                            if (jsonObj != null) {
                                String retCode = jsonObj.optString("retCode");

                                if (retCode != null && retCode.equals("0")) {

                                    Toast.makeText(this, "上传成功！", Toast.LENGTH_SHORT).show();

                                    return;
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;

            }
        } else {
            switch (requestType) {

            }
        }
    }
}
