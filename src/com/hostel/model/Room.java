package com.hostel.model;

import java.util.Arrays;

public class Room {
    private final String roomNumber, hostelId;
    private int capacity;
    // Array: one position per bed; null means vacant.
    private String[] bedStatus;

    public Room(String roomNumber,String hostelId,int capacity){
        this.roomNumber=roomNumber; this.hostelId=hostelId; setCapacity(capacity);
    }
    public String getRoomNumber(){return roomNumber;}
    public String getHostelId(){return hostelId;}
    public int getCapacity(){return capacity;}
    public String[] getBedStatus(){return bedStatus;}
    public void setCapacity(int capacity){
        if(capacity<=0) throw new IllegalArgumentException("Room capacity must be greater than 0.");
        if(bedStatus!=null && capacity<getOccupiedBeds())
            throw new IllegalArgumentException("New capacity cannot be less than current occupancy.");
        String[] b=new String[capacity];
        if(bedStatus!=null) System.arraycopy(bedStatus,0,b,0,Math.min(bedStatus.length,b.length));
        this.capacity=capacity; this.bedStatus=b;
    }
    public int getOccupiedBeds(){int n=0; for(String s:bedStatus)if(s!=null)n++; return n;}
    public int getVacantBeds(){return capacity-getOccupiedBeds();}
    public boolean isFull(){return getOccupiedBeds()>=capacity;}
    public int allocateBed(String studentId){
        if(isFull()) return -1;
        for(int i=0;i<bedStatus.length;i++) if(bedStatus[i]==null){bedStatus[i]=studentId;return i+1;}
        return -1;
    }
    public boolean removeStudent(String id){
        for(int i=0;i<bedStatus.length;i++) if(id.equals(bedStatus[i])){bedStatus[i]=null;return true;}
        return false;
    }
    public int getBedNumber(String id){
        for(int i=0;i<bedStatus.length;i++)if(id.equals(bedStatus[i]))return i+1;
        return -1;
    }
    public String getBedSummary(){return Arrays.toString(bedStatus);}
}
