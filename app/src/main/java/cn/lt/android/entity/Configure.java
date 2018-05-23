package cn.lt.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ltbl on 2016/9/1.
 */
public class Configure implements Serializable, Parcelable {
    private String status; //按钮状态
    private Long time;     //弹框频率

    public Configure(String status) {
        this.status = status;
    }

    protected Configure(Parcel in) {
        status = in.readString();
        if (in.readByte() == 0) {
            time = null;
        } else {
            time = in.readLong();
        }
    }

    public static final Creator<Configure> CREATOR = new Creator<Configure>() {
        @Override
        public Configure createFromParcel(Parcel in) {
            return new Configure(in);
        }

        @Override
        public Configure[] newArray(int size) {
            return new Configure[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        if (time == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(time);
        }
    }
}
