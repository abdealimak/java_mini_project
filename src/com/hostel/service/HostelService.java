package com.hostel.service;

import com.hostel.exception.HostelException;
import com.hostel.model.*;
import com.hostel.util.ValidationUtil;
import java.util.*;

public class HostelService {
    private final ArrayList<Student> students=new ArrayList<>();
    private final ArrayList<Hostel> hostels=new ArrayList<>();
    private final LinkedList<Allotment> allotmentHistory=new LinkedList<>();
    private final HashMap<String,Room> roomMap=new HashMap<>();
    private final HashMap<String,Student> studentMap=new HashMap<>();
    private final HashMap<String,Hostel> hostelMap=new HashMap<>();
    private int allotmentCounter=1;

    public void addHostel(Hostel h)throws HostelException{
        ValidationUtil.text(h.getHostelId(),"Hostel ID"); ValidationUtil.text(h.getHostelName(),"Hostel name");
        ValidationUtil.text(h.getWardenName(),"Warden name"); ValidationUtil.text(h.getLocation(),"Location");
        if(hostelMap.containsKey(h.getHostelId()))throw new HostelException("Hostel ID already exists.");
        hostels.add(h);hostelMap.put(h.getHostelId(),h);
    }
    public Hostel getHostel(String id)throws HostelException{
        Hostel h=hostelMap.get(id);if(h==null)throw new HostelException("Hostel not found.");return h;
    }
    public List<Hostel> getAllHostels(){return new ArrayList<>(hostels);}

    public void addRoom(Room r)throws HostelException{
        ValidationUtil.text(r.getRoomNumber(),"Room number"); ValidationUtil.text(r.getHostelId(),"Hostel ID");
        ValidationUtil.positive(r.getCapacity(),"Capacity"); Hostel h=getHostel(r.getHostelId());
        if(roomMap.containsKey(r.getRoomNumber()))throw new HostelException("Room number already exists.");
        roomMap.put(r.getRoomNumber(),r);h.addRoom(r);
    }
    public Room getRoom(String id)throws HostelException{
        Room r=roomMap.get(id);if(r==null)throw new HostelException("Room not found.");return r;
    }
    public List<Room> getAllRooms(){return new ArrayList<>(roomMap.values());}

    public void addStudent(Student s)throws HostelException{
        ValidationUtil.text(s.getStudentId(),"Student ID");ValidationUtil.text(s.getName(),"Student name");
        ValidationUtil.text(s.getCourse(),"Course");ValidationUtil.positive(s.getYear(),"Year");
        ValidationUtil.text(s.getPhone(),"Phone");ValidationUtil.email(s.getEmail());
        if(studentMap.containsKey(s.getStudentId()))throw new HostelException("Student ID already exists.");
        students.add(s);studentMap.put(s.getStudentId(),s);
    }
    public Student getStudent(String id)throws HostelException{
        Student s=studentMap.get(id);if(s==null)throw new HostelException("Student not found.");return s;
    }
    public List<Student> getAllStudents(){return new ArrayList<>(students);}

    public Allotment allocateRoom(String studentId,String roomNo)throws HostelException{
        Student s=getStudent(studentId); Room r=getRoom(roomNo);
        if(findActiveAllotmentByStudent(studentId)!=null)throw new HostelException("Student already has an active room allotment.");
        if(r.isFull())throw new HostelException("Room is full. No vacant bed available.");
        int bed=r.allocateBed(s.getStudentId()); if(bed<0)throw new HostelException("Unable to allocate a bed.");
        Allotment a=new Allotment("ALT"+String.format("%04d",allotmentCounter++),studentId,r.getHostelId(),r.getRoomNumber(),bed);
        allotmentHistory.add(a);return a;
    }

    public Allotment transferStudent(String studentId,String newRoomNo)throws HostelException{
        Allotment old=findActiveAllotmentByStudent(studentId);if(old==null)throw new HostelException("Student does not have an active allotment.");
        Room oldRoom=getRoom(old.getRoomNumber()), newRoom=getRoom(newRoomNo);
        if(oldRoom.getRoomNumber().equals(newRoom.getRoomNumber()))throw new HostelException("Student is already in this room.");
        if(newRoom.isFull())throw new HostelException("New room is full. Transfer cannot be completed.");
        int bed=newRoom.allocateBed(studentId);if(bed<0)throw new HostelException("Unable to allocate a new bed.");
        oldRoom.removeStudent(studentId);old.markTransferred();
        Allotment a=new Allotment("ALT"+String.format("%04d",allotmentCounter++),studentId,newRoom.getHostelId(),newRoom.getRoomNumber(),bed);
        allotmentHistory.add(a);return a;
    }

