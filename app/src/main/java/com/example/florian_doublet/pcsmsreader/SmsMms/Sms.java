package com.example.florian_doublet.pcsmsreader.SmsMms;

/**
 * Created by Florian on 30/05/2016.
 */
public class Sms extends SmsMms{


    public Sms(int type, String body, String address, Long date){
        this.type = type;
        this.body = body;
        this.address = address;
        this.date = date;
        this.mime_type = this.isSMS;
    }
}
