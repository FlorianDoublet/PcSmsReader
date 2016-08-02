package com.example.florian_doublet.pcsmsreader.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.florian_doublet.pcsmsreader.Contact;
import com.example.florian_doublet.pcsmsreader.Contacts;
import com.example.florian_doublet.pcsmsreader.Helpers.SmsHelper;
import com.example.florian_doublet.pcsmsreader.SmsMms.Sms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flori on 05/05/2016.
 */
public class IncommingSmsBroadcastReceiver extends BroadcastReceiver {

    // Get the object of SmsManager
    private final SmsManager smsManager = SmsManager.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {
        //something
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Contacts contacts = Contacts.getInstance();

            //TODO: NE MARCHE PAS PREND PAS LE SMS QU'ON VIENS DE RECEVOIR MAIS L'ANCIEN

            Cursor cur = context.getContentResolver().query(SmsHelper.SMS_INBOX_CONTENT_PROVIDER, null, null, null, null);
            assert cur != null;
            cur.moveToFirst();

            //retrieve ID of the new sms
            int _id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));
            int thread_id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_THREAD_ID));
            Sms sms = SmsHelper.getSms(context, _id);
            cur.moveToNext();

            _id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));
            thread_id = cur.getInt(cur.getColumnIndexOrThrow(SmsHelper.COLUMN_THREAD_ID));
            sms = SmsHelper.getSms(context, _id);
            cur.close();

            //retrieve the sms

            String contactName = Contact.getContactNameByAddress(context, sms.getAddress());
            if(contactName == null) contactName = sms.getAddress();
            //create a temporary Contact to check if a conversation already exist
            Contact contact = new Contact(thread_id, contactName, sms.getAddress());
            contacts.addMessageToSpecificContact(contact, sms);

            //TODO TESTER SI CA RAJOUTE AU DEBUTT ET SUPPRIME A LA FIN
            Contacts contacts1 = Contacts.getInstance();


        }

    }
}
