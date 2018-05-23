package cn.lt.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ltbl on 2017/6/11.
 */

public class PersisData implements Serializable, Parcelable {
    private long timestamp;

    protected PersisData(Parcel in) {
        timestamp = in.readLong();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static final Creator<PersisData> CREATOR = new Creator<PersisData>() {
        @Override
        public PersisData createFromParcel(Parcel in) {
            return new PersisData(in);
        }

        @Override
        public PersisData[] newArray(int size) {
            return new PersisData[size];
        }
    };

    public PersisData(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PersisData{" +
                "timestamp=" + timestamp +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
    }
}
