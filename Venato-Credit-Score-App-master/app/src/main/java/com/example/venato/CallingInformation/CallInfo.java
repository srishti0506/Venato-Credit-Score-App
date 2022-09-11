package com.example.venato.CallingInformation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CallInfo {

    Context context;
    private Date currDate=null;
    private String[] messageFolders={"None","Incoming","Outgoing","MissedCalls","VoiceMail","CallCancelled","CallBlocked","OnAnotherPc","SomethingElse"};


    public CallInfo(Context _context){
        context=_context;
        currDate=getDateFromGmt(Calendar.getInstance().getTime());
    }

    public List<Double> calScore(){
        List<CallLogs> allContacts=null;
        try{
            allContacts=alignAllCallLogs();
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        double totalHours=(double)0;
        double totalSeconds=(double)0;
        double totalIncoming=(double)0;
        double totalOutgoing=(double)0;
        double inOutRatio=0;

        HashMap<String, Integer> incomingUniqueness = new HashMap<>();
        HashMap<String, Integer> outgoingUniqueness = new HashMap<>();

        for(CallLogs i:allContacts) {


            totalSeconds +=(double)Integer.parseInt(i.get_duration()) ; //into hours
            if(i.get_folderName()=="Incoming" && (!incomingUniqueness.containsKey(i.get_caller()))){
                incomingUniqueness.put(i.get_caller(),1);
            }
            if(i.get_folderName()=="Outgoing" && (!outgoingUniqueness.containsKey(i.get_caller()))){
                outgoingUniqueness.put(i.get_caller(),1);
            }
        }


        totalIncoming=incomingUniqueness.size();
        totalOutgoing=incomingUniqueness.size();

        totalHours=(double)totalSeconds/3600;
        double score=(double)(totalHours/24)*100;

        if(totalOutgoing!=0){
            inOutRatio=totalIncoming/totalOutgoing;
        }
        else{
            inOutRatio=totalIncoming;
        }

        List<Double> parameters=new ArrayList<>();
        parameters.add(score);
        parameters.add(inOutRatio);

        return parameters;
    }
    public List<CallLogs> alignAllCallLogs() throws ParseException {

        List<CallLogs> listOfCallLogs=new ArrayList<CallLogs>();
        CallLogs objCalls;
        Uri message = Uri.parse("content://call_log/calls");

        Cursor c=context.getContentResolver().query(message,null,null,null,null);
        int totalCalls=c.getCount();

        if(c.moveToFirst()){

            for(int i=0;i<totalCalls;i++) {

                objCalls = new CallLogs();


                //converts dates into  simple formats
                String epochString = c.getString(c.getColumnIndexOrThrow((CallLog.Calls.DATE)));
                long epoch = Long.parseLong( epochString );
                Date expiry = new Date( epoch * 1 );
                String _date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(expiry);
                Date dateOfCall=stringToDate(_date);

                if(getDaysDifference(dateOfCall,currDate)<=1){
                    objCalls.set_caller(c.getString(c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)));
                    objCalls.set_duration(c.getString(c.getColumnIndexOrThrow(CallLog.Calls.DURATION)));
                    objCalls.set_type(c.getString(c.getColumnIndexOrThrow(CallLog.Calls.TYPE)));
                    objCalls.set_id(c.getString(c.getColumnIndexOrThrow("_id"))); //throws IllegalArgumentException
                    objCalls.set_date(_date);

                    objCalls.set_folderName(messageFolders[(Integer.parseInt(c.getString(c.getColumnIndexOrThrow(CallLog.Calls.TYPE))))]);
                    listOfCallLogs.add(objCalls);
                }
                else{
                    continue;
                }
                c.moveToNext();
            }

        }
        c.close();
        return listOfCallLogs;
    }

    //get difference between two dates
    public static int getDaysDifference(Date fromDate,Date toDate)
    {
        if(fromDate==null||toDate==null)
            return 0;

        return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    //formats to date
    public Date getDateFromGmt(Date date){
        String pattern="dd-MM-yyy";
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(pattern);
        String getDate=simpleDateFormat.format(date);
        Date newDate=null;
        try {
            newDate=simpleDateFormat.parse(getDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }
    public Date stringToDate(String date){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        Date newDate=null;
        try {
            newDate=simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getDateFromGmt(newDate);
    }
}
