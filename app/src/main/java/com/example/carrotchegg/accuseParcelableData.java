package com.example.carrotchegg;

import android.os.Parcel;
import android.os.Parcelable;

public class accuseParcelableData implements Parcelable{
    String writername;
    String title;
    String subject;
    int Number;
    public accuseParcelableData(String writername,String title,String subject,int Number){
        this.writername = writername;
        this.title = title;
        this.subject = subject;
        this.Number = Number;
    }
    protected accuseParcelableData(Parcel in) {
        writername = in.readString();
        title = in.readString();
        subject = in.readString();
        Number = in.readInt();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(writername);
        dest.writeString(title);
        dest.writeString(subject);
        dest.writeInt(Number);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<accuseParcelableData> CREATOR = new Creator<accuseParcelableData>() {
        @Override
        public accuseParcelableData createFromParcel(Parcel in) {
            return new accuseParcelableData(in);
        }

        @Override
        public accuseParcelableData[] newArray(int size) {
            return new accuseParcelableData[size];
        }
    };
}
