package com.hostel.model;

import java.util.ArrayList;

public class Hostel {
    private final String hostelId;
    private String hostelName, wardenName, location;
    private final ArrayList<Room> rooms = new ArrayList<>();

    public Hostel(String hostelId, String hostelName, String wardenName, String location) {
        this.hostelId=hostelId; this.hostelName=hostelName;
        this.wardenName=wardenName; this.location=location;
    }
    public String getHostelId(){return hostelId;}
    public String getHostelName(){return hostelName;}
    public String getWardenName(){return wardenName;}
    public String getLocation(){return location;}
    public ArrayList<Room> getRooms(){return rooms;}
    public void setHostelName(String v){hostelName=v;}
    public void setWardenName(String v){wardenName=v;}
    public void setLocation(String v){location=v;}
    public void addRoom(Room r){rooms.add(r);}
    public int getTotalCapacity(){int n=0; for(Room r:rooms)n+=r.getCapacity(); return n;}
    public int getOccupiedBeds(){int n=0; for(Room r:rooms)n+=r.getOccupiedBeds(); return n;}
    public int getVacantBeds(){return getTotalCapacity()-getOccupiedBeds();}
}
