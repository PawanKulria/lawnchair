package com.nkart.neo.wallpapers.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * TODO Created by Tanay on 05-09-2015 at 12:52 PM.
 */
public class PictureModel implements Parcelable{
    public String url;
    public RowType rowType;
    public String favCount, pageUrl;
    public String picMedium;
    public String picOriginal;
    public String imageID;

    public PictureModel(Parcel in) {
        url = in.readString();
        rowType = in.readParcelable(RowType.class.getClassLoader());
        favCount = in.readString();
        pageUrl = in.readString();
        picMedium = in.readString();
        picOriginal = in.readString();
        imageID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeParcelable(rowType, flags);
        dest.writeString(favCount);
        dest.writeString(pageUrl);
        dest.writeString(picMedium);
        dest.writeString(picOriginal);
        dest.writeString(imageID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PictureModel> CREATOR = new Creator<PictureModel>() {
        @Override
        public PictureModel createFromParcel(Parcel in) {
            return new PictureModel(in);
        }

        @Override
        public PictureModel[] newArray(int size) {
            return new PictureModel[size];
        }
    };
}
