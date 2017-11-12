package message.centit.com.message;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centit.GlobalState;
import com.centit.app.cmipConstant.Constant_Mgr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import message.centit.com.message.activity.FailMsgActivity;
import message.centit.com.message.database.MsgDatebaseManager;
import message.centit.com.message.database.MyMessage;
import message.centit.com.message.service.UpLoadService;
import message.centit.com.message.util.LogUtil;
import message.centit.com.message.util.SharedUtil;
import message.centit.com.message.util.SimpleDialog;

public class MainActivity extends AppCompatActivity {

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private EditText phoneNoEt;
    private EditText webAddressEt;
    private Button okBtn;
    private LinearLayout statisticLayout;

    private LinearLayout failAcceptLayout;
    private LinearLayout failSendLayout;
//
//    "sendNewsUrl":"消息推送跳转url"，
//            "sendNewsAgentid":"消息推送跳转agentid"，
//            "sendNewsType":"消息推送跳转类型"
    private EditText sendNewsUrlEt;
    private EditText sendNewsAgentidEt;
    private EditText sendNewsTypeEt;


    private TextView totalTv;
    private TextView sucAcceptTv;
    private TextView sucSendTv;
    private TextView failAcceptTv;
    private TextView failSendTv;


    private String sendNewsUrl;
    private String sendNewsAgentid;
    private String sendNewsType;

    int total=0;
    int  sucAccept=0;
    int  sucSend=0;
    int  failAccept=0;
    int  failSend=0;
private MsgDatebaseManager dbManager;
    SmsObserver  smsObserver;
    //收件箱uri，如果不写inbox 会调用3次监听事件，
    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    private static final String DIALOG_ADD="AddPhoneDialog";
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtil.d("");
            setContentView(R.layout.activity_main);
            initDate();
            initView();
            dbManager=new MsgDatebaseManager(this);
            Intent intent=new Intent(this, UpLoadService.class);
            //绑定服务
            bindService(intent,connection, Context.BIND_AUTO_CREATE);

