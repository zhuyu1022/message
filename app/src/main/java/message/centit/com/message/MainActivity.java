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
            initView();


    }

    private void initView(){

         phoneNoEt= (EditText) findViewById(R.id.phoneEt);
        webAddressEt= (EditText) findViewById(R.id.webAdressEt);
        String phoneNo= (String) SharedUtil.getValue(this,SharedUtil.phoneNo,"");
        String webAddress= (String) SharedUtil.getValue(this,SharedUtil.webAddress,"");

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
        String webAddress=webAddressEt.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNo)){
            SimpleDialog.show(this,"号码不能为空！");
            return;
        }
        if (TextUtils.isEmpty(webAddress)){
            SimpleDialog.show(this,"服务器地址不能为空！");
            return;
        }
        //保存到本地
        SharedUtil.putValue(this,SharedUtil.phoneNo,phoneNo);
        SharedUtil.putValue(this,SharedUtil.webAddress,webAddress);
        SimpleDialog.show(this,"保存成功！");
    }







    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d("");
    }
}
