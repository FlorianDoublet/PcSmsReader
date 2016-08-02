package com.example.florian_doublet.pcsmsreader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.florian_doublet.pcsmsreader.SmsMms.SmsMms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Florian on 31/05/2016.
 */
public class Contact {

    public static int MESSAGE_LIMIT = 5;

    protected int thread_id;
    public String name;
    public String address;
    protected List<SmsMms> conversation;

    public Contact(int thread_id, String name, String address){
        this.thread_id = thread_id;
        this.name = name;
        this.address = address;
    }

    public Contact(String name, String address){
        this.name = name;
        this.address = address;
        this.conversation = new ArrayList<>();
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setConversation(List<SmsMms> conversation){
        this.conversation = conversation;
    }

    public static String getContactNameByAddress(Context context, String address){
        String name = null;

        Uri uriContact = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        final String where = ContactsContract.CommonDataKinds.Phone.NUMBER+"='"+address+"'";
        Cursor cur = context.getContentResolver().query(uriContact,null,where,null,null);
        assert cur != null;
        while(cur.moveToNext()) {
            name = cur.getString(cur.getColumnIndexOrThrow("DISPLAY_NAME"));
        }
        cur.close();
        return name;
    }

    //will delete the older and add the new one
    public void addMessage(SmsMms message){
        conversation.add(0, message);
        if(conversation.size() > MESSAGE_LIMIT){
            conversation.remove(MESSAGE_LIMIT);
        }
    }
}
