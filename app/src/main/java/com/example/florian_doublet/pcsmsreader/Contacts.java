package com.example.florian_doublet.pcsmsreader;

import com.example.florian_doublet.pcsmsreader.SmsMms.SmsMms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Florian on 08/07/2016.
 */
public class Contacts {

    public List<Contact> contactsConversations;

    private Contacts(){
        contactsConversations = new ArrayList<>();
    }

    private static Contacts INSTANCE = new Contacts();

    public static Contacts getInstance(){return INSTANCE;}

    //Will add the new message to the conversation of the contact ( and add it if the conversation doesn't exist )
    public void addMessageToSpecificContact(Contact contact, SmsMms message){
        for(Contact contactConv : contactsConversations){
            if(contactConv.name.equals(contact.name) && contactConv.address.equals(contact.address)){
                contactConv.addMessage(message);
                return;
            }
        }
        //TODO: get thread ID for this contact
        contactsConversations.add(contact);
        contact.addMessage(message);

    }




}
