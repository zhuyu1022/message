package message.centit.com.message.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import message.centit.com.message.MainActivity;
import message.centit.com.message.R;

import message.centit.com.message.SmsObserver;
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
    public static final String EXTRA_MESSAGE= "message";

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

  /*  public static void actionStart(Context context, String receiveTime, String msgBody, String sender) {
        Intent intent = new Intent(context, UpLoadService.class);
        intent.putExtra(EXTRA_TIME, receiveTime);
        intent.putExtra(EXTRA_MSG, msgBody);
        intent.putExtra(EXTRA_SENDER, sender);
        context.startService(intent);
    }*/


    public static void actionStart(Context context,MyMessage message) {
        Intent intent = new Intent(context, UpLoadService.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.startService(intent);
    }

    public UpLoadService() {
    }


    /**   短信接收时间**/
    String time="";
    /**短信内容**/
    String body="";
    /**上传之前的短信内容**/
    String msgBodybeforeUpload="" ;
    /**发送方**/
    String number="";

    String reason="";
    String type="3";

   /* int total=0;
    int  sucAccept=0;
    int  sucSend=0;
    int  failAccept=0;
    int  failSend=0;*/
    UpLoadBinder mBinder=new UpLoadBinder();
    //临时存放短信内容
    String tempMsg="";
    //记录上传服务器之前的短信临时内容
    String msgStrBeforeUpload="";
    SmsObserver smsObserver;
    //收件箱uri，如果不写inbox 会调用3次监听事件，
    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    //采用队列的形式
    Queue <MyMessage> messageQueue;
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
        messageQueue=new LinkedList<>();
       // registerObserver();
    }
