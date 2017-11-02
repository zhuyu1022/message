package message.centit.com.message;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuyu on 2017/11/2.
 */

public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder> {

    private List<String > phonelist=new ArrayList<>();

    public PhoneAdapter(List<String> phonelist) {
        //通过构造函数获取数据源
        this.phonelist.addAll(phonelist) ;
        // this.fruitlist=fruitlist;//这种形式是引用传递，外部list的修改会影响内部list
    }

    /**
     * 内部类ViewHolder需要继承自RecyclerView.ViewHolder，
     * 完成item中每个子项的声明与初始化
     * 纠结了半天是定义成static还是public，为了能访问成员变量fruitlist，还是用public来修饰吧
     */
    public  class PhoneViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;
        public PhoneViewHolder(final View itemView) {
            super(itemView);

            textView= (TextView) itemView.findViewById(R.id.item_phonphoneNoTv);
            //第一行代码书中将监听器放在了onCreateViewHolder（）方法中，放在这里也是可以得
            //RecyclerView的一个强大之处就是可以非常方便地实现子项和子项中的控件的点击事件
         
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(itemView.getContext(), "你点击了整个item", Toast.LENGTH_SHORT).show();
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(itemView.getContext(), "你长按了整个item", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }
    /**
     * 用于创建ViewHolder实例
     * @param parent  RecyclerView所在的父视图
     * @param viewType
     * @return
     */
    @Override
    public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //将itemview布局文件加载进来
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phone_layout,parent,false);
        //返回ViewHolder实例
        return new PhoneViewHolder(view);
    }

    /**
     * 用于绑定数据，给每个RecyclerView子项的数据进行赋值
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(PhoneViewHolder holder, int position) {
        //得到当前子项的Fruit实例
        String phoneNo=phonelist.get(position);
        holder.textView.setText(phoneNo);
    }

    /**
     * 告诉RecyclerView一共有多少子项
     * @return
     */
    @Override
    public int getItemCount() {
        return phonelist.size();
    }



    public void additem(int position){
       // phonelist.add(position,new Fruit("new Fruit",R.drawable.apple_pic));
        notifyItemInserted(position);


    }
    public void removeitem(int position){
        phonelist.remove(position);
        notifyItemRemoved(position);
    }
}

