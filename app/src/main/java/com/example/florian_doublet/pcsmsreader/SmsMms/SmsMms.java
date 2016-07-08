package com.example.florian_doublet.pcsmsreader.SmsMms;

/**
 * Created by Florian on 30/05/2016.
 */
public abstract class SmsMms {

    public static int RECEIVED = 1;
    public static int SENT = 2;

    public static int isSMS = 1;
    public static int isMMS = 2;

    protected int mime_type;


    protected int type;
    protected String body;
    protected String address;
    protected Long date;

    public int getType(){
        return this.type;
    }

    public String getBody() {
        return body;
    }

    public String getAddress() {
        return address;
    }

    public Long getDate() {
        return date;
    }
}
