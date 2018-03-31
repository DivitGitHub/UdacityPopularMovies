package com.android.divit.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Trailer extends BaseModel implements Parcelable {

    private String name;
    private String key;

    public Trailer(String name, String key) {
        this.name = name;
        this.key = key;
    }

    private Trailer(Parcel in) {
        name = in.readString();
        key = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(key);
    }

    public static Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel parcel) {
            return new Trailer(parcel);
        }

        @Override
        public Trailer[] newArray(int i) {
            return new Trailer[i];
        }
    };

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
