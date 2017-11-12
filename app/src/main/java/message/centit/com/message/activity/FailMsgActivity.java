package message.centit.com.message.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import message.centit.com.message.R;
import message.centit.com.message.adapter.MsgAdapter;
import message.centit.com.message.database.MyMessage;
import message.centit.com.message.database.MsgDatebaseManager;

public class FailMsgActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView msgRecyclerView;

   // private SwipeRefreshLayout swipeRefresh;
private MsgAdapter adapter;
    private List<MyMessage> msglist;
    MsgDatebaseManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail_msg);
         dbManager=new MsgDatebaseManager(this);
        initView();
    }

    private void initView(){

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        msgRecyclerView= (RecyclerView) findViewById(R.id.recyclerView);
       // swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //设置显示返回键
        actionBar.setDisplayHomeAsUpEnabled(true);
        //设置不显示默认标题
        actionBar.setDisplayShowTitleEnabled(false);
        //msglist=new ArrayList<>();
     //   msglist=dbManager.query();
        msglist=dbManager.queryFailMsg();

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
