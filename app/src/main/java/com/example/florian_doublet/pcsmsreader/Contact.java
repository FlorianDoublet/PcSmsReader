package com.example.florian_doublet.pcsmsreader;

import com.example.florian_doublet.pcsmsreader.SmsMms.SmsMms;

import java.util.List;

/**
 * Created by Florian on 31/05/2016.
 */
public class Contact {

    protected int thread_id;
    public String name;
    public String address;
    protected List<SmsMms> conversation;

    public Contact(int thread_id, String name, String address){
        this.thread_id = thread_id;
        this.name = name;
        this.address = address;
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setConversation(List<SmsMms> conversation){
        this.conversation = conversation;
    }
}
