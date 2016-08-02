package com.example.florian_doublet.pcsmsreader.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.florian_doublet.pcsmsreader.SmsMms.Mms;
import com.example.florian_doublet.pcsmsreader.SmsMms.Sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 * Created by Florian on 02/06/2016.
 */
public class SmsHelper {


    public static final Uri CONVERSATION_PROVIDER = Uri.parse("content://mms-sms/conversations/");
    public static final Uri SMS_CONTENT_PROVIDER = Uri.parse("content://sms/");
    public static final Uri MMS_CONTENT_PROVIDER = Uri.parse("content://mms/");
    public static final Uri MMS_PART_CONTENT_PROVIDER = Uri.parse("content://mms/part/");
    public static final Uri SMS_INBOX_CONTENT_PROVIDER =  Uri.parse("content://sms/inbox");

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

    public static Sms getSms(Context context, int _id){
        String where = SmsHelper.COLUMN_ID + " = "+ _id;
        Cursor cur = context.getContentResolver().query(SmsHelper.SMS_CONTENT_PROVIDER, null, where, null, null);


        assert cur != null;
        cur.moveToFirst();

        String address = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_ADDRESS));
        int type = cur.getInt(cur.getColumnIndex(SmsHelper.COLUMN_TYPE));
        Long date = Long.parseLong(cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_DATE)));
        String body = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_BODY));

        cur.close();

        return new Sms(type, body, address, date);
    }

    public static Mms getMms(Context context, int _id){
        String where = SmsHelper.COLUMN_MMS_ID + " = "+_id;

        Cursor cur = context.getContentResolver().query(SmsHelper.MMS_PART_CONTENT_PROVIDER, null, where, null, null);

        assert cur != null;
        cur.moveToFirst();

        String partId = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_ID));
        String mimeType = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MIMETYPE));

        String body = null;
        if (Mms.hasAText(mimeType)) {
            body = getMmsText(context, cur, partId);
        }

        Bitmap image = null;
        if (Mms.hasAnImage(mimeType)) {
            image = getMmsImage(context, partId);
        }

        String address = getAddressFromMms(context, _id);


        cur.close();

        where = SmsHelper.COLUMN_ID + " = " + _id;
        cur = context.getContentResolver().query(SmsHelper.MMS_CONTENT_PROVIDER, null, where, null, null);

        assert cur != null;
        cur.moveToFirst();

        Long date = Long.parseLong(cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_DATE)));

        cur.close();

        return new Mms(body, address, date, image);

    }

    public static String getMmsText(Context context, Cursor cur, String partId){
        String data = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MMS_DATA));
        String body;
        if (data != null) {
            //mean that the text is in a data, so we have to extract it
            body = getMmsTextInData(context, partId);
        } else {
            body = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MMS_TEXT));
        }
        return body;
    }

    public static String getMmsTextInData(Context context, String _id) {
        Uri partURI = SmsHelper.getMMSPartProviderWithId(_id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException ignored) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        }
        return sb.toString();
    }

    public static Bitmap getMmsImage(Context context, String _id) {
        Uri partURI = SmsHelper.getMMSPartProviderWithId(_id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException ignored) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return bitmap;
    }

    //TODO when it's a group conversation take the last phone numer despite of the first
    public static String getAddressFromMms(Context context, int _id) {
        String selectionAdd = "msg_id =" + _id;
        String uriStr = MessageFormat.format("content://mms/{0}/addr", _id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = context.getContentResolver().query(uriAddress, null,
                selectionAdd, null, null);
        String address = null;
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        address = number;
                    } catch (NumberFormatException nfe) {
                        if (address == null) {
                            address = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) cAdd.close();
        return address;
    }

    public static boolean isMms(String ct_t){
        return ct_t != null && "application/vnd.wap.multipart.related".equals(ct_t);
    }

}
