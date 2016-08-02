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






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Contacts contacts = Contacts.getInstance();

        loadAllContacts();
        //TODO take a long time, need to see why
        for(Contact contact : contacts.contactsConversations){
            contact.setConversation(getAllMessageFromConversation(contact.getThread_id()));
        }
        contacts.contactsConversations = contacts.contactsConversations;
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
                address = SmsHelper.getAddressFromMms(getApplicationContext(), _id);
            } else{
                //it's a SMS
               address = cur.getString(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_ADDRESS));
            }

            String name = Contact.getContactNameByAddress(getApplicationContext(), address);
            if(name == null){
                name = address;
            }
            Contacts.getInstance().contactsConversations.add(new Contact(thread_id, name, address));
        }

        cur.close();
    }



    public List<SmsMms>  getAllMessageFromConversation(int thread_id){
        List<SmsMms> messages = new ArrayList<>();
        Uri uriConversation = SmsHelper.getConversationProviderWithThreadId(thread_id);
        final String[] projection = new String[]{SmsHelper.COLUMN_ID, SmsHelper.COLUMN_MMS};
        final String sortBy = SmsHelper.COLUMN_DATE_NORMALIZED;
        Cursor cur = getContentResolver().query(uriConversation, projection, null, null, sortBy + " desc limit " + Contact.MESSAGE_LIMIT);
        //will go through all messages for this conversation thread
        while(cur.moveToNext()) {
            String ct_t = cur.getString(cur.getColumnIndex(SmsHelper.COLUMN_MMS));
            int _id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));

            if(SmsHelper.isMms(ct_t)) {
                messages.add(SmsHelper.getMms(getApplicationContext(), _id));
            }else{
                messages.add(SmsHelper.getSms(getApplicationContext(),_id));
            }

        }
        cur.close();

        return messages;
    }












}
