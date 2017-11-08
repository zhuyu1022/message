package message.centit.com.message.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import message.centit.com.message.R;
import message.centit.com.message.database.FailMesage;

/**
 * Created by zhuyu on 2017/11/8.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private List<FailMesage> msglist=new ArrayList<>();

    public MsgAdapter(List<FailMesage> msglist) {
        //通过构造函数获取数据源
       // this.msglist.addAll(msglist) ;
        this.msglist=msglist;//这种形式是引用传递，外部list的修改会影响内部list
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       // /将itemview布局文件加载进来
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_layout,parent,false);
        //返回ViewHolder实例
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //得到当前子项的Fruit实例
        FailMesage failMesage=msglist.get(position);

        holder.noTv.setText(failMesage.no);
        holder.timeTv.setText(failMesage.time);
        holder.reasonTv.setText(failMesage.reason);
        holder.typeTv.setText(failMesage.failtype);
        holder.contentTv.setText(failMesage.content);
    }

    @Override
    public int getItemCount() {
        return msglist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{


        private TextView noTv;
        private TextView timeTv;
        private TextView reasonTv;
        private TextView typeTv;
        private TextView contentTv;
        public ViewHolder(final View itemView) {
            super(itemView);
         noTv=(TextView) itemView.findViewById(R.id.item_noTv);;
            timeTv=(TextView) itemView.findViewById(R.id.item_timeTv);;;
            reasonTv=(TextView) itemView.findViewById(R.id.item_reasonTv);;
             typeTv=(TextView) itemView.findViewById(R.id.item_failtypeTv);;
             contentTv=(TextView) itemView.findViewById(R.id.item_contentTv);;

            //第一行代码书中将监听器放在了onCreateViewHolder（）方法中，放在这里也是可以得
            //RecyclerView的一个强大之处就是可以非常方便地实现子项和子项中的控件的点击事件
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
        }
    }

}
