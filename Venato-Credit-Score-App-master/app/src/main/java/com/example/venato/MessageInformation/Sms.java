package com.example.venato.MessageInformation;

class Sms{
    private String _id;
    private String _address;
    private String _msg;
    private String _readState;
    private String _date;
    private String _folderName;

    //getters
    public String get_id(){
        return _id;
    }
    public String get_address(){
        return _address;
    }
    public String get_msg(){
        return _msg;
    }
    public String get_readState(){
        return _readState;
    }
    public String get_date(){
        return _date;
    }
    public String get_folder(){
        return _folderName;
    }

    //setters
    public void set_id(String id) {
        _id = id;
    }
    public void set_address(String address){
        _address = address;
    }
    public void set_msg(String msg){
        _msg = msg;
    }
    public void set_readState(String readState){ // 1:read 0:not read
        _readState = readState;
    }
    public void set_date(String date){
        _date = date;
    }
    public void set_folderName(String folderName){
        _folderName=folderName;
    }
}