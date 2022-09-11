package com.example.venato.Appography;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppDetails extends AppCompatActivity {


    Context context;
    //appography Global variables
    private UsageStatsManager mngr;
    private PackageManager mPm;
    private AppsInfo res; // this is the calss object storing all app info and productive non productive time
    ArrayList<AppoGraphy> infoList;

    public AppDetails(Context _context) {
        res = new AppsInfo();
        context = _context;
//        //Log.i("Check This is Score","Hello");
        mngr = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        mPm = context.getApplicationContext().getPackageManager();
    }

    private boolean compareString(String s1, String s2) {
        if (s1.compareTo(s2) == 0) return true;
        return false;
    }


    public ArrayList<AppoGraphy> getInfoList() {
        this.infoList = res.getInfoList();
        return this.infoList;
    }

    private class AppsInfo {
        ArrayList<AppoGraphy> infoList;
        double nonProd;
        double prod;
        double msg;
        double score;


        public void setScore() {
            double nonProdH = this.nonProd / 60;
            double prodH = this.prod / 60;
            double msgH = this.msg / 60;
            ////Log.i("Component", nonProdH+" "+prodH+" "+msgH);
            this.score = ((msgH / 24.0) * 100) + ((prodH / 24.0) * 100) - ((nonProdH / 24.0) * 100); //calculating
        }

        public double getScore() {
            return this.score;
        }

        public void setTimings() {
            double sumNP = 0;
            double sumP = 0;
            double sumMsg = 0;

            for (AppoGraphy appoGraphy : this.infoList) {
                // Do according to your requirement
                if (compareString(appoGraphy.appName, "Instagram") || compareString(appoGraphy.appName, "Reddit") || compareString(appoGraphy.appName, "Snapchat") || compareString(appoGraphy.appName, "WhatsApp") ||
                        compareString(appoGraphy.appName, "Zomato") || compareString(appoGraphy.appName, "Swiggy") ||
                        compareString(appoGraphy.appName, "Facebook") || compareString(appoGraphy.appName, "YouTube") ||
                        compareString(appoGraphy.appName, "Discord") || compareString(appoGraphy.appName, "Telegram") ||
                        (appoGraphy.category != null && compareString(appoGraphy.category, "Games"))) {
                    sumNP += appoGraphy.timeInForeground;
                } else {
                    sumP += appoGraphy.timeInForeground;
                }
                if (compareString(appoGraphy.appName, "Messages") || compareString(appoGraphy.appName, "Gmail")) {
                    sumMsg += appoGraphy.timeInForeground;
                }


                ////Log.i("output","info: "+appoGraphy.category + " : " + appoGraphy.launchCount + " : " + appoGraphy.appName + " : " + appoGraphy.timeInForeground + "\n\n");
            }
            this.msg = sumMsg;
            this.prod = sumP;
            this.nonProd = sumNP;
            setScore();
            //Log.i("WORK ND CHILL"," "+this.score); //score
        }

        public double getNonProd() {
            return this.nonProd;
        }

        public double getMsg() {
            return this.msg;
        }

        public double getProd() {
            return this.prod;
        }

        public ArrayList<AppoGraphy> getInfoList() {
            return this.infoList;
        }
    }

    public double calScore() {

        String[] apps = new String[0];
        double appScore = 0.0;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -1);
        HashMap<String, AppoGraphy> map = new HashMap<>();
        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();

        //Log.i("qwerty","Button Clicked");

        UsageEvents.Event currentEvent;
        long start_time = cal.getTimeInMillis();
        long end_time = System.currentTimeMillis();

        if (mngr != null) {
            // Get all apps data from starting time to end time
            UsageEvents usageEvents = mngr.queryEvents(start_time, end_time);

            // Put these data into the map
            int sz = 0;
            while (usageEvents.hasNextEvent()) {
                currentEvent = new UsageEvents.Event();
                usageEvents.getNextEvent(currentEvent);
                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ||
                        currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {

                    String key = currentEvent.getPackageName();

                    PackageInfo pk = null;
                    try {
                        pk = mPm.getPackageInfo(currentEvent.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    String applicationName = "";

                    applicationName = pk.applicationInfo.loadLabel(context.getPackageManager()).toString();
                    Drawable applicationIcon = pk.applicationInfo.loadIcon(context.getPackageManager());

                    ApplicationInfo applicationInfo = null;
                    try {
                        applicationInfo = mPm.getApplicationInfo(key, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    String categoryTitle = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int appCategory = applicationInfo.category;
                        categoryTitle = (String) ApplicationInfo.getCategoryTitle(context, appCategory);
                    }


                    if (map.get(key) == null) {
                        AppoGraphy temp = new AppoGraphy(key);
                        temp.setAppName(applicationName);
                        temp.setAppIcon(applicationIcon);
                        temp.setCategory(categoryTitle);
                        map.put(key, temp);
                        ++sz;
                        sameEvents.put(key, new ArrayList<UsageEvents.Event>());
                    }

                    sameEvents.get(key).add(currentEvent);
                }
            }
            apps = new String[sz];

            // Traverse through each app data which is grouped together and count launch, calculate duration
            for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
                int totalEvents = entry.getValue().size();
                if (totalEvents > 1) {
                    for (int i = 0; i < totalEvents - 1; i++) {
                        UsageEvents.Event E0 = entry.getValue().get(i);
                        UsageEvents.Event E1 = entry.getValue().get(i + 1);

                        if (E1.getEventType() == 1 || E0.getEventType() == 1) {
                            map.get(E1.getPackageName()).launchCount++;
                        }

                        if (E0.getEventType() == 1 && E1.getEventType() == 2) {
                            long diff = E1.getTimeStamp() - E0.getTimeStamp();
                            diff = (diff / 1000) / 60;
                            map.get(E0.getPackageName()).timeInForeground += diff;
                        }
                    }
                }

                // If First eventtype is ACTIVITY_PAUSED then added the difference of start_time and Event occuring time because the application is already running.
                if (entry.getValue().get(0).getEventType() == 2) {
                    long diff = entry.getValue().get(0).getTimeStamp() - start_time;
                    diff = (diff / 1000) / 60;
                    map.get(entry.getValue().get(0).getPackageName()).timeInForeground += diff;
                }

                // If Last eventtype is ACTIVITY_RESUMED then added the difference of end_time and Event occuring time because the application is still running .
                if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
                    long diff = end_time - entry.getValue().get(totalEvents - 1).getTimeStamp();
                    diff = (diff / 1000) / 60;
                    map.get(entry.getValue().get(totalEvents - 1).getPackageName()).timeInForeground += diff;
                }

            }


            res.infoList = new ArrayList<>(map.values());
            res.setTimings();
            appScore = res.getScore();

            String strMsg = "";

            // Concatenating data to show in a text view. You may do according to your requirement
            int i = 0;


            for (AppoGraphy appoGraphy : res.infoList) {
                // Do according to your requirement
                //Log.i("output","info: "+appoGraphy.category + " : " + appoGraphy.launchCount + " : " + appoGraphy.appName + " : " + appoGraphy.timeInForeground + "\n\n");
            }
            //listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, apps));
        }
        return appScore;
    }
}