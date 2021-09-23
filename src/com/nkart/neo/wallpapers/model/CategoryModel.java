package com.nkart.neo.wallpapers.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * TODO: Created by Tanay on 11-08-2015 at 04:30 AM.
 */
public class CategoryModel implements Parcelable {

    private String albumId, albumName, primaryPhoto, countPhoto;

    public CategoryModel() {
    }

    private CategoryModel(Parcel in) {
        albumId = in.readString();
        albumName = in.readString();
        primaryPhoto = in.readString();
        countPhoto = in.readString();
    }

    public static final Creator<CategoryModel> CREATOR = new Creator<CategoryModel>() {
        @Override
        public CategoryModel createFromParcel(Parcel in) {
            return new CategoryModel(in);
        }

        @Override
        public CategoryModel[] newArray(int size) {
            return new CategoryModel[size];
        }
    };

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getCountPhoto() {
        return countPhoto;
    }

    public void setCountPhoto(String countPhoto) {
        this.countPhoto = countPhoto;
    }

    public String getPrimaryPhoto() {
        return primaryPhoto;
    }

    public void setPrimaryPhoto(String primaryPhoto) {
        this.primaryPhoto = primaryPhoto;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(albumId);
        dest.writeString(albumName);
        dest.writeString(primaryPhoto);
        dest.writeString(countPhoto);
    }
}
