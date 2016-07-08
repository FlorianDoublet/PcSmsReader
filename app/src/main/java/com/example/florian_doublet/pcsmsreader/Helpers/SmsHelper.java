package com.example.florian_doublet.pcsmsreader.Helpers;

import android.net.Uri;

/**
 * Created by Florian on 02/06/2016.
 */
public class SmsHelper {


    public static final Uri CONVERSATION_PROVIDER = Uri.parse("content://mms-sms/conversations/");
    public static final Uri SMS_CONTENT_PROVIDER = Uri.parse("content://sms/");
    public static final Uri MMS_CONTENT_PROVIDER = Uri.parse("content://mms/");
    public static final Uri MMS_PART_CONTENT_PROVIDER = Uri.parse("content://mms/part/");

    public static Uri getConversationProviderWithThreadId(int thread_id){
        return Uri.parse("content://mms-sms/conversations/" + thread_id);
    }

    public static Uri getMMSPartProviderWithId(String _id){
        return Uri.parse("content://mms/part/" + _id);
    }


    //Usefull column for SMS or MMS

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_THREAD_ID = "thread_id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DATE_NORMALIZED = "normalized_date";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_MMS = "ct_t";
    public static final String COLUMN_MMS_ID = "mid";
    public static final String COLUMN_MIMETYPE = "ct";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_MMS_DATA = "_data";
    public static final String COLUMN_MMS_TEXT = "text";

}
