package message.centit.com.message.database;

import java.io.Serializable;

/**
 * Created by zhuyu on 2017/11/13.
 */

public class MessageStatistics implements Serializable {


    public MessageStatistics(String number, int total, int sucAccept, int sucSend, int failAccept, int failSend) {
        this.number = number;
        this.total = total;
        this.sucAccept = sucAccept;
        this.sucSend = sucSend;
        this.failAccept = failAccept;
        this.failSend = failSend;
    }

    public MessageStatistics() {

    }

    public String number;
    public int total;
    public int sucAccept;
    public int sucSend;
    public int failAccept;
    public int failSend;


}
