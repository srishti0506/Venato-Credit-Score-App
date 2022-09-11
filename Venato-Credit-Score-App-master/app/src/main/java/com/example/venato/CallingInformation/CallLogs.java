package com.example.venato.CallingInformation;

public class CallLogs{
    private String _id;
    private String _caller;
    private String _duration;
    private String _type;
    private String _date;
    private String _folderName;

    //getters
    public String get_id() {
        return _id;
    }

    public String get_caller() {
        return _caller;
    }

    public String get_duration() {
        return _duration;
    }

    public String get_type() {
        return _type;
    }

    public String get_date() {
        return _date;
    }
    public String get_folderName(){
        return _folderName;
    }

    //setters
    public void set_id(String _id) {
        this._id = _id;
    }

    public void set_caller(String _caller) {
        this._caller = _caller;
    }

    public void set_duration(String _duration) {
        this._duration = _duration;
    }

    public void set_type(String _type) {
        this._type = _type;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public void set_folderName(String _folderName){
        this._folderName=_folderName;
    }
}
