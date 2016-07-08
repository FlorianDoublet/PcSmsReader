package com.example.florian_doublet.pcsmsreader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.example.florian_doublet.pcsmsreader.Helpers.SmsHelper;
import com.example.florian_doublet.pcsmsreader.SmsMms.Mms;
import com.example.florian_doublet.pcsmsreader.SmsMms.Sms;
import com.example.florian_doublet.pcsmsreader.SmsMms.SmsMms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static List<Contact> conversationContacts = new ArrayList<>();
    private int MESSAGE_LIMIT = 200;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadAllContacts();
        //TODO take a long time, need to see why
        for(Contact contact : conversationContacts){
            contact.setConversation(getAllMessageFromConversation(contact.getThread_id()));
        }
    }

    /**
     * load contacts into the static List "conversationContacts"
     * contacts are added form the newest conversation to the oldest
     */
    public void loadAllContacts(){

        //all the fields we want
        final String[] projection = new String[]{SmsHelper.COLUMN_ID, SmsHelper.COLUMN_MMS, SmsHelper.COLUMN_THREAD_ID, SmsHelper.COLUMN_ADDRESS};
        final String sortBy = SmsHelper.COLUMN_DATE_NORMALIZED;
        Cursor cur = getContentResolver().query(SmsHelper.CONVERSATION_PROVIDER, projection, null, null, sortBy);


        assert cur != null;
        while(cur.moveToNext()){
            Integer thread_id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_THREAD_ID));
            String address;

            String ct_t = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MMS));
            int _id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));

            if(ct_t != null) {
                //it's a MMS
                address = getAddressFromMms(_id);
            } else{
                //it's a SMS
               address = cur.getString(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_ADDRESS));
            }

            String name = getContactNameByAddress(address);
            if(name == null){
                name = address;
            }
            conversationContacts.add(new Contact(thread_id, name, address));
        }

        cur.close();
    }

    public String getContactNameByAddress(String address){
        String name = null;

        Uri uriContact = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        final String where = ContactsContract.CommonDataKinds.Phone.NUMBER+"='"+address+"'";
        Cursor cur = getContentResolver().query(uriContact,null,where,null,null);
        assert cur != null;
        while(cur.moveToNext()) {
            name = cur.getString(cur.getColumnIndexOrThrow("DISPLAY_NAME"));
        }
        cur.close();
        return name;
    }

    public List<SmsMms>  getAllMessageFromConversation(int thread_id){
        List<SmsMms> messages = new ArrayList<>();
        Uri uriConversation = SmsHelper.getConversationProviderWithThreadId(thread_id);
        final String[] projection = new String[]{SmsHelper.COLUMN_ID, SmsHelper.COLUMN_MMS};
        final String sortBy = SmsHelper.COLUMN_DATE_NORMALIZED;
        Cursor cur = getContentResolver().query(uriConversation, projection, null, null, sortBy + " desc limit " + MESSAGE_LIMIT);
        //will go through all messages for this conversation thread
        while(cur.moveToNext()) {
            String ct_t = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MMS));
            int _id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));

            if(isMms(ct_t)) {
                messages.add(getMms(_id));
            }else{
                messages.add(getSms(_id));
            }

        }
        cur.close();

        return messages;
    }

    public boolean isMms(String ct_t){
        return ct_t != null && "application/vnd.wap.multipart.related".equals(ct_t);
    }

    public Sms getSms(int _id){
        String where = SmsHelper.COLUMN_ID + " = "+ _id;
        Cursor cur = getContentResolver().query(SmsHelper.SMS_CONTENT_PROVIDER, null, where, null, null);


        assert cur != null;
        cur.moveToFirst();

        String address = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_ADDRESS));
        int type = cur.getInt(cur.getColumnIndex(SmsHelper.COLUMN_TYPE));
        Long date = Long.parseLong(cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_DATE)));
        String body = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_BODY));

        cur.close();

        return new Sms(type, body, address, date);
    }


    public Mms getMms(int _id){
        String where = SmsHelper.COLUMN_MMS_ID + " = "+_id;

        Cursor cur = getContentResolver().query(SmsHelper.MMS_PART_CONTENT_PROVIDER, null, where, null, null);

        assert cur != null;
        cur.moveToFirst();

        String partId = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_ID));
        String mimeType = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MIMETYPE));

        String body = null;
        if (Mms.hasAText(mimeType)) {
            body = getMmsText(cur, partId);
        }

        Bitmap image = null;
        if (Mms.hasAnImage(mimeType)) {
            image = getMmsImage(partId);
        }

        String address = getAddressFromMms(_id);


        cur.close();

        where = SmsHelper.COLUMN_ID + " = " + _id;
        cur = getContentResolver().query(SmsHelper.MMS_CONTENT_PROVIDER, null, where, null, null);

        assert cur != null;
        cur.moveToFirst();

        Long date = Long.parseLong(cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_DATE)));

        cur.close();

        return new Mms(body, address, date, image);

    }

    public String getMmsText(Cursor cur, String partId){
        String data = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MMS_DATA));
        String body;
        if (data != null) {
            //mean that the text is in a data, so we have to extract it
            body = getMmsTextInData(partId);
        } else {
            body = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MMS_TEXT));
        }
        return body;
    }

    public String getMmsTextInData(String _id) {
        Uri partURI = SmsHelper.getMMSPartProviderWithId(_id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = getContentResolver().openInputStream(partURI);
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

    public Bitmap getMmsImage(String _id) {
        Uri partURI = SmsHelper.getMMSPartProviderWithId(_id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = getContentResolver().openInputStream(partURI);
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
    private String getAddressFromMms(int _id) {
        String selectionAdd = "msg_id =" + _id;
        String uriStr = MessageFormat.format("content://mms/{0}/addr", _id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = getContentResolver().query(uriAddress, null,
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

}
