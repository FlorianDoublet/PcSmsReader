package com.example.florian_doublet.pcsmsreader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.example.florian_doublet.pcsmsreader.SmsMms.Mms;
import com.example.florian_doublet.pcsmsreader.SmsMms.Sms;
import com.example.florian_doublet.pcsmsreader.SmsMms.SmsMms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static List<Contact> conversationContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadAllContacts();
        //TODO take a long time, need to see why
        for(Contact contact : conversationContacts){
            getAllMessageFromConversation(contact.getThread_id());
        }
    }

    /**
     * load contacts into the static List "conversationContacts"
     * contacts are added form the newest conversation to the oldest
     */
    public void loadAllContacts(){

        Uri uriConversations = Uri.parse("content://mms-sms/conversations");

        //all the fields we want
        final String[] projection = new String[]{"_id", "ct_t", "thread_id", "address"};
        final String sortBy = "normalized_date desc";
        Cursor cur = getContentResolver().query(uriConversations, projection, null, null, sortBy);


        assert cur != null;
        while(cur.moveToNext()){
            Integer thread_id = cur.getInt(cur.getColumnIndexOrThrow("thread_id"));
            String address = null;

            String ct_t = cur.getString(cur.getColumnIndex("ct_t"));
            int _id = cur.getInt(cur.getColumnIndexOrThrow("_id"));

            if(ct_t != null) {
                //it's a MMS
                address = getAddressFromMms(_id);
            } else{
                //it's a SMS
               address = cur.getString(cur.getColumnIndexOrThrow("address"));
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

    public List<SmsMms>  getAllMessageFromConversation(int conv_id){
        List<SmsMms> messages = new ArrayList<>();
        Uri uriConversation = Uri.parse("content://mms-sms/conversations/" + conv_id);
        final String[] projection = new String[]{"_id", "ct_t"};
        final String sortBy = "normalized_date desc";
        Cursor cur = getContentResolver().query(uriConversation, projection, null, null, sortBy);
        while(cur.moveToNext()) {
            String ct_t = cur.getString(cur.getColumnIndex("ct_t"));
            int _id = cur.getInt(cur.getColumnIndexOrThrow("_id"));

            if(ct_t != null) {
                //it's a MMS
                messages.add(getMms(_id));
            }else{
                //is it a SMS
                messages.add(getSms(_id));
            }

        }
        cur.close();

        return messages;
    }

    public Sms getSms(int _id){
        String where = "_id = "+_id;
        Uri uri = Uri.parse("content://sms");
        Cursor cur = getContentResolver().query(uri, null, where, null, null);


        assert cur != null;
        cur.moveToFirst();

        String address = cur.getString(cur.getColumnIndex("address"));
        int type = cur.getInt(cur.getColumnIndex("type"));
        Long date = Long.parseLong(cur.getString(cur.getColumnIndex("date")));
        String body = cur.getString(cur.getColumnIndex("body"));

        cur.close();

        return new Sms(type, body, address, date);
    }


    public Mms getMms(int _id){
        String where = "mid = "+_id;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cur = getContentResolver().query(uri, null, where, null, null);

        assert cur != null;
        cur.moveToFirst();

        String partId = cur.getString(cur.getColumnIndex("_id"));
        String mimeType = cur.getString(cur.getColumnIndex("ct"));

        String body = null;
        if ("text/plain".equals(mimeType)) {
            body = getMmsText(cur, partId);
        }

        Bitmap image = null;
        if ("image/jpeg".equals(mimeType) || "image/bmp".equals(mimeType) ||
                "image/gif".equals(mimeType) || "image/jpg".equals(mimeType) ||
                "image/png".equals(mimeType)) {
            image = getMmsImage(partId);
        }

        String address = getAddressFromMms(_id);


        cur.close();

        uri = Uri.parse("content://mms/");
        where = "_id = " + _id;
        cur = getContentResolver().query(uri, null, where, null, null);

        assert cur != null;
        cur.moveToFirst();

        int type = cur.getInt(cur.getColumnIndex("type"));
        Long date = Long.parseLong(cur.getString(cur.getColumnIndex("date")));

        cur.close();

        return new Mms(type, body, address, date, image);

    }

    public String getMmsText(Cursor cur, String partId){
        String data = cur.getString(cur.getColumnIndex("_data"));
        String body;
        if (data != null) {
            //mean that the text is in a data, so we have to extract it
            body = getMmsTextInData(partId);
        } else {
            body = cur.getString(cur.getColumnIndex("text"));
        }
        return body;
    }

    public String getMmsTextInData(String _id) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
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
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

    public Bitmap getMmsImage(String _id) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {}
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
        String selectionAdd = new String("msg_id=" + _id);
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
        if (cAdd != null) {
            cAdd.close();
        }
        return address;
    }


    /*
    for the moment useless
     */
    public String dateSms(int _id){

        String where = "_id = "+_id;
        Uri uri = Uri.parse("content://sms");
        Cursor cur = getContentResolver().query(uri, null, where, null, null);
        Long date_s = null;
        assert cur != null;
        while(cur.moveToNext()) {
            date_s = Long.parseLong(cur.getString(cur.getColumnIndex("date")));

        }
        cur.close();
        String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date_s);
        return formattedDate;

    }



}
