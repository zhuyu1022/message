package message.centit.com.message.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.centit.GlobalState;
import com.centit.core.baseView.baseUI.MIPBaseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import message.centit.com.message.MainActivity;
import message.centit.com.message.R;

import message.centit.com.message.database.MsgDatebaseManager;
import message.centit.com.message.database.MyMessage;
import message.centit.com.message.net.ServiceImpl;
import message.centit.com.message.util.LogUtil;
import message.centit.com.message.util.SharedUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpLoadService extends MIPBaseService {
    public static final String EXTRA_TIME = "receiveTime";
    public static final String EXTRA_MSG = "msgBody";
    public static final String EXTRA_SENDER = "sender";

        private static final String MSG_START="【嘉兴公安】";
    private static final String MSG_END="#";
    /**   失败类型    1  失败发送 **/
    private static final String TYPE_FAILSEND="0";
    /**   失败类型    0   失败接收  **/
    private static final String TYPE_FAILACCEPT="1";
    /***   成功  */
    private static final String TYPE_SUCCESS="2";
    private MsgDatebaseManager dbManager;
    String phoneNoStr;
    String webAddress;
   // private String[] phoneList;
    private List<String> phoneList=new ArrayList<>();
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
    String smsId="";
    /**   短信接收时间**/
    String receiveTime="";
    /**短信内容**/
    String msgBody="";
    /**上传之前的短信内容**/
    String msgBodybeforeUpload="" ;
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (intent!=null){
                    receiveTime = intent.getStringExtra(EXTRA_TIME);
                    msgBody = intent.getStringExtra(EXTRA_MSG);
                    sender = intent.getStringExtra(EXTRA_SENDER);

                    total= (int) SharedUtil.getValue(UpLoadService.this,SharedUtil.total,0);
                    sucAccept=(int) SharedUtil.getValue(UpLoadService.this,SharedUtil.sucAccept,0);
                    sucSend=(int) SharedUtil.getValue(UpLoadService.this,SharedUtil.sucSend,0);
                    failAccept=(int) SharedUtil.getValue(UpLoadService.this,SharedUtil.failAccept,0);
                    failSend=(int) SharedUtil.getValue(UpLoadService.this,SharedUtil.failSend,0);

                    phoneNoStr = GlobalState.getInstance().getPhoneStrs();
                    webAddress = GlobalState.getInstance().getmIPAddr();

                    if (!TextUtils.isEmpty(phoneNoStr) && !TextUtils.isEmpty(webAddress)) {
                        if (phoneNoStr.contains(",")) {
                           String [] tempphones = phoneNoStr.split(",");
                            for (int i = 0; i <tempphones.length ; i++) {
                                phoneList.add(tempphones[i]);
                                phoneList.add("+86"+tempphones[i]);
                            }
                        } else {
                            //phoneList = new String[]{phoneNoStr};
                            phoneList.add(phoneNoStr);
                            phoneList.add("+86"+phoneNoStr);
                        }

                        for (int i = 0; i < phoneList.size(); i++) {      //如果是用户输入的其中的一个号码，就上传服务器
                            if (sender.equals(phoneList.get(i).trim())) {
                                msgBody=msgBody.trim();
                                msgBodybeforeUpload=msgBodybeforeUpload+msgBody;
                                if (msgBody.startsWith(MSG_START)){
                                    msgBody=msgBody.substring(MSG_START.length());
                                }else if (msgBody.startsWith("【")){
                                    //短息头不正确
                                    failAccept++;
                                    SharedUtil.putValue(UpLoadService.this,SharedUtil.failAccept,failAccept);
                                    //  MyMessage message=new MyMessage(sender,receiveTime,tempMsg,"json格式错误",TYPE_FAILACCEPT);
                                    MyMessage message=new MyMessage(sender,receiveTime,msgBodybeforeUpload,"短信头格式错误",TYPE_FAILACCEPT);
                                    //保存数据库
                                    dbManager.add(message);
                                    if (listener!=null){
                                        listener.onResult();
                                    }
                                    msgBodybeforeUpload="";
                                    return;
                                }


                                //当短信内容以结束标志结束时才上传服务器
                                if (msgBody.endsWith(MSG_END)){
                                    //到这说明成功接收到一条指定号码的短信，切短信以#结尾
                                    total++;

                                    //保存
                                    SharedUtil.putValue(UpLoadService.this,SharedUtil.total,total);
                                    if (listener!=null){
                                        listener.onResult();
                                    }
                                    //截取"#"之前的短信内容，拼接上一次的tempMsg，并存入临时变量
                                    tempMsg=tempMsg+msgBody.substring(0,msgBody.lastIndexOf(MSG_END));
                                    //上传服务器之前先判断json格式是否正确,笨方法，但是能解决问题
                                    try {
                                        new JSONObject(tempMsg);
                                        //如果没有异常 说明json格式正确，接收成功！
                                        sucAccept++;
                                        //保存
                                        SharedUtil.putValue(UpLoadService.this,SharedUtil.sucAccept,sucAccept);
                                        if (listener!=null){
                                            listener.onResult();
                                        }

                                      //  msgStrBeforeUpload=tempMsg;
                                        uploadMessage(tempMsg);


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        //出现异常 说明接收失败，传过来的json格式有问题
                                        failAccept++;
                                        SharedUtil.putValue(UpLoadService.this,SharedUtil.failAccept,failAccept);
                                      //  MyMessage message=new MyMessage(sender,receiveTime,tempMsg,"json格式错误",TYPE_FAILACCEPT);
                                        MyMessage message=new MyMessage(sender,receiveTime,msgBodybeforeUpload,"json格式错误",TYPE_FAILACCEPT);
                                        //保存数据库
                                        dbManager.add(message);
                                        if (listener!=null){
                                            listener.onResult();
                                        }
                                        msgBodybeforeUpload="";

                                    }
                                    //上传之后清空
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
            }
        }).start();


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
            MyMessage message=new MyMessage(sender,receiveTime,msgBodybeforeUpload,"连接超时，网络异常",TYPE_FAILSEND);
            //只有失败的情况才要加入数据库
            dbManager.add(message);

            if (listener!=null){
                listener.onResult();
            }
            msgBodybeforeUpload="";
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            if (response.code()!=200){
                //网络异常 说明发送失败
                failSend++;
                SharedUtil.putValue(UpLoadService.this,SharedUtil.failSend,failSend);
                MyMessage message=new MyMessage(sender,receiveTime,msgBodybeforeUpload,"服务器接口异常",TYPE_FAILSEND);
                dbManager.add(message);

            }
            String result = response.body().string();
            Log.d("result", result);
            try {

                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj != null) {
                    String retCode = jsonObj.optString("retCode");
                    if (retCode != null && retCode.equals("0")) {
                                 sucSend++;
                                 SharedUtil.putValue(UpLoadService.this,SharedUtil.sucSend,sucSend);
                        MyMessage message=new MyMessage(sender,receiveTime,msgBodybeforeUpload,"成功发送",TYPE_SUCCESS);
                        dbManager.add(message);
                             if (listener!=null){
                                 listener.onResult();
                             }

                        return;
                    }else{
                        failSend++;
                        SharedUtil.putValue(UpLoadService.this,SharedUtil.failSend,failSend);
                        MyMessage message=new MyMessage(sender,receiveTime,msgBodybeforeUpload,"服务器解析数据失败",TYPE_FAILSEND);
                        //只有失败的情况才要加入数据库
                        dbManager.add(message);
                        if (listener!=null){
                            listener.onResult();
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            msgBodybeforeUpload="";
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
