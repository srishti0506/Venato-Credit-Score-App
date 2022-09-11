package com.example.venato.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.venato.Appography.AppDetails;
import com.example.venato.Appography.AppoGraphy;
import com.example.venato.BatteryInformation.BatteryInfo;
import com.example.venato.CallingInformation.CallInfo;
import com.example.venato.CallingInformation.CallLogs;
import com.example.venato.MessageInformation.MessageInfo;
import com.example.venato.R;
import com.example.venato.databinding.ActivityMainBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    double averageBattery=-1;
    double prevScore=0;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_main);


        AppOpsManager usgStat = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = usgStat.checkOpNoThrow("android:get_usage_stats",android.os.Process.myUid(), getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        if(!granted){
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        getData(); //gets the battery Status and CREDIT_SCORE when last time device was active

        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.add(Manifest.permission.READ_CALL_LOG);
        listPermissionsNeeded.add(Manifest.permission.READ_SMS);

        ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);

        //call regarding information
        CallInfo regCalls=new CallInfo(this);
        List<Double> callParameters=regCalls.calScore();//{callScore,Incoming/Outgoing ratio}

        //message regarding info
        MessageInfo regMessage=new MessageInfo(this);
        List<Double> messageParameter=regMessage.calScore();//{"dueScore","totalRead","totalUnread","totalCredited","totalDebited"}


        //Battery regarding info
        BatteryInfo batteryObject=new BatteryInfo(this);
        double batteryPct=batteryObject.calBattery();
        double newBatteryScore=(averageBattery>0)?(batteryPct+averageBattery)/2:batteryPct;

        //Apps regarding info
        AppDetails appObject=new AppDetails(this);
        double appScore=appObject.calScore();//maximum 100



        double readUnreadRatio=((messageParameter.get(1))*100)/(messageParameter.get(1)+messageParameter.get(2)); //maximum 100
        double behaviouralScore=appScore-callParameters.get(0)+readUnreadRatio+newBatteryScore;   /*Behavioural Score*///maximum behavioural 200
        int behaviouralScorePercent = (int) ((behaviouralScore/200)*100);


        int check=(messageParameter.get(3)-messageParameter.get(4))<=0?0:1;

        double savingRatio=(messageParameter.get(3)-messageParameter.get(4))*check*100/messageParameter.get(3); //how much he saves in 60 days respective of credit score
        double clearanceScore=(20-messageParameter.get(0))<0?0:20-messageParameter.get(0);
        double financialScore=(savingRatio*0.20)+(clearanceScore*0.80);  //calculates the financial of 20% of savings and 80% of clearance score maximum can be 36
        int financialScorePercent = ( ((int)financialScore) / 36) * 100;


        double CREDIT_SCORE_WITHOUT_PERCENTAGE=behaviouralScore*0.10+financialScore*0.90;//maximum can be 62.4
        double BONUS=0.2;
        CREDIT_SCORE_WITHOUT_PERCENTAGE = (prevScore!=0 && (prevScore < CREDIT_SCORE_WITHOUT_PERCENTAGE)) ? (CREDIT_SCORE_WITHOUT_PERCENTAGE + BONUS) : CREDIT_SCORE_WITHOUT_PERCENTAGE;


        double CREDIT_SCORE_WITH_PERCENTAGE=(CREDIT_SCORE_WITHOUT_PERCENTAGE*100)/62.4;//maximum can be 62.4

        startAnimationCounter(0,behaviouralScorePercent, binding.progressbar, binding.counter);
        startAnimationCounter(0,(int) CREDIT_SCORE_WITH_PERCENTAGE, binding.progressbar2, binding.counter3);
        startAnimationCounter(0,financialScorePercent, binding.progressbar3, binding.counter4);

        // Filling Charts

        // APPOGRAPHY
        ArrayList<PieEntry> appUsageData = new ArrayList<PieEntry>();
        for(AppoGraphy app: appObject.getInfoList()){
            if(app.getTimeInForeground() > 2) appUsageData.add(new PieEntry(app.getTimeInForeground(), app.getAppName()));
        }
        showChart(R.id.activity_main_piechart,appUsageData);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // CALL TYPES
        ArrayList<PieEntry> callType = new ArrayList<PieEntry>();
        List<CallLogs> allContacts = null;
        try {
            allContacts = regCalls.alignAllCallLogs();
        } catch (ParseException e) {
            Log.i("IN ERROR","ooooooooooooooooo");
            e.printStackTrace();
        }

        int numOfIncoming = 0;
        int numOfOutgoing = 0;
        int numOfMissed = 0;


        for(CallLogs call : allContacts) {
            Log.i("call folder",""+call.get_folderName());
            if(call.get_folderName() == "Incoming")++numOfIncoming;
            else if(call.get_folderName() == "Outgoing")++numOfOutgoing;
            else if(call.get_folderName() == "MissedCalls")++numOfMissed;
        }

        Log.i("call Type", ""+numOfIncoming+" "+numOfOutgoing+" "+numOfMissed);

        callType.add(new PieEntry(numOfIncoming,"Incoming"));
        callType.add(new PieEntry(numOfOutgoing,"Outgoing"));
        callType.add(new PieEntry(numOfMissed,"Missed"));
        showChart(R.id.activity_main_piechart2,callType);
        //////////////////////////////////////////////////////////////////////////////////////////////


        // MESSAGES
        ArrayList<PieEntry> msgType = new ArrayList<PieEntry>();
        msgType.add(new PieEntry( regMessage.getTotalRead().floatValue(),"Read"));
        msgType.add(new PieEntry( regMessage.getTotalUnread().floatValue(),"Not Read"));
        showChart(R.id.activity_main_piechart3,msgType);
        //////////////////////////////////////////////////////////////////////////////////////////////





        Log.i("##CallDuration Score ", callParameters.get(0)+" ##appScore"+appScore);
        Log.i("##Due Score ", messageParameter.get(0)+" ##Total read unread messages "+messageParameter.get(1)+" "+messageParameter.get(2)+" Total Credited Rupees "+messageParameter.get(3)+"Total Debited Rupees "+messageParameter.get(4));
        Log.i("##Current Battery % ", batteryPct+"##New Battery Score "+newBatteryScore);
        Log.i("##Behavioural Score ", behaviouralScore+"##Credit Score with percentage "+CREDIT_SCORE_WITH_PERCENTAGE);

        averageBattery=newBatteryScore;
        prevScore=CREDIT_SCORE_WITHOUT_PERCENTAGE;
        putData();
    }

        /*Insert the latest id in local Storage till which we have read the messages--for optimal usage*/
        public void putData(){
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();  // Put the values from the UI

            editor.putFloat("batteryStatus",(float) averageBattery);
            editor.putFloat("CREDIT_SCORE",(float) prevScore);
            editor.apply();
        }

        /*Fetches the latest id in local storage*/
        public void getData() {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            averageBattery=preferences.getFloat("batteryStatus",0);
            prevScore=preferences.getFloat("CREDIT_SCORE",0);
        }
    private void startAnimationCounter(int start_no, int end_no, final ProgressBar pb, final TextView val)
    {
        ValueAnimator animator=ValueAnimator.ofInt(start_no,end_no);
        animator.setDuration(5000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                val.setText(animation.getAnimatedValue().toString()+"");
                pb.setProgress(Integer.parseInt(animation.getAnimatedValue().toString()));

            }
        });
        animator.start();
    }
    private  void showChart(int grphId, ArrayList<PieEntry> entries){
        setupPieChart(grphId);
        loadPieChartData(grphId, entries);
    }

    private void setupPieChart(int grphId) {
        PieChart pieChart;
        pieChart = findViewById(grphId);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Spending by Category");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(true);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void loadPieChartData(int grphId, ArrayList<PieEntry> entries) {
        PieChart pieChart;
        pieChart = findViewById(grphId);
        //ArrayList<PieEntry> entries = new ArrayList<>();


        ArrayList<Integer> colors = new ArrayList<>();
        int[] rainbow = getResources().getIntArray(R.array.rainbow);
        for(int i=0; i<rainbow.length; ++i){
            colors.add(rainbow[i]);
        }
        for (int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }

        for (int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Category");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }
}