package message.centit.com.message;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.centit.GlobalState;
import com.centit.app.cmipConstant.Constant_Mgr;
import com.centit.core.baseView.baseUI.MIPBaseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import message.centit.com.message.net.ServiceImpl;
import message.centit.com.message.util.LogUtil;
import message.centit.com.message.util.SharedUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpLoadService extends MIPBaseService {
    public static final String EXTRA_TIME = "receiveTime";
    public static final String EXTRA_MSG = "msgBody";
    public static final String EXTRA_SENDER = "sender";


    String phoneNoStr;
    String webAddress;
    private String[] phoneList;

    /**
     * 封装供外部启动服务
     *
     * @param context
     * @param receiveTime
     * @param msgBody
     * @param sender
     */
    public static void actionStart(Context context, String receiveTime, String msgBody, String sender) {
        Intent intent = new Intent(context, UpLoadService.class);
        intent.putExtra(EXTRA_TIME, receiveTime);
        intent.putExtra(EXTRA_MSG, msgBody);
        intent.putExtra(EXTRA_SENDER, sender);
        context.startService(intent);
    }

    public UpLoadService() {
    }


    UpLoadBinder mBinder=new UpLoadBinder();
    //用于获取service实例
    public class UpLoadBinder extends Binder {
        public UpLoadService getService() {
            return UpLoadService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
       return mBinder;
    }

    @Override
    public void onCreate() {
        LogUtil.d("");
        super.onCreate();
        startForeground();


    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent!=null){
            String receiveTime = intent.getStringExtra(EXTRA_TIME);
            String msgBody = intent.getStringExtra(EXTRA_MSG);
            String sender = intent.getStringExtra(EXTRA_SENDER);

            phoneNoStr = GlobalState.getInstance().getPhoneStrs();
            webAddress = GlobalState.getInstance().getmIPAddr();
            if (!TextUtils.isEmpty(phoneNoStr) && !TextUtils.isEmpty(webAddress)) {
                if (phoneNoStr.contains(",")) {
                    phoneList = phoneNoStr.split(",");
                } else {
                    phoneList = new String[]{phoneNoStr};

                }
            }

            for (int i = 0; i < phoneList.length; i++) {      //如果是其中的一个号码，就上传服务器
                if (sender.equals(phoneList[i].trim())) {
                   // Toast.makeText(UpLoadService.this, msgBody, Toast.LENGTH_LONG).show();
                    uploadMessage(msgBody);
                    // downloadConfParams();
                    break;
                }
            }
            LogUtil.d("uploadservice服务启动");
            LogUtil.d("receiveTime:" + receiveTime + "msgBody:" + msgBody + "sender:" + sender);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    //将service变为前台进程
    private void startForeground() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("短信监听")
                .setContentText("正在运行，请勿关闭")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
    }

    /**
     * 上传短信内容
     *
     * @param msgBody
     */
    private void uploadMessage(String msgBody) {


       /* {
            "messageid": "消息id",
                "content": "消息内容",
                "lng": "经度",
                "lat": "纬度",
                "useridList": [{
            "userid": "钉钉人员id",
                    "username": "钉钉人员名称"
        }]
        }*/
        // ServiceImpl.acceptMessage(null,mHandler,ServiceImpl.TYPE_AcceptMessage,msgBody);
        ServiceImpl.acceptMessage(ServiceImpl.TYPE_AcceptMessage, msgBody, callback);
    }

    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {


            String result = response.body().string();
            Log.d("result", result);
            try {

                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj != null) {
                    String retCode = jsonObj.optString("retCode");
                    if (retCode != null && retCode.equals("0")) {

                             if (listener!=null){
                                 listener.onSuccess();
                             }

                        return;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
    OnUploadSuccessListener listener;
public interface  OnUploadSuccessListener{
        void onSuccess();
    }

    public void setOnUploadSuccess(OnUploadSuccessListener listener){
         this. listener=listener;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPostHandle(int requestType, Object objHeader, Object objBody, boolean error, int errorCode) {
        if (error) {
            switch (requestType) {
                case ServiceImpl.TYPE_AcceptMessage:

                    if (objBody != null && objBody instanceof String) {
                        try {
                            Log.d("返回的数据为：", objBody.toString());
                            JSONObject jsonObj = new JSONObject((String) objBody);
                            if (jsonObj != null) {
                                String retCode = jsonObj.optString("retCode");

                                if (retCode != null && retCode.equals("0")) {

                                    Toast.makeText(this, "上传成功！", Toast.LENGTH_SHORT).show();

                                    return;
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;

            }
        } else {
            switch (requestType) {

            }
        }
    }
}
