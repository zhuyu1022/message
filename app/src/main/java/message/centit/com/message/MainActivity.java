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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import message.centit.com.message.activity.FailMsgActivity;
import message.centit.com.message.database.MessageStatistics;
import message.centit.com.message.database.MsgDatebaseManager;
import message.centit.com.message.database.MyMessage;
import message.centit.com.message.service.UpLoadService;
import message.centit.com.message.util.LogUtil;
import message.centit.com.message.util.SharedUtil;
import message.centit.com.message.util.SimpleDialog;

public class MainActivity extends AppCompatActivity {

    /**
     * 失败类型    1  失败发送
     **/
    public static final String TYPE_FAILSEND = "0";
    /**
     * 失败类型    0   失败接收
     **/
    public static final String TYPE_FAILACCEPT = "1";
    /***   成功  */
    public static final String TYPE_SUCCESS = "2";


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

    private MsgDatebaseManager dbManager;
    SmsObserver smsObserver;
    //收件箱uri，如果不写inbox 会调用3次监听事件，
    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");
    //存储本地未上传过的短信
    List<MyMessage> messageList = new ArrayList<>();
    int count = 0;
    //查询时间，默认为保存按钮的触发时间，数据库中有数据时，是最后一条记录的时间
    //Date queryDate=new Date();
    private static final String DIALOG_ADD = "AddPhoneDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("");
        setContentView(R.layout.activity_main);
        smsObserver = new SmsObserver(this, smsHandler);
        dbManager = new MsgDatebaseManager(this);
        initDate();
        initView();
        Intent intent = new Intent(this, UpLoadService.class);
        //绑定服务
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // registerObserver();
        restoreMsg();

    }

    /**
     * 注册
     */
