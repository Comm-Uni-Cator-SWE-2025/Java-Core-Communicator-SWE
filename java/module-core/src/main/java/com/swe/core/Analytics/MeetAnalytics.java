package com.swe.core.Analytics;

import java.util.HashMap;
import java.util.Map;

public class MeetAnalytics {
//    Working on Meet Analytics
    private String[][] data; //row = {id,start,end}

    public MeetAnalytics(String[][] data){
        this.data = data;
    }

//    Active member = Number of people still Alive
    public Map<String,String> activeUsers(){
        Map<String,String> active = new HashMap<>();
        for (int i = 0; i < data.length; i++){
            if(data[i][2].equalsIgnoreCase("online")){
                active.put(data[i][0], data[i][1]);
            }
        }
        return active;
    }

//  Left Member = Number of people who left the Meeting
    public Map<String, String[]> leftUsers() {
        Map<String, String[]> left = new HashMap<>();

        for (int i = 0; i < data.length; i++) {
            if (!data[i][2].equalsIgnoreCase("online")) {
                left.put(data[i][0], new String[]{data[i][1], data[i][2]});
            }
        }
        return left;
    }

//    Return Total Length To Know Total user
    public int size(){
        return data.length;
    }

    public static void main(String[] args) {
        String[][] cloudData = {
                {"u1", "10:00", "11:00"},
                {"u2", "10:30", "online"},
                {"u3", "11:00", "11:45"}
        };
        MeetAnalytics ma = new MeetAnalytics(cloudData);

        System.out.println("Active Users: " + ma.activeUsers());
        System.out.println("Left Users: " + ma.leftUsers());
        System.out.println("Total Users: " + ma.size());
    }
}
