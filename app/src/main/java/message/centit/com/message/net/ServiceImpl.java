package message.centit.com.message.net;

import android.os.Handler;
import android.telecom.Call;

import com.centit.GlobalState;
import com.centit.app.cmipConstant.Constant_Mgr;
import com.centit.app.cmipNetHandle.NetRequestController;
import com.centit.core.tools.netUtils.baseEngine.netTask.NetTask;

import org.json.JSONException;
import org.json.JSONObject;

import message.centit.com.message.util.LogUtil;
import okhttp3.Callback;

/**
 * Created by zhu_yu on 2017/10/11.
 */

public class ServiceImpl {



    public static void  acceptMessage(int requestType, String msgBody, Callback callback)
    {   GlobalState.getInstance().setmMethodName("/acceptMessage");
        JSONObject  requestObj=null;
        try {
             requestObj = new JSONObject(msgBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.getInstance();
        OkHttpUtil.post(Constant_Mgr.getMIP_BAES_URL(),requestObj,callback);
    }





    public static final int TYPE_AcceptMessage=1;

    public static NetTask acceptMessage(NetTask task, Handler handler, int requestType, String msgBody)
    {   //设置方法名
        GlobalState.getInstance().setmMethodName("/acceptMessage");
        JSONObject  requestObj = new JSONObject();
        try {
//
         requestObj.put("messageid","122345发挥发货就");
            requestObj.put("lng","lng");
//           //String messageid=requestObj.optString("messageid");
//         //String lng=requestObj.optString("lng");
//         //LogUtil.d(messageid+","+lng);
        } catch (JSONException e) {
          e.printStackTrace();
//            //如果转换json遇到异常泽不进行上传
//            //return null;
       }
        return NetRequestController.sendStrBaseServletNew(task, handler, requestType, requestObj);
    }







}