    public void cancelAllotment(String studentId)throws HostelException{
        Allotment a=findActiveAllotmentByStudent(studentId);if(a==null)throw new HostelException("Student does not have an active allotment.");
        Room r=getRoom(a.getRoomNumber());if(!r.removeStudent(studentId))throw new HostelException("Student was not found in the allotted room.");
        a.cancel();
    }

    public Allotment findActiveAllotmentByStudent(String id){
        for(Allotment a:allotmentHistory)if(a.getStudentId().equals(id)&&a.isActive())return a;return null;
    }

    public List<Student> searchStudents(String key){
        key=key==null?"":key.toLowerCase();List<Student> out=new ArrayList<>();
        for(Student s:students)if((s.getStudentId()+" "+s.getName()+" "+s.getCourse()).toLowerCase().contains(key))out.add(s);return out;
    }
    public List<Room> searchRooms(String key){
        key=key==null?"":key.toLowerCase();List<Room> out=new ArrayList<>();
        for(Room r:roomMap.values())if((r.getRoomNumber()+" "+r.getHostelId()).toLowerCase().contains(key))out.add(r);return out;
    }

    public List<Room> getRoomsSortedByNumber(){
        List<Room> x=getAllRooms();x.sort(Comparator.comparing(Room::getRoomNumber));return x;
    }
    public List<Room> getRoomsSortedByOccupancy(){
        List<Room> x=getAllRooms();x.sort(Comparator.comparingInt(Room::getOccupiedBeds).thenComparing(Room::getRoomNumber));return x;
    }
    // TreeMap: room records ordered by occupancy, then room number.
    public TreeMap<String,Room> getRoomsTreeMap(){
        TreeMap<String,Room> x=new TreeMap<>((a,b)->{
            int c=Integer.compare(roomMap.get(a).getOccupiedBeds(),roomMap.get(b).getOccupiedBeds());
            return c!=0?c:a.compareTo(b);
        });x.putAll(roomMap);return x;
    }
    public int getTotalVacantBeds(){int n=0;for(Room r:roomMap.values())n+=r.getVacantBeds();return n;}
    public List<Room> getRoomsWithVacancy(){
        List<Room>x=new ArrayList<>();for(Room r:getAllRooms())if(r.getVacantBeds()>0)x.add(r);
        x.sort(Comparator.comparing(Room::getRoomNumber));return x;
    }
    public List<Allotment> getAllotmentHistory(){return new ArrayList<>(allotmentHistory);}

    public String generateReport(){
        StringBuilder b=new StringBuilder("========== HOSTEL OCCUPANCY REPORT ==========\n");
        b.append("Hostels: ").append(hostels.size()).append("\nStudents: ").append(students.size())
         .append("\nRooms: ").append(roomMap.size()).append("\nVacant Beds: ").append(getTotalVacantBeds()).append("\n\n");
        for(Hostel h:hostels)b.append(h.getHostelId()).append(" | ").append(h.getHostelName())
            .append(" | Capacity: ").append(h.getTotalCapacity()).append(" | Occupied: ")
            .append(h.getOccupiedBeds()).append(" | Vacant: ").append(h.getVacantBeds()).append("\n");
        return b.toString();
    }

    public void seedDemoData(){
        try{
            addHostel(new Hostel("H001","Boys Hostel A","Mr. Sharma","North Campus"));
            addHostel(new Hostel("H002","Boys Hostel B","Mr. Patil","South Campus"));
            addRoom(new Room("A101","H001",3));addRoom(new Room("A102","H001",2));addRoom(new Room("A103","H001",4));
            addRoom(new Room("B201","H002",3));addRoom(new Room("B202","H002",2));
            addStudent(new Student("S001","Aarav Sharma","CSE",2,"9000000001","aarav@example.com"));
            addStudent(new Student("S002","Vihaan Patil","CSE",3,"9000000002","vihaan@example.com"));
            addStudent(new Student("S003","Rohan Mehta","IT",2,"9000000003","rohan@example.com"));
            addStudent(new Student("S004","Kabir Shah","ECE",1,"9000000004","kabir@example.com"));
            allocateRoom("S001","A101");allocateRoom("S002","A101");allocateRoom("S003","B201");
        }catch(HostelException ignored){}
    }
}
