package message.centit.com.message.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import message.centit.com.message.MainActivity;
import message.centit.com.message.R;
import message.centit.com.message.adapter.MsgAdapter;
import message.centit.com.message.database.MyMessage;
import message.centit.com.message.database.MsgDatebaseManager;

public class FailMsgActivity extends AppCompatActivity {

    public static void actionStart(Context context,String type,List<MyMessage> messageList ){
        Intent intent =new Intent(context,FailMsgActivity.class);
        intent.putExtra("type",type);
        intent.putExtra("messageList", (Serializable) messageList);
        context.startActivity(intent);
    }

String type="";

    Toolbar toolbar;
    RecyclerView msgRecyclerView;
    TextView titleTv;
   // private SwipeRefreshLayout swipeRefresh;
private MsgAdapter adapter;
    private List<MyMessage> msglist=new ArrayList<>();
    MsgDatebaseManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail_msg);
         dbManager=new MsgDatebaseManager(this);
        msglist= (List<MyMessage>) getIntent().getSerializableExtra("messageList");
        type=getIntent().getStringExtra("type");
        initView();
    }

    private void initView(){

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        msgRecyclerView= (RecyclerView) findViewById(R.id.recyclerView);
        titleTv= (TextView) findViewById(R.id.title);
       // swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //设置显示返回键
        actionBar.setDisplayHomeAsUpEnabled(true);
        //设置不显示默认标题
        actionBar.setDisplayShowTitleEnabled(false);
        if (MainActivity.TYPE_FAILACCEPT.equals(type)){
                 titleTv.setText("失败接收详情");
            }else if (MainActivity.TYPE_FAILSEND.equals(type)){
                 titleTv.setText("失败发送详情");
                }


        adapter=new MsgAdapter(msglist);
        msgRecyclerView.setAdapter(adapter);
        msgRecyclerView.setLayoutManager(new LinearLayoutManager(this));


     /*   swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                msglist=dbManager.query();
                adapter.notifyDataSetChanged();//刷新数据
                swipeRefresh.setRefreshing(false);
            }
        });*/


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
