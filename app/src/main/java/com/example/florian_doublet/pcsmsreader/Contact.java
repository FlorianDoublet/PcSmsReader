package com.example.florian_doublet.pcsmsreader;

/**
 * Created by Florian on 31/05/2016.
 */
public class Contact {

    public int thread_id;
    public String name;
    public String address;

    public Contact(int thread_id, String name, String address){
        this.thread_id = thread_id;
        this.name = name;
        this.address = address;
    }

    public int getThread_id() {
        return thread_id;
    }
}
