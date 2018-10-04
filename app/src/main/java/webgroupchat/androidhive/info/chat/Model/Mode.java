package webgroupchat.androidhive.info.chat.Model;

import java.util.Date;

public class Mode {
    private String fromName, message;
    private int isSelf;
    private Date mDate;

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Mode() {
    }

    public Mode(String fromName, String message, int isSelf , Date mDate) {
        this.fromName = fromName;
        this.message = message;
        this.isSelf = isSelf;
        this.mDate = mDate;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int isSelf() {
        return isSelf;
    }

    public void setSelf(int isSelf) {
        this.isSelf = isSelf;
    }

}
