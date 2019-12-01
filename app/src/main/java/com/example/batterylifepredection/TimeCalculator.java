package com.example.batterylifepredection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCalculator {
    public  String predict(int row_no,String mapping_filename,String secondaryFilePath,int drain) throws Exception{
        String mapped="";
        int t=0;
        try{
            File mapping_file = new File(mapping_filename);
            FileReader fileReader = new FileReader(mapping_file);
            BufferedReader br = new BufferedReader(fileReader);
            for(int i = 0; i < row_no; ++i)
                br.readLine();
            System.out.println("yes");
            mapped = br.readLine();
            System.out.println(mapped+drain);
            Log.e("JKT",mapped+" "+drain);
            br.close();
            fileReader.close();
        }
        catch(IOException e){
            Log.e("JKT","before secondary");
            e.printStackTrace();
        }
        String[] mapped_to=mapped.split(":");
        mapped_to[0]=mapped_to[0].substring(4,8)+mapped_to[0].substring(2,4)+mapped_to[0].substring(0,2);
        int date=Integer.parseInt(mapped_to[0]);
        int time=Integer.parseInt(mapped_to[1]);
        int battery=Integer.parseInt(mapped_to[2]);


        boolean night_case=true;

        if(time<=70000){
            night_case=false;
        }

        try{
            FileReader fileReader = new FileReader(secondaryFilePath);
            BufferedReader br=new BufferedReader(fileReader);
            String st="";
            int flag=0;
            int prev_date=0;
            int prev_time=0;
            int prev_battery=0;
            int start_level_battery=0;
            int current_battery=0;
            int curr_decreasing_battery=0;
            int last_level_time=0;
            int count=0;
            while((st=br.readLine())!=null && drain>0){
                count=count+1;
                Log.e("JKT","reading secondary");
                String[] sample=st.split(",");
                int this_date=Integer.parseInt(sample[0]);
                int this_time=Integer.parseInt(sample[1]);
                int this_battery=Integer.parseInt(sample[2]);
                Log.e("JKT","got data"+" "+count+" "+drain);
                if((flag==0 && this_date<date) || (this_date==date && this_time<=time)){
                    prev_date=this_date;
                    prev_time=this_time;
                    prev_battery=this_battery;

                }
                else if((flag==0 && this_date==date && this_time>time) || (flag==0 && this_date>date)){
                    start_level_battery=prev_battery;
                    current_battery=prev_battery;
                    curr_decreasing_battery=prev_battery;
                    last_level_time=prev_time;
                    flag=1;
                    System.out.println("Starting from "+start_level_battery+" "+prev_date+" "+prev_time);
                    Log.e("JKT","stopped at"+" "+start_level_battery+" "+prev_date+" "+prev_time);
                }
                if(flag==1 && drain>0){

                    if(night_case==true && this_time<=70000){
                        System.out.println("night case");
                        int time_left_in_minutes=10*drain;
                        int hours=time_left_in_minutes/60;
                        int minutes=time_left_in_minutes%60;
                        int time_left=(hours*100)+minutes;
                        time_left=time_left*100; //time_left is now as hhmmss
                        t=add(t,time_left);
                        int hhmm=t/100;
                        int hh=hhmm/100;
                        int mm=hhmm%100;
                        if(mm>59){
                            mm=mm-59;
                            hh=hh+1;
                        }
                        Log.e("JKT","night case");
                        String result=hh+" hours "+mm+" minutes remianing.";
                        br.close();
                        return result;
                    }
                    else{
                        if(this_battery<curr_decreasing_battery){

                            if(this_battery<0 && curr_decreasing_battery<0){
                                drain=drain-(curr_decreasing_battery-this_battery);
                            }
                            else if(this_battery<0 && curr_decreasing_battery>=0){
                                drain=drain-(curr_decreasing_battery-this_battery);
                            }
                            else if(this_battery>=0 && curr_decreasing_battery>=0){
                                drain=drain-(curr_decreasing_battery-this_battery);
                            }
                            curr_decreasing_battery=this_battery;
                        }
                        if(this_battery<=current_battery){
                            int this_discharge_time=cal_time(this_time,last_level_time); //calculate difference of time
                            t=add(t,this_discharge_time);
                            current_battery=this_battery;
                            last_level_time=this_time;
                        }
                        if((this_battery>curr_decreasing_battery) || (drain<=0)){
                            last_level_time=this_time;
                            current_battery=this_battery;
                            curr_decreasing_battery=this_battery;
                        }
                    }
                }

            }
            br.close();
        }
        catch(IOException e){
            Log.e("JKT","Some error");
            e.printStackTrace();

            return "Something went wrong. Please try again." ;
        }
        if(drain>0){
            System.out.println(drain);
            return "Could not find desired drainage in past within 2 days.";
        }
        else{
            int hhmm=t/100;
            int hh=hhmm/100;
            int mm=hhmm%100;
            if(mm>59){
                mm=mm-59;
                hh=hh+1;
            }
            Log.e("JKT","normal case");
            return hh+" hours "+mm+" minutes remaining.";
        }
    }


    public  int add(int t1,int t2){
        Log.e("JKT","ENTERED ADD"+" "+t1+" "+t2);
        String n1=Integer.toString(t1);
        String n2=Integer.toString(t2);
        if(n1.length()>6 || n2.length()>6){
            System.out.println("Some error occured. Try again");
            throw new ArithmeticException();
        }
        int len1=n1.length();
        int len2=n2.length();
        String h1="",m1="",s1="",h2="",m2="",s2="";
        if(len1==6){
            h1=n1.substring(0,2);
            m1=n1.substring(2,4);
            s1=n1.substring(4,6);
        }
        else if(len1==5){
            h1=n1.substring(0,1);
            m1=n1.substring(1,3);
            s1=n1.substring(3,5);
        }
        else if(len1==4){
            h1="00";
            m1=n1.substring(0,2);
            s1=n1.substring(2,4);

        }
        else if(len1==3){
            h1="00";
            m1="0"+n1.substring(0,1);
            s1=n1.substring(1,3);
        }
        else if(len1==2){
            h1="00";
            m1="00";
            s1=n1.substring(0,2);
        }
        else if(len1==1){
            h1="00";
            m1="00";
            s1="0"+n1.substring(0,1);
        }

        if(len2==6){
            h2=n2.substring(0,2);
            m2=n2.substring(2,4);
            s2=n2.substring(4,6);
        }
        else if(len2==5){
            h2=n2.substring(0,1);
            m2=n2.substring(1,3);
            s2=n2.substring(3,5);
        }
        else if(len2==4){
            h2="00";
            m2=n2.substring(0,2);
            s2=n2.substring(2,4);

        }
        else if(len2==3){
            h2="00";
            m2="0"+n2.substring(0,1);
            s2=n2.substring(1,3);
        }
        else if(len2==2){
            h2="00";
            m2="00";
            s2=n2.substring(0,2);
        }
        else if(len2==1){
            h2="00";
            m2="00";
            s2="0"+n2.substring(0,1);
        }

        Integer sec1=Integer.parseInt(s1);
        Integer sec2=Integer.parseInt(s2);
        Integer min1=Integer.parseInt(m1);
        Integer min2=Integer.parseInt(m2);
        Integer hr1=Integer.parseInt(h1);
        Integer hr2=Integer.parseInt(h2);

        int sec=sec1+sec2;
        int min=min1+min2;
        int hr=hr1+hr2;
        if(sec>59){
            sec=sec-59;
            min=min+1;
            if(min>59){
                min=min-59;
                hr=hr+1;
            }
        }
        if(min>59){
            min=min-59;
            hr=hr+1;
        }

        String m=Integer.toString(min);
        if(m.length()==1){
            m="0"+m;
        }

        String s=Integer.toString(sec);
        if(s.length()==1){
            s="0"+s;
        }

        String hhmmss=Integer.toString(hr)+m+s;
        return Integer.parseInt(hhmmss);
    }


    public  int cal_time(int t1,int t2) throws Exception{

        System.out.println("Difference between "+t1+" "+t2);
        String stop=Integer.toString(t1);
        if(stop.length()==6){
            stop=stop.substring(0,2)+":"+stop.substring(2,4)+":"+stop.substring(4,6);
        }
        else if(stop.length()==5){
            stop="0"+stop.substring(0,1)+":"+stop.substring(1,3)+":"+stop.substring(3,5);
        }
        else if(stop.length()==4){
            stop="00"+":"+stop.substring(0,2)+":"+stop.substring(2,4);
        }
        else if(stop.length()==3){
            stop="00"+":"+"0"+stop.substring(0,1)+":"+stop.substring(1,3);
        }
        else if(stop.length()==2){
            stop="00"+":"+"00"+":"+stop.substring(0,2);
        }
        else if(stop.length()==1){
            stop="00"+":"+"00"+":"+"0"+stop.substring(0,1);
        }

        String start=Integer.toString(t2);
        if(start.length()==6){
            start=start.substring(0,2)+":"+start.substring(2,4)+":"+start.substring(4,6);
        }
        else if(start.length()==5){
            start="0"+start.substring(0,1)+":"+start.substring(1,3)+":"+start.substring(3,5);
        }
        else if(start.length()==4){
            start="00"+":"+start.substring(0,2)+":"+start.substring(2,4);
        }
        else if(start.length()==3){
            start="00"+":"+"0"+start.substring(0,1)+":"+start.substring(1,3);
        }
        else if(start.length()==2){
            start="00"+":"+"00"+":"+start.substring(0,2);
        }
        else if(start.length()==1){
            start="00"+":"+"00"+":"+"0"+start.substring(0,1);
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date1 = format.parse(stop);
        Date date2 = format.parse(start);
        int difference =(int)( date1.getTime() - date2.getTime());
        int difference_in_sec=(int)difference/1000;
        if(t1<t2){
            difference_in_sec=86400+difference_in_sec;
        }
        System.out.println(difference_in_sec);
        int hours=difference_in_sec/3600;
        int sec=difference_in_sec%3600;
        int mins=sec/60;
        sec=sec%60;
        int time=hours*100+mins;
        time=time*100+sec;
        System.out.println(time);
        return time;

    }
}
