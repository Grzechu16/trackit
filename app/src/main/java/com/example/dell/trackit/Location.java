package com.example.dell.trackit;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Location implements Parcelable {

    private double longitude = 0;
    private double latitude = 0;
    private int gpsStatus = 0;
    private String city = "";
    private String state = "";
    private String zip = "";
    private String country = "";
    private String knownName = "";
    private String address = "";
    private String updateTime = "";
    private String queryTime = "";

    public Location() {
    }

    public Location(double longitude, double latitude, int gpsStatus, String city, String state, String zip, String country, String knownName, String address, String updateTime, String queryTime) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.gpsStatus = gpsStatus;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.knownName = knownName;
        this.address = address;
        this.updateTime = updateTime;
        this.queryTime = queryTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getKnownName() {
        return knownName;
    }

    public void setKnownName(String knownName) {
        this.knownName = knownName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(String queryTime) {
        this.queryTime = queryTime;
    }

    public int getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(int gpsStatus) {
        this.gpsStatus = gpsStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeInt(gpsStatus);
        parcel.writeString(updateTime);
        parcel.writeString(queryTime);
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public Location(Parcel in) {
        longitude = in.readDouble();
        latitude = in.readDouble();
        gpsStatus = in.readInt();
        updateTime = in.readString();
        queryTime = in.readString();
    }

}