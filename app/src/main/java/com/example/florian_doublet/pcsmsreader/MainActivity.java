package com.example.florian_doublet.pcsmsreader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static HashMap<Integer, String> conversationsHash = new HashMap<Integer, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Integer> conv_ids = getAllConversationsId();
        for(Integer conv_id : conv_ids){
            getAllMessageFromConversation(conv_id);
        }
    }

    public List<Integer> getAllConversationsId(){
        List<Integer> conversationsId = new ArrayList<Integer>();

        Uri uriConversations = Uri.parse("content://mms-sms/conversations");

        //all the fields we want
        final String[] projection = new String[]{"thread_id", "address"};
        final String sortBy = "date";
        Cursor cur = getContentResolver().query(uriConversations, projection, null, null, sortBy);

        while(cur.moveToNext()){
            Integer thread_id = cur.getInt(cur.getColumnIndexOrThrow("thread_id"));
            String address = cur.getString(cur.getColumnIndexOrThrow("address"));
            String name = getContactNameByAddress(address);
            if(name == null){
                name = address;
            }
            conversationsHash.put(thread_id, name);
            conversationsId.add(thread_id);
        }

        cur.close();

        return conversationsId;
    }

    public String getContactNameByAddress(String address){
        String name = null;

        Uri uriContact = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        final String[] projection = new String[]{"DISPLAY_NAME"};
        final String where = ContactsContract.CommonDataKinds.Phone.NUMBER+"='"+address+"'";
        Cursor cur = getContentResolver().query(uriContact,null,where,null,null);
        while(cur.moveToNext()) {
            name = cur.getString(cur.getColumnIndexOrThrow("DISPLAY_NAME"));
        }
        cur.close();
        return name;
    }

    public List<String>  getAllMessageFromConversation(int conv_id){
        List<String> messages = new ArrayList<String>();
        Uri uriConversation = Uri.parse("content://mms-sms/conversations/" + Integer.toString(conv_id));
        final String[] projection = new String[]{"body"};
        //actually a problem here
        final String sortBy = "date";
        Cursor cur = getContentResolver().query(uriConversation, projection, null, null, sortBy);

        while(cur.moveToNext()) {
            String message = cur.getString(cur.getColumnIndexOrThrow("body"));
            messages.add(message);
        }

        return messages;
    }



}