            registerObserver();
            restoreMsg();


        }

    /**
     * 注册
     */
    private void registerObserver(){
            smsObserver = new SmsObserver(this, smsHandler);
            getContentResolver().registerContentObserver(SMS_INBOX, true,
                    smsObserver);
        }

    /**
     * 从数据库中比对短信，如果没有改短信，说明需要重新读取并上传
     */
    private void restoreMsg() {
     /*   String phoneNoStr = GlobalState.getInstance().getPhoneStrs();
        String[] phoneList;
        if (!TextUtils.isEmpty(phoneNoStr)) {
            if (phoneNoStr.contains(",")) {
                phoneList = phoneNoStr.split(",");
            } else {
                phoneList = new String[]{phoneNoStr};
            }

            for (int i = 0; i < phoneList.length; i++) {      //如果是用户输入的其中的一个号码，就上传服务器


            }*/

        String time = dbManager.querylastTime();
        Date date=null;
        try {
             date=df.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date!=null) {
            //需要传一个long型的时间进去，切记
            List<MyMessage> messageList = smsObserver.getSmsByTime(this, "123456", date.getTime());
            LogUtil.d("共查到数据：" + messageList.size());
            for (int i = 0; i < messageList.size(); i++) {
                MyMessage message = messageList.get(i);
                String receiveTime = message.time;
                String body = message.content;
                String number = message.no;
               UpLoadService.actionStart(this, receiveTime, body, number);
            }
        }

    }



    // Message 类型值
    public  static final int MSG_AIRPLANE = 1;
    public static final int MSG_OUTBOXCONTENT = 2;
    private Handler smsHandler = new Handler() {

        public void handleMessage(Message msg) {

            System.out.println("---mHanlder----");
            switch (msg.what) {
              /*  case MSG_AIRPLANE:
                    int isAirplaneOpen = (Integer) msg.obj;
                    if (isAirplaneOpen != 0)
                        tvAirplane.setText("飞行模式已打开");
                    else if (isAirplaneOpen == 0)
                        tvAirplane.setText("飞行模式已关闭");
                    break;*/
                case MSG_OUTBOXCONTENT:
                    String outbox = msg.obj.toString();
                  //  Toast.makeText(MainActivity.this, outbox, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    private void initDate(){
         //初始化APP配置
         String url= Constant_Mgr.getMIP_BASEURL();
         GlobalState.getInstance().setmRequestURL(url);

        sendNewsUrl=GlobalState.getInstance().getSendNewsUrl();
     sendNewsAgentid=GlobalState.getInstance().getSendNewsAgentid();
        sendNewsType=GlobalState.getInstance().getSendNewsType();

        total= (int) SharedUtil.getValue(this,SharedUtil.total,0);
          sucAccept=(int) SharedUtil.getValue(this,SharedUtil.sucAccept,0);
         sucSend=(int) SharedUtil.getValue(this,SharedUtil.sucSend,0);
        failAccept=(int) SharedUtil.getValue(this,SharedUtil.failAccept,0);
         failSend=(int) SharedUtil.getValue(this,SharedUtil.failSend,0);


    }
    private void initView(){

        statisticLayout= (LinearLayout) findViewById(R.id.statisticLayout);

         failAcceptLayout=(LinearLayout) findViewById(R.id.failAcceptLl);
        failSendLayout=(LinearLayout) findViewById(R.id.failSendLl);

        phoneNoEt= (EditText) findViewById(R.id.phoneEt);
        webAddressEt= (EditText) findViewById(R.id.webAdressEt);

        sendNewsUrlEt=(EditText) findViewById(R.id.sendNewsUrlEt);
       sendNewsAgentidEt=(EditText) findViewById(R.id.sendNewsAgentidEt);
        sendNewsTypeEt=(EditText) findViewById(R.id.sendNewsTypeEt);


       totalTv= (TextView) findViewById(R.id.totalTv);
         sucAcceptTv= (TextView) findViewById(R.id.sucAcceptTv);
      sucSendTv= (TextView) findViewById(R.id.sucSendTv);
        failAcceptTv= (TextView) findViewById(R.id.failAcceptTv);
       failSendTv= (TextView) findViewById(R.id.failSendTv);

        sendNewsUrlEt.setText(sendNewsUrl);
                sendNewsAgentidEt.setText(sendNewsAgentid);
        sendNewsTypeEt.setText(sendNewsType);

        totalTv.setText(total+"");
        sucAcceptTv.setText(sucAccept+"");
        sucSendTv.setText(sucSend+"");
        failAcceptTv.setText(failAccept+"");
        failSendTv.setText(failSend+"");

        String phoneNo= GlobalState.getInstance().getPhoneStrs();
        phoneNoEt.setText(phoneNo);


        String url= GlobalState.getInstance().getmIPAddr();
        String port= GlobalState.getInstance().getmPortNum();
        if (TextUtils.isEmpty(port)){
            webAddressEt.setText(url);
        }else {
            webAddressEt.setText(url+":"+port);
        }

        okBtn= (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveConfig();
            }
        });


        failAcceptLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity.this,FailMsgActivity.class);
                startActivity(intent);
            }
        });


        failSendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity.this,FailMsgActivity.class);
                startActivity(intent);
            }
        });

    }



    /**
     * 保存配置
     */
    private void saveConfig(){
        String phoneNo=phoneNoEt.getText().toString().trim();
        String address = webAddressEt.getText().toString().trim();
        String sendNewsUrl=sendNewsUrlEt.getText().toString().trim();
        String sendNewsAgentid=sendNewsAgentidEt.getText().toString().trim();
         String sendNewsType=sendNewsTypeEt.getText().toString().trim();


        if (TextUtils.isEmpty(phoneNo)){
            SimpleDialog.show(this,"号码不能为空！");
            return;
        }
        if (TextUtils.isEmpty(address)){
            SimpleDialog.show(this,"服务器地址不能为空！");
            return;
        }
        if (TextUtils.isEmpty(sendNewsUrl)){
            SimpleDialog.show(this,"消息推送跳转url不能为空！");
            return;
        }
        if (TextUtils.isEmpty(sendNewsAgentid)){
            SimpleDialog.show(this,"消息推送跳转agentid不能为空！");
            return;
        }
        if (TextUtils.isEmpty(sendNewsType)){
            SimpleDialog.show(this,"消息推送跳转类型不能为空！");
            return;
        }

        String ip="";
        String port="";
        if (address.contains(":")){
            //避免输入了“：”，但是没有输入端口号
            if (address.split(":").length==2){
                ip= address.split(":")[0];
                port = address.split(":")[1];
            }else{
                Toast.makeText(this, "你输入的ip地址有误,重新输入!", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            ip= address;
        }
        GlobalState.getInstance().setmIPAddr(ip);
        GlobalState.getInstance().setmPortNum(port);
        String url = "http://" + ip;
        if (!port.equals(""))
        {
            url = url + ":" + port;
        }
        GlobalState.getInstance().setmRequestURL(url);
        GlobalState.getInstance().setPhoneStrs(phoneNo);

        GlobalState.getInstance().setSendNewsUrl(sendNewsUrl);
        GlobalState.getInstance().setSendNewsAgentid(sendNewsAgentid);
        GlobalState.getInstance().setSendNewsType(sendNewsType);

        Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
    }


    UpLoadService.UpLoadBinder upLoadBinder= null;
    private ServiceConnection connection = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //  获得binder实例
            upLoadBinder = (UpLoadService.UpLoadBinder) iBinder;
            //调用getservice方法获取service实例
            UpLoadService upLoadService= upLoadBinder.getService();
            upLoadService.setOnUploadListener(new UpLoadService.OnUploadListener() {
                @Override
                public void onResult() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {



                            total= (int) SharedUtil.getValue(MainActivity.this,SharedUtil.total,0);
                            sucAccept=(int) SharedUtil.getValue(MainActivity.this,SharedUtil.sucAccept,0);
                            sucSend=(int) SharedUtil.getValue(MainActivity.this,SharedUtil.sucSend,0);
                            failAccept=(int) SharedUtil.getValue(MainActivity.this,SharedUtil.failAccept,0);
                            failSend=(int) SharedUtil.getValue(MainActivity.this,SharedUtil.failSend,0);


                            totalTv.setText(total+"");
                            sucAcceptTv.setText(sucAccept+"");
                            sucSendTv.setText(sucSend+"");
                            failAcceptTv.setText(failAccept+"");
                            failSendTv.setText(failSend+"");

                        }
                    });
                }
            });
        }
      /**
         * 当与Service之间的连接丢失的时候会调用该方法，。
         * @param componentName 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.d("");
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        LogUtil.d("");

        getContentResolver().unregisterContentObserver(smsObserver);
    }





}
