package message.centit.com.message;

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
import android.widget.TextView;
import android.widget.Toast;

import com.centit.GlobalState;
import com.centit.app.cmipConstant.Constant_Mgr;

import java.util.ArrayList;
import java.util.List;

import message.centit.com.message.util.LogUtil;
import message.centit.com.message.util.SharedUtil;
import message.centit.com.message.util.SimpleDialog;

public class MainActivity extends AppCompatActivity {
    private EditText phoneNoEt;
    private EditText webAddressEt;
    private Button okBtn;

    private static final String DIALOG_ADD="AddPhoneDialog";
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtil.d("");
            setContentView(R.layout.activity_main);
            initDate();
            initView();


    }
    private void initDate(){
         //初始化APP配置
         String url= Constant_Mgr.getMIP_BASEURL();
         GlobalState.getInstance().setmRequestURL(url);

    }
    private void initView(){

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







    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d("");
    }
}
