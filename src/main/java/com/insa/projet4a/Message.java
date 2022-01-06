package com.insa.projet4a;

public class Message {

    public Boolean from;
    public String date;
    public String content;
    
    public Message(Boolean from, String date, String content){
        this.from = from;
        this.date = date;
        this.content = content;
    }   

    @Override
    public String toString() {
        return "Message from? " + from.toString() + " "
                + date + " -> " + content;
    }
}
