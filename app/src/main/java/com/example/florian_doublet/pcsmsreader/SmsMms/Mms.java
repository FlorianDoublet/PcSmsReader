package com.example.florian_doublet.pcsmsreader.SmsMms;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Florian on 31/05/2016.
 */
public class Mms extends SmsMms {

    protected Bitmap image;

    public static final String mimeTextType = "text/plain";
    public static final String[] mimeImageTypes = new String[]{"image/jpeg", "image/bmp", "image/gif", "image/jpg", "image/png"};

    public static boolean hasAnImage(String mimeType){
        for(String type : mimeImageTypes)
            if(type.equals(mimeType)) return true;
        return false;
    }

    public static boolean hasAText(String mimeType){
        return mimeTextType.equals(mimeType);
    }

    public Mms(String body, String address, Long date, Bitmap image){
        this.body = body;
        this.address = address;
        this.date = date;
        this.image = image;
        this.mime_type = this.isMMS;
    }

}
