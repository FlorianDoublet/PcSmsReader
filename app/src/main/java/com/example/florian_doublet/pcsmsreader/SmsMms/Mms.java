package com.example.florian_doublet.pcsmsreader.SmsMms;

import android.graphics.Bitmap;

/**
 * Created by Florian on 31/05/2016.
 */
public class Mms extends SmsMms {

    protected Bitmap image;

    public Mms(int type, String body, String address, Long date, Bitmap image){
        this.type = type;
        this.body = body;
        this.address = address;
        this.date = date;
        this.image = image;
        this.mime_type = this.isMMS;
    }

}
