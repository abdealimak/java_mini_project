package com.hostel.model;

public class Student {
    private final String studentId;
    private String name,course,phone,email;
    private int year;

    public Student(String studentId,String name,String course,int year,String phone,String email){
        this.studentId=studentId;this.name=name;this.course=course;this.year=year;this.phone=phone;this.email=email;
    }
    public String getStudentId(){return studentId;}
    public String getName(){return name;}
    public String getCourse(){return course;}
    public int getYear(){return year;}
    public String getPhone(){return phone;}
    public String getEmail(){return email;}
    public void setName(String v){name=v;} public void setCourse(String v){course=v;}
    public void setYear(int v){year=v;} public void setPhone(String v){phone=v;} public void setEmail(String v){email=v;}
}
