package message.centit.com.message.database;

import java.io.Serializable;

/**
 * Created by zhuyu on 2017/11/8.
 */

public class MyMessage  implements Serializable{



    public MyMessage( String number, String time, String body, String reason, String type) {

        this.number = number;
        this.time = time;
        this.body = body;
        this.reason = reason;
        this.type = type;
    }

    public MyMessage() {

    }


    //手机号
    public String number;
    //时间
    public String time;
    //短信内容
    public String body ;
    //失败原因
    public String reason;
    //失败类型  0 失败接收，1 失败发送,2   成功
    public String type;



}
