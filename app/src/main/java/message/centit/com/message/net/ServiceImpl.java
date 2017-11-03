package message.centit.com.message.net;

import android.os.Handler;

import com.centit.GlobalState;
import com.centit.app.cmipNetHandle.NetRequestController;
import com.centit.core.tools.netUtils.baseEngine.netTask.NetTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhu_yu on 2017/10/11.
 */

public class ServiceImpl {
    public static final int TYPE_AcceptMessage=1;

    public static NetTask acceptMessage(NetTask task, Handler handler, int requestType, String msgBody)
    {   //设置方法名
        GlobalState.getInstance().setmMethodName("/acceptMessage");
          JSONObject requestObj = null;
        try {
            requestObj = new JSONObject(msgBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return NetRequestController.sendStrBaseServletNew(task, handler, requestType, requestObj);
    }









}
