package message.centit.com.message.database;

/**
 * Created by zhuyu on 2017/11/8.
 */

public class FailMesage {

    public FailMesage(String no, String time, String content, String reason, String failtype) {
        this.no = no;
        this.time = time;
        this.content = content;
        this.reason = reason;
        this.failtype = failtype;
    }

    public FailMesage() {

    }

    //手机号
    public String no;
    //时间
    public String time;
    //短信内容
    public String content ;
    //失败原因
    public String reason;
    //失败类型  0 失败接收，1 失败发送
    public String failtype;



}
