package message.centit.com.message;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centit.GlobalState;
import com.centit.app.cmipConstant.Constant_Mgr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import message.centit.com.message.net.ServiceImpl;
import message.centit.com.message.util.LogUtil;
import message.centit.com.message.util.SharedUtil;
import message.centit.com.message.util.SimpleDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText phoneNoEt;
    private EditText webAddressEt;
    private Button okBtn;
    private LinearLayout statisticLayout;

    private static final String DIALOG_ADD="AddPhoneDialog";
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtil.d("");
            setContentView(R.layout.activity_main);
            initDate();
            initView();
            Intent intent=new Intent(this, UpLoadService.class);
            //绑定服务
            bindService(intent,connection, Context.BIND_AUTO_CREATE);

    }
    private void initDate(){
         //初始化APP配置
         String url= Constant_Mgr.getMIP_BASEURL();
         GlobalState.getInstance().setmRequestURL(url);

    }
    private void initView(){

        statisticLayout= (LinearLayout) findViewById(R.id.statisticLayout);

        phoneNoEt= (EditText) findViewById(R.id.phoneEt);
        webAddressEt= (EditText) findViewById(R.id.webAdressEt);
        String phoneNo= GlobalState.getInstance().getPhoneStrs();
        String webAddress= GlobalState.getInstance().getmIPAddr();

        phoneNoEt.setText(phoneNo);
        webAddressEt.setText(webAddress);

        okBtn= (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveConfig();
            }
        });

        statisticLayout.setOnClickListener(new View.OnClickListener() {
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
        String ip = webAddressEt.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNo)){
            SimpleDialog.show(this,"号码不能为空！");
            return;
        }
        if (TextUtils.isEmpty(ip)){
            SimpleDialog.show(this,"服务器地址不能为空！");
            return;
        }
        String port = "";
        GlobalState.getInstance().setmIPAddr(ip);
        GlobalState.getInstance().setmPortNum(port);
        String url = "http://" + ip;
        if (!port.equals(""))
        {
            url = url + ":" + port;
        }
        //保存到本地
        GlobalState.getInstance().setmRequestURL(url);
        GlobalState.getInstance().setPhoneStrs(phoneNo);
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
            upLoadService.setOnUploadSuccess(new UpLoadService.OnUploadSuccessListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "上传短信成功！", Toast.LENGTH_LONG).show();
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
    }



}
