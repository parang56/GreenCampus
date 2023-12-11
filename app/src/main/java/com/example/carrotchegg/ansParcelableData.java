package com.example.carrotchegg;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ansParcelableData implements Parcelable {
    String userName;
    String subject;
    int postNumber;
    int ansNum;
    public ansParcelableData(String userName, String subject, int postNumber,int ansNum){
        this.subject = subject;
        this.userName = userName;
        this.postNumber = postNumber;
        this.ansNum = ansNum;
    }
    protected ansParcelableData(Parcel in) {
        userName = in.readString();
        subject = in.readString();
        postNumber = in.readInt();
        ansNum = in.readInt();
    }

    public static final Creator<ansParcelableData> CREATOR = new Creator<ansParcelableData>() {
        @Override
        public ansParcelableData createFromParcel(Parcel in) {
            return new ansParcelableData(in);
        }

        @Override
        public ansParcelableData[] newArray(int size) {
            return new ansParcelableData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(subject);
        dest.writeInt(postNumber);
        dest.writeInt(ansNum);
    }
}