boolean isReadMsg=true;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        //messageQueue
        final Thread  parseMsg= new Thread(new Runnable() {
            @Override
            public void run() {
                if (intent!=null){
                    MyMessage receiveMessage=new MyMessage();
                    receiveMessage= (MyMessage) intent.getExtras().get(EXTRA_MESSAGE);
                    messageQueue.offer(receiveMessage);
                   if(isReadMsg){
                       isReadMsg=false;
                       //解析并上传短信
                        parseMsgAndUpload(receiveMessage);

                   }

                LogUtil.d("uploadservice服务启动");
                    LogUtil.d("receiveTime:" + time + "msgBody:" + body + "sender:" + number);
                }
            }
        });

        parseMsg.start();
        return super.onStartCommand(intent, flags, startId);
    }


    private void parseMsgAndUpload(MyMessage receiveMessage){

            time = receiveMessage.time;
            body = receiveMessage.body;
            number = receiveMessage.number;

        // time = intent.getStringExtra(EXTRA_TIME);
        //body = intent.getStringExtra(EXTRA_MSG);
        // number = intent.getStringExtra(EXTRA_SENDER);
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
            for (int i = 0; i < phoneList.size(); i++) {      //如果是用户输入的其中的一个号码，才进行解析
                if (number.equals(phoneList.get(i).trim())) {
                    body=body.trim();
                    msgBodybeforeUpload=msgBodybeforeUpload+body;
                    if (body.startsWith(MSG_START)){
                        body=body.substring(MSG_START.length());
                    }else if (body.startsWith("【")){
                        //短息头不正确
                        dbManager.addFailAccept(number);
                        MyMessage message=new MyMessage(number,time,msgBodybeforeUpload,"短信头格式错误",TYPE_FAILACCEPT);
                        dbManager.add(message);
                        if (listener!=null){
                            listener.onFailAccept();
                        }
                        tempMsg="";
                        msgBodybeforeUpload="";
                        isReadMsg=true;
                        //移除队列首个元素
                        messageQueue.poll();
                        return;
                    }
                    //当短信内容以结束标志结束时才上传服务器
                    if (body.endsWith(MSG_END)){
                        //到这说明成功接收到一条指定号码的短信，切短信以#结尾
                        dbManager.addTotal(number);
                        if (listener!=null){
                            listener.onTotal();
                        }
                        //截取"#"之前的短信内容，拼接上一次的tempMsg，并存入临时变量
                        tempMsg=tempMsg+body.substring(0,body.lastIndexOf(MSG_END));
                        //上传服务器之前先判断json格式是否正确,笨方法，但是能解决问题
                        try {
                            new JSONObject(tempMsg);
                            //如果没有异常 说明json格式正确，接收成功！
                            dbManager.addSucAccept(number);
                            if (listener!=null){
                                listener.onSucAccept();
                            }
                            uploadMessage(tempMsg);
                            tempMsg="";
                            //移除队列首个元素
                            messageQueue.poll();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //出现异常 说明接收失败，传过来的json格式有问题
                            dbManager.addFailAccept(number);
                            MyMessage message=new MyMessage(number,time,msgBodybeforeUpload,"json格式错误",TYPE_FAILACCEPT);
                            dbManager.add(message);
                            if (listener!=null){
                                listener.onFailAccept();
                            }
                            tempMsg="";
                            msgBodybeforeUpload="";
                            isReadMsg=true;
                            //移除队列首个元素
                            messageQueue.poll();
                        }
                    }else{
                        //若没有#号 把内容赋给tempMsg
                        tempMsg=body;
                        isReadMsg=true;
                    }
                    break;
                }
            }
        }
    }

    /**
     * 注册
     */
   private void registerObserver(){
            smsObserver = new SmsObserver(this, null);
            getContentResolver().registerContentObserver(SMS_INBOX, true,
                    smsObserver);
        }



    /**
     * 根据字符串解析号码，并增加+86号码
     * @param phones
     * @return
     */
    private List<String > getPhoneListfromStr(String phones) {

List<String > phoneList=new ArrayList<>();

        if (!TextUtils.isEmpty(phones)) {
            if (phones.contains(",")) {
                String[] tempphones = phones.split(",");
                for (int i = 0; i < tempphones.length; i++) {
                    phoneList.add(tempphones[i]);
                    phoneList.add("+86" + tempphones[i]);
                }
            } else {
                //phoneList = new String[]{phoneNoStr};
                phoneList.add(phones);
                phoneList.add("+86" + phones);
            }
        }

        return  phoneList;
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

     /***
      * 读取队列中的下个元素并上传
      */
    private void readNextMessageFromQueue(){

        if (messageQueue.size()>0){
            //返回队列首个元素
            MyMessage nextMessage=messageQueue.peek();
            parseMsgAndUpload(nextMessage);
        }else{
            //队列为空，可以通过onstartcommand来上传下一条短信了
            isReadMsg=true;
        }
    }



    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

            //网络异常 说明发送失败
            dbManager.addFailSend(number);
            MyMessage message=new MyMessage(number,time,msgBodybeforeUpload,"连接超时，网络异常",TYPE_FAILSEND);
            dbManager.add(message);
            if (listener!=null){
                listener.onFailSend();
            }
            msgBodybeforeUpload="";
            isReadMsg=true;
            //移除队列首个元素
            messageQueue.poll();
            readNextMessageFromQueue();

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        if (response.code()!=200){
                //网络异常 说明发送失败
                dbManager.addFailSend(number);
             // failSend++;
              //  SharedUtil.putValue(UpLoadService.this,SharedUtil.failSend,failSend);*//*
                MyMessage message=new MyMessage(number,time,msgBodybeforeUpload,"服务器异常",TYPE_FAILSEND);
                dbManager.add(message);
                if (listener!=null){
                    listener.onFailSend();
                }
                msgBodybeforeUpload="";
            isReadMsg=true;
            //移除队列首个元素
            messageQueue.poll();
                readNextMessageFromQueue();
                return;
            }
            String result = response.body().string();
            Log.d("result", result);
            try {

                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj != null) {
                    String retCode = jsonObj.optString("retCode");
                    if (retCode != null && retCode.equals("0")) {
                        dbManager.addSucSend(number);
                              /*   sucSend++;
                                 SharedUtil.putValue(UpLoadService.this,SharedUtil.sucSend,sucSend);*/
                        MyMessage message=new MyMessage(number,time,msgBodybeforeUpload,"成功发送",TYPE_SUCCESS);
                        dbManager.add(message);
                        if (listener!=null){
                            listener.onSucSend();
                        }
                        msgBodybeforeUpload="";
                        isReadMsg=true;
                        //移除队列首个元素
                        messageQueue.poll();
                        readNextMessageFromQueue();
                        return;
                    }else{
                        dbManager.addFailSend(number);
                       /* failSend++;
                        SharedUtil.putValue(UpLoadService.this,SharedUtil.failSend,failSend);*/
                        MyMessage message=new MyMessage(number,time,msgBodybeforeUpload,"服务器解析数据失败",TYPE_FAILSEND);
                        dbManager.add(message);
                        if (listener!=null){
                            listener.onFailSend();
                        }
                        msgBodybeforeUpload="";
                        isReadMsg=true;
                        //移除队列首个元素
                        messageQueue.poll();
                        readNextMessageFromQueue();
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
       // void onResult();
        void onTotal();
        void onSucAccept();
        void onSucSend();
        void onFailAccept();
        void onFailSend();
    }

    public void setOnUploadListener( OnUploadListener listener){
         this. listener=listener;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        //getContentResolver().unregisterContentObserver(smsObserver);
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
