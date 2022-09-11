package com.example.venato.MessageInformation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MessageInfo extends AppCompatActivity {

    Context context;
    Double dueScore=0.0;
    Double totalRead=0.0;
    Double totalUnread=0.0;
    Double totalCredited=0.0;
    Double totalDebited=0.0;

    String DUMMY="Hey, [name]! We’re here to remind you that a payment for [$] is awaiting payment since [due date]. More info: [URL]. [Institution name]";

    private final String[] folders;
    private Date currDate=null;

    public MessageInfo(Context _context){
        folders= new String[]{"due", "credited", "debited"};
        context=_context;
        currDate=getDateFromGmt(Calendar.getInstance().getTime());
    }
    public Double getTotalRead(){
        return totalRead;
    }
    public Double getTotalUnread(){
        return totalUnread;
    }

    public List<Double> calScore(){
        List<Sms> allMessages=alignAllSms();
        List<Double> toReturn= new ArrayList<Double>(); //{"dueScore","totalRead","totalUnread","totalCredited","totalDebited"}

        for(Sms s:allMessages){
            if(s.get_folder().equals("due")){
                dueScore+=1;
            }

        }
        toReturn.add(dueScore);
        toReturn.add(totalRead);
        toReturn.add(totalUnread);
        toReturn.add(totalCredited);
        toReturn.add(totalDebited);
        return toReturn;
    }
    public List<Sms> alignAllSms(){
        List<Sms> listOfSms=new ArrayList<Sms>();
        Sms objSms = null;
        Uri message=Uri.parse("content://sms");

        Cursor c=context.getContentResolver().query(message,null,null,null,null);
        int totalSms=c.getCount();

        if(c.moveToFirst()){
            for(int i=0;i<totalSms;i++){
                String curr_Folder="None";
                for(String j:folders){
                    String body=c.getString(c.getColumnIndexOrThrow("body")).toLowerCase();
                    boolean permission=ifMessageIsNonPersonal(body,j);
                    if(permission){
                        curr_Folder=j;
                        break;
                    }
                }
                String epochString = c.getString(c.getColumnIndexOrThrow((CallLog.Calls.DATE)));
                long epoch = Long.parseLong( epochString );
                Date expiry = new Date(epoch);
                String _date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(expiry);
                Date dateOfCall=stringToDate(_date);

                if(!Objects.equals(curr_Folder, "None")){
                    objSms=new Sms();
                    objSms.set_id(c.getString(c.getColumnIndexOrThrow("_id"))); //throws IllegalArgumentException
                    objSms.set_address(c.getString(c.getColumnIndexOrThrow("address")));
                    objSms.set_msg(c.getString(c.getColumnIndexOrThrow("body")));
                    objSms.set_readState(c.getString(c.getColumnIndexOrThrow("read")));
                    objSms.set_date(_date);
                    objSms.set_folderName(curr_Folder);
                    listOfSms.add(objSms);

                    //updates the credited and debited
                    String msg= objSms.get_msg();
                    if(!Objects.equals(curr_Folder, "None")){
                        if(curr_Folder.equals("credited")){
                            Double amount=findAmountFromMessage(msg);
                            totalCredited+=amount;

                        }
                        if(curr_Folder.equals("debited")){
                            Double amount=findAmountFromMessage(msg);
                            totalDebited+=amount;
                        }
                    }
                }
                //check if the message read or not read or unread
                if(Integer.parseInt(c.getString(c.getColumnIndexOrThrow("read")))==1){
                    totalRead+=1;
                }
                else{
                    totalUnread+=1;
                }
                c.moveToNext();
            }
        }
        c.close();
        return listOfSms;
    }

    public Double findAmountFromMessage(String msg){

        int index=msg.indexOf("Rs");
        if(index==-1){
            return 0.0;
        }
        index+=2;
        String amount= new String();
        while( index<msg.length() && (msg.charAt(index)!='.' && msg.charAt(index)!=' ')){
            amount+=(msg.charAt(index));
            ++index;
        }
        if(!amount.isEmpty())
            return Double.parseDouble(amount);
        return 0.0;
    }

    /*checks the requirement of string accordingly*/
    /*checks the requirement of string accordingly*/
    public boolean ifMessageIsNonPersonal(String body,String toFind){
        if(toFind.equals("due")){
            return (body.contains("due") || body.contains("awaiting")) && body.contains("payment") && body.contains("remind") && (body.contains("₹") || body.contains("Rs"));
        }
        return body.contains(toFind);
    }

    //get difference between two dates
    public static int getDaysDifference(Date fromDate,Date toDate)
    {
        if(fromDate==null||toDate==null)
            return 0;

        return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
    }

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
