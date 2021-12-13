package com.insa.projet4a;

public class User {
    
    public Integer id;
    public String  ip;
    public String  name;
    public Boolean connected;
    
    public User(Integer id, String ip, String name){
        this.id = id;
        this.ip = ip;
        this.name = name;

        // Je sais pas si vraiment utile
        this.connected = true;
    }

    @Override
    public String toString() {
        return "User (" + id + ") "
            + name
            + " ip : " + ip;
    }
}
