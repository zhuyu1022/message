package message.centit.com.message.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import message.centit.com.message.R;
import message.centit.com.message.util.LogUtil;

/**
 * Created by zhu_yu on 2017/10/30.
 */

public class AddPhoneDialog extends DialogFragment {

    public static final String EXTRA_URL = "url";

    private EditText phoneEt;




    public static AddPhoneDialog newInstance() {
        AddPhoneDialog dialog = new AddPhoneDialog();
//        Bundle bundle = new Bundle();
//        bundle.putString(EXTRA_URL, url);
//        dialog.setArguments(bundle);
        return dialog;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogUtil.d("");
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.add_phone_dialog_layout, null);
        initView(view);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("添加新的监听号码")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false)
                .create();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LogUtil.d("");
        // 点击外部不消失的方法：
        getDialog().setCanceledOnTouchOutside(false);
        // 对于点击返回键不消失，需要监听OnKeyListener:
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("");
    }

    private void initView(View view) {

        phoneEt = view.findViewById(R.id.phoneEt);



    }






}
