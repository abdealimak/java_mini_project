package com.hostel.model;

import java.time.LocalDateTime;

public class Allotment {
    public enum Status { ACTIVE, CANCELLED, TRANSFERRED }
    private final String allotmentId,studentId;
    private String hostelId,roomNumber;
    private int bedNumber;
    private final LocalDateTime allotmentTime=LocalDateTime.now();
    private LocalDateTime endTime;
    private Status status=Status.ACTIVE;

    public Allotment(String id,String student,String hostel,String room,int bed){
        allotmentId=id;studentId=student;hostelId=hostel;roomNumber=room;bedNumber=bed;
    }
    public String getAllotmentId(){return allotmentId;} public String getStudentId(){return studentId;}
    public String getHostelId(){return hostelId;} public String getRoomNumber(){return roomNumber;}
    public int getBedNumber(){return bedNumber;} public LocalDateTime getAllotmentTime(){return allotmentTime;}
    public LocalDateTime getEndTime(){return endTime;} public Status getStatus(){return status;}
    public void cancel(){status=Status.CANCELLED;endTime=LocalDateTime.now();}
    public void markTransferred(){status=Status.TRANSFERRED;endTime=LocalDateTime.now();}
    public boolean isActive(){return status==Status.ACTIVE;}
}