/*    private void registerObserver(){
            smsObserver = new SmsObserver(this, smsHandler);
            getContentResolver().registerContentObserver(SMS_INBOX, true,
                    smsObserver);
        }*/

    /**
     * 根据输入的号码遍历短信数据，并上传
     */
    private void restoreMsg() {
        messageList.clear();
        count=0;
        LogUtil.d("开始检查本地短信数据");
        String phoneNo = GlobalState.getInstance().getPhoneStrs();
        List<String> phonrList = getPhoneListfromStrWith86(phoneNo);
        for (int i = 0; i < phonrList.size(); i++) {
            //查询本地数据库中改短信的最后一次记录时间
            String time = dbManager.querylastTime(phonrList.get(i));
            LogUtil.d("time" + time);
            Date date=null;
            try {
                 date = df.parse(time);
                LogUtil.d("解析后的日期为" + date);
            } catch (ParseException e) {
                e.printStackTrace();
                LogUtil.d("解析出错，时间按0进行查询，返回所有短信" );
            }
            long querytime =0;
            if (date!=null){
                querytime=date.getTime();
            }
            //需要传一个long型的时间进去，切记
            List<MyMessage> tempMessageList = smsObserver.getSmsByTime(this, phonrList.get(i),querytime );
            LogUtil.d("查到数据：" + tempMessageList.size());
            messageList.addAll(tempMessageList);
        }
        LogUtil.d("共查到数据：" + messageList.size());
        //不要在for循环中上传，在接口回调中进行上传，这里这上传第一条
        if (count < messageList.size()) {
            MyMessage message = messageList.get(count);;
            UpLoadService.actionStart(MainActivity.this, message);
            count++;
        }
    /*    for (int i = 0; i <messageList.size() ; i++) {
            MyMessage message = messageList.get(i);
            UpLoadService.actionStart(MainActivity.this, message);
        }*/



    }


    // Message 类型值
    public static final int MSG_AIRPLANE = 1;
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


    private void initDate() {
        //初始化APP配置
        String url = Constant_Mgr.getMIP_BASEURL();
        GlobalState.getInstance().setmRequestURL(url);

        sendNewsUrl = GlobalState.getInstance().getSendNewsUrl();
        sendNewsAgentid = GlobalState.getInstance().getSendNewsAgentid();
        sendNewsType = GlobalState.getInstance().getSendNewsType();


    }

    private void initView() {

        statisticLayout = (LinearLayout) findViewById(R.id.statisticLayout);

        failAcceptLayout = (LinearLayout) findViewById(R.id.failAcceptLl);
        failSendLayout = (LinearLayout) findViewById(R.id.failSendLl);

        phoneNoEt = (EditText) findViewById(R.id.phoneEt);
        webAddressEt = (EditText) findViewById(R.id.webAdressEt);

        sendNewsUrlEt = (EditText) findViewById(R.id.sendNewsUrlEt);
        sendNewsAgentidEt = (EditText) findViewById(R.id.sendNewsAgentidEt);
        sendNewsTypeEt = (EditText) findViewById(R.id.sendNewsTypeEt);


        totalTv = (TextView) findViewById(R.id.totalTv);
        sucAcceptTv = (TextView) findViewById(R.id.sucAcceptTv);
        sucSendTv = (TextView) findViewById(R.id.sucSendTv);
        failAcceptTv = (TextView) findViewById(R.id.failAcceptTv);
        failSendTv = (TextView) findViewById(R.id.failSendTv);

        sendNewsUrlEt.setText(sendNewsUrl);
        sendNewsAgentidEt.setText(sendNewsAgentid);
        sendNewsTypeEt.setText(sendNewsType);

    /*    totalTv.setText(total+"");
        sucAcceptTv.setText(sucAccept+"");
        sucSendTv.setText(sucSend+"");
        failAcceptTv.setText(failAccept+"");
        failSendTv.setText(failSend+"");*/

        readStatisticsFromDB();

        String phoneNo = GlobalState.getInstance().getPhoneStrs();
        phoneNoEt.setText(phoneNo);


        String url = GlobalState.getInstance().getmIPAddr();
        String port = GlobalState.getInstance().getmPortNum();
        if (TextUtils.isEmpty(port)) {
            webAddressEt.setText(url);
        } else {
            webAddressEt.setText(url + ":" + port);
        }

        okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveConfig();
            }
        });


        failAcceptLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNoStr = GlobalState.getInstance().getPhoneStrs();
                List<String> phonrList = getPhoneListfromStrWith86(phoneNoStr);
                List<MyMessage> messageList = new ArrayList<MyMessage>();
                for (int i = 0; i < phonrList.size(); i++) {
                    messageList.addAll(dbManager.queryFailMsg(phonrList.get(i), TYPE_FAILACCEPT));
                }
                FailMsgActivity.actionStart(MainActivity.this, TYPE_FAILACCEPT, messageList);
            }
        });


        failSendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNoStr = GlobalState.getInstance().getPhoneStrs();
                List<String> phonrList = getPhoneListfromStrWith86(phoneNoStr);
                List<MyMessage> messageList = new ArrayList<MyMessage>();
                for (int i = 0; i < phonrList.size(); i++) {
                    messageList.addAll(dbManager.queryFailMsg(phonrList.get(i), TYPE_FAILSEND));
                }
                FailMsgActivity.actionStart(MainActivity.this, TYPE_FAILSEND, messageList);
            }
        });

    }


    /**
     * 保存配置
     */
    private void saveConfig() {
        //记录保存的触发时间
      //  queryDate = new Date();

        String phoneNo = phoneNoEt.getText().toString().trim();
        String address = webAddressEt.getText().toString().trim();
        String sendNewsUrl = sendNewsUrlEt.getText().toString().trim();
        String sendNewsAgentid = sendNewsAgentidEt.getText().toString().trim();
        String sendNewsType = sendNewsTypeEt.getText().toString().trim();


        if (TextUtils.isEmpty(phoneNo)) {
            SimpleDialog.show(this, "号码不能为空！");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            SimpleDialog.show(this, "服务器地址不能为空！");
            return;
        }
        if (TextUtils.isEmpty(sendNewsUrl)) {
            SimpleDialog.show(this, "消息推送跳转url不能为空！");
            return;
        }
        if (TextUtils.isEmpty(sendNewsAgentid)) {
            SimpleDialog.show(this, "消息推送跳转agentid不能为空！");
            return;
        }
        if (TextUtils.isEmpty(sendNewsType)) {
            SimpleDialog.show(this, "消息推送跳转类型不能为空！");
            return;
        }

        String ip = "";
        String port = "";
        if (address.contains(":")) {
            //避免输入了“：”，但是没有输入端口号
            if (address.split(":").length == 2) {
                ip = address.split(":")[0];
                port = address.split(":")[1];
            } else {
                Toast.makeText(this, "你输入的ip地址有误,重新输入!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            ip = address;
        }
        GlobalState.getInstance().setmIPAddr(ip);
        GlobalState.getInstance().setmPortNum(port);
        String url = "http://" + ip;
        if (!port.equals("")) {
            url = url + ":" + port;
        }
        GlobalState.getInstance().setmRequestURL(url);
        GlobalState.getInstance().setPhoneStrs(phoneNo);

        GlobalState.getInstance().setSendNewsUrl(sendNewsUrl);
        GlobalState.getInstance().setSendNewsAgentid(sendNewsAgentid);
        GlobalState.getInstance().setSendNewsType(sendNewsType);
        /***************************************************************************/
        //根据phonestr 在数据库中创建数据
        List<String> phonrList = getPhoneListfromStrWith86(phoneNo);
        for (int i = 0; i < phonrList.size(); i++) {
            MessageStatistics msgStatistics = new MessageStatistics(phonrList.get(i), 0, 0, 0, 0, 0);
            dbManager.addStatistics(msgStatistics);
        }
        Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
        //保存完后，重新刷新界面
        readStatisticsFromDB();
        //读取新号码对应的历史数据，并上传
        restoreMsg();

    }


    UpLoadService.UpLoadBinder upLoadBinder = null;
    private ServiceConnection connection = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //  获得binder实例
            upLoadBinder = (UpLoadService.UpLoadBinder) iBinder;
            //调用getservice方法获取service实例
            UpLoadService upLoadService = upLoadBinder.getService();


            upLoadService.setOnUploadListener(new UpLoadService.OnUploadListener() {
                @Override
                public void onTotal() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            readStatisticsFromDB();

                        }
                    });

                }

                @Override
                public void onSucAccept() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "开始上传...", Toast.LENGTH_SHORT).show();
                            readStatisticsFromDB();

                        }
                    });


                }

                @Override
                public void onSucSend() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
                           readStatisticsFromDB();
                            readNextMsg();
                        }
                    });

                }

                @Override
                public void onFailAccept() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readStatisticsFromDB();
                            readNextMsg();
                        }
                    });

                }

                @Override
                public void onFailSend() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readStatisticsFromDB();
                            readNextMsg();
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


    private void readNextMsg(){

        if (count < messageList.size()) {
            MyMessage message = messageList.get(count);
            // UpLoadService.actionStart(MainActivity.this, message.time, message.body, message.number);
            UpLoadService.actionStart(MainActivity.this, message);
            count++;
        }
    }


    /**
     * 从数据库中读取短信统计
     */
    private void readStatisticsFromDB() {
        int total = 0;
        int sucAccept = 0;
        int sucSend = 0;
        int failAccept = 0;
        int failSend = 0;
        String phoneNoStr = GlobalState.getInstance().getPhoneStrs();
        List<String> phonrList = getPhoneListfromStrWith86(phoneNoStr);
        for (int i = 0; i < phonrList.size(); i++) {
            MessageStatistics msgStatistics = dbManager.queryMsgStatistics(phonrList.get(i));
            if (msgStatistics != null) {
                total += msgStatistics.total;
                sucAccept += msgStatistics.sucAccept;
                sucSend += msgStatistics.sucSend;
                failAccept += msgStatistics.failAccept;
                failSend += msgStatistics.failSend;
            }

        }
        totalTv.setText(total + "");
        sucAcceptTv.setText(sucAccept + "");
        sucSendTv.setText(sucSend + "");
        failAcceptTv.setText(failAccept + "");
        failSendTv.setText(failSend + "");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        LogUtil.d("");

        // getContentResolver().unregisterContentObserver(smsObserver);
    }


    /**
     * 根据字符串解析号码，并增加+86号码
     *
     * @param phones
     * @return
     */
    private List<String> getPhoneListfromStrWith86(String phones) {

        List<String> phoneList = new ArrayList<>();

        if (!TextUtils.isEmpty(phones)) {
            if (phones.contains(",")) {
                String[] tempphones = phones.split(",");
                for (int i = 0; i < tempphones.length; i++) {
                    phoneList.add(tempphones[i]);
                    if (!tempphones[i].contains("+86")) {
                        phoneList.add("+86" + tempphones[i]);
                    }

                }
            } else {

                phoneList.add(phones);
                if (!phones.contains("+86")) {
                    phoneList.add("+86" + phones);
                }

            }
        }

        return phoneList;
    }

    private List<String> getPhoneListfromStr(String phones) {

        List<String> phoneList = new ArrayList<>();

        if (!TextUtils.isEmpty(phones)) {
            if (phones.contains(",")) {
                String[] tempphones = phones.split(",");
                for (int i = 0; i < tempphones.length; i++) {
                    phoneList.add(tempphones[i]);
                }
            } else {

                phoneList.add(phones);

            }
        }

        return phoneList;
    }


    private void showProgressDialog() {

    }

}
