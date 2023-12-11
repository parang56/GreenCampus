package com.example.carrotchegg;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class questionParcelableData implements Parcelable {
    int documentNum;
    String subjectName;

    public questionParcelableData(String subjectName,int documentNum){
        this.documentNum = documentNum;
        this.subjectName = subjectName;
    }

    protected questionParcelableData(Parcel in) {
        documentNum = in.readInt();
        subjectName = in.readString();
    }

    public static final Creator<questionParcelableData> CREATOR = new Creator<questionParcelableData>() {
        @Override
        public questionParcelableData createFromParcel(Parcel in) {
            return new questionParcelableData(in);
        }

        @Override
        public questionParcelableData[] newArray(int size) {
            return new questionParcelableData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(documentNum);
        dest.writeString(subjectName);
    }
}
