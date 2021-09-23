package com.nkart.neo.wallpapers.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * TODO: Created by Tanay Mondal on 21-09-2016
 */
public enum RowType implements Parcelable {
    NORMAL,
    ADS;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RowType> CREATOR = new Creator<RowType>() {
        @Override
        public RowType createFromParcel(Parcel in) {
            return RowType.values()[in.readInt()];
        }

        @Override
        public RowType[] newArray(int size) {
            return new RowType[size];
        }
    };
}
