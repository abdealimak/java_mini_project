package com.hostel.service;

import com.hostel.exception.HostelException;
import com.hostel.model.*;
import com.hostel.util.ValidationUtil;
import java.util.*;

/**
 * Core business-logic layer of the Hostel Room Allocation Management System.
 * Holds all in-memory data (hostels, rooms, students, allotment history)
 * and enforces every business rule: no duplicate IDs, no allocating into
 * a full room, no double allotments, etc.
 */
public class HostelService {

    // ---------- In-memory data stores ----------

    // Ordered lists (preserve insertion order, used for display)
    private final ArrayList<Student> students = new ArrayList<>();
    private final ArrayList<Hostel> hostels = new ArrayList<>();

    // Sequential, append-only log of every allotment ever made
    // (records are never deleted, only marked CANCELLED / TRANSFERRED)
    private final LinkedList<Allotment> allotmentHistory = new LinkedList<>();

    // Fast O(1) lookup by ID, mirrors the lists above
    private final HashMap<String, Room> roomMap = new HashMap<>();
    private final HashMap<String, Student> studentMap = new HashMap<>();
    private final HashMap<String, Hostel> hostelMap = new HashMap<>();

    // Used to auto-generate allotment IDs: ALT0001, ALT0002, ...
    private int allotmentCounter = 1;


    // ---------- Hostel CRUD ----------

    public void addHostel(Hostel hostel) throws HostelException {
        ValidationUtil.text(hostel.getHostelId(), "Hostel ID");
        ValidationUtil.text(hostel.getHostelName(), "Hostel name");
        ValidationUtil.text(hostel.getWardenName(), "Warden name");
        ValidationUtil.text(hostel.getLocation(), "Location");

        if (hostelMap.containsKey(hostel.getHostelId())) {
            throw new HostelException("Hostel ID already exists.");
        }

        hostels.add(hostel);
        hostelMap.put(hostel.getHostelId(), hostel);
    }

    public Hostel getHostel(String hostelId) throws HostelException {
        Hostel hostel = hostelMap.get(hostelId);
        if (hostel == null) {
            throw new HostelException("Hostel not found.");
        }
        return hostel;
    }

    public List<Hostel> getAllHostels() {
        return new ArrayList<>(hostels);
    }


    // ---------- Room CRUD ----------

    public void addRoom(Room room) throws HostelException {
        ValidationUtil.text(room.getRoomNumber(), "Room number");
        ValidationUtil.text(room.getHostelId(), "Hostel ID");
        ValidationUtil.positive(room.getCapacity(), "Capacity");

        // Confirm the parent hostel actually exists before attaching the room
        Hostel hostel = getHostel(room.getHostelId());

        if (roomMap.containsKey(room.getRoomNumber())) {
            throw new HostelException("Room number already exists.");
        }

        roomMap.put(room.getRoomNumber(), room);
        hostel.addRoom(room);
    }

    public Room getRoom(String roomNumber) throws HostelException {
        Room room = roomMap.get(roomNumber);
        if (room == null) {
            throw new HostelException("Room not found.");
        }
        return room;
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(roomMap.values());
    }


    // ---------- Student CRUD ----------

    public void addStudent(Student student) throws HostelException {
        ValidationUtil.text(student.getStudentId(), "Student ID");
        ValidationUtil.text(student.getName(), "Student name");
        ValidationUtil.text(student.getCourse(), "Course");
        ValidationUtil.positive(student.getYear(), "Year");
        ValidationUtil.text(student.getPhone(), "Phone");
        ValidationUtil.email(student.getEmail());

        if (studentMap.containsKey(student.getStudentId())) {
            throw new HostelException("Student ID already exists.");
        }

        students.add(student);
        studentMap.put(student.getStudentId(), student);
    }

    public Student getStudent(String studentId) throws HostelException {
        Student student = studentMap.get(studentId);
        if (student == null) {
            throw new HostelException("Student not found.");
        }
        return student;
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }


    // ---------- Allotment operations ----------

    /**
     * Allocates the first free bed in the given room to the given student.
     * Fails if the student already has an active allotment, or the room is full.
     */
    public Allotment allocateRoom(String studentId, String roomNumber) throws HostelException {
        Student student = getStudent(studentId);
        Room room = getRoom(roomNumber);

        if (findActiveAllotmentByStudent(studentId) != null) {
            throw new HostelException("Student already has an active room allotment.");
        }
        if (room.isFull()) {
            throw new HostelException("Room is full. No vacant bed available.");
        }

        int bedNumber = room.allocateBed(student.getStudentId());
        if (bedNumber < 0) {
            throw new HostelException("Unable to allocate a bed.");
        }

        Allotment allotment = new Allotment(
                nextAllotmentId(),
                studentId,
                room.getHostelId(),
                room.getRoomNumber(),
                bedNumber
        );
        allotmentHistory.add(allotment);
        return allotment;
    }

    /**
     * Moves a student from their current room to a new room.
     * The new bed is allocated FIRST, and the old bed is only freed
     * after that succeeds — so a failed transfer never leaves the
     * student without any room at all.
     */
    public Allotment transferStudent(String studentId, String newRoomNumber) throws HostelException {
        Allotment oldAllotment = findActiveAllotmentByStudent(studentId);
        if (oldAllotment == null) {
            throw new HostelException("Student does not have an active allotment.");
        }

        Room oldRoom = getRoom(oldAllotment.getRoomNumber());
        Room newRoom = getRoom(newRoomNumber);

        if (oldRoom.getRoomNumber().equals(newRoom.getRoomNumber())) {
            throw new HostelException("Student is already in this room.");
        }
        if (newRoom.isFull()) {
            throw new HostelException("New room is full. Transfer cannot be completed.");
        }

        int bedNumber = newRoom.allocateBed(studentId);
        if (bedNumber < 0) {
            throw new HostelException("Unable to allocate a new bed.");
        }

        // Only free the old bed once the new one is secured
        oldRoom.removeStudent(studentId);
        oldAllotment.markTransferred();

        Allotment newAllotment = new Allotment(
                nextAllotmentId(),
                studentId,
                newRoom.getHostelId(),
                newRoom.getRoomNumber(),
                bedNumber
        );
        allotmentHistory.add(newAllotment);
        return newAllotment;
    }

    /** Cancels a student's active allotment and frees their bed. */
    public void cancelAllotment(String studentId) throws HostelException {
        Allotment allotment = findActiveAllotmentByStudent(studentId);
        if (allotment == null) {
            throw new HostelException("Student does not have an active allotment.");
        }

        Room room = getRoom(allotment.getRoomNumber());
        if (!room.removeStudent(studentId)) {
            throw new HostelException("Student was not found in the allotted room.");
        }

        allotment.cancel();
    }

    /** Returns the student's current ACTIVE allotment, or null if they have none. */
    public Allotment findActiveAllotmentByStudent(String studentId) {
        for (Allotment allotment : allotmentHistory) {
            if (allotment.getStudentId().equals(studentId) && allotment.isActive()) {
                return allotment;
            }
        }
        return null;
    }

    /** Formats the next allotment ID as e.g. ALT0001, then increments the counter. */
    private String nextAllotmentId() {
        return "ALT" + String.format("%04d", allotmentCounter++);
    }


    // ---------- Search ----------

    public List<Student> searchStudents(String keyword) {
        String key = (keyword == null) ? "" : keyword.toLowerCase();
        List<Student> results = new ArrayList<>();

        for (Student student : students) {
            String searchable = (student.getStudentId() + " " + student.getName() + " " + student.getCourse())
                    .toLowerCase();
            if (searchable.contains(key)) {
                results.add(student);
            }
        }
        return results;
    }

    public List<Room> searchRooms(String keyword) {
        String key = (keyword == null) ? "" : keyword.toLowerCase();
        List<Room> results = new ArrayList<>();

        for (Room room : roomMap.values()) {
            String searchable = (room.getRoomNumber() + " " + room.getHostelId()).toLowerCase();
            if (searchable.contains(key)) {
                results.add(room);
            }
        }
        return results;
    }


    // ---------- Sorting ----------

    public List<Room> getRoomsSortedByNumber() {
        List<Room> rooms = getAllRooms();
        rooms.sort(Comparator.comparing(Room::getRoomNumber));
        return rooms;
    }

    public List<Room> getRoomsSortedByOccupancy() {
        List<Room> rooms = getAllRooms();
        rooms.sort(
                Comparator.comparingInt(Room::getOccupiedBeds)
                        .thenComparing(Room::getRoomNumber)
        );
        return rooms;
    }

    /** TreeMap keeps room records ordered by occupancy first, then room number. */
    public TreeMap<String, Room> getRoomsTreeMap() {
        TreeMap<String, Room> sortedRooms = new TreeMap<>((roomNumberA, roomNumberB) -> {
            int occupancyCompare = Integer.compare(
                    roomMap.get(roomNumberA).getOccupiedBeds(),
                    roomMap.get(roomNumberB).getOccupiedBeds()
            );
            return (occupancyCompare != 0) ? occupancyCompare : roomNumberA.compareTo(roomNumberB);
        });
        sortedRooms.putAll(roomMap);
        return sortedRooms;
    }


    // ---------- Vacancy ----------

    public int getTotalVacantBeds() {
        int total = 0;
        for (Room room : roomMap.values()) {
            total += room.getVacantBeds();
        }
        return total;
    }

    public List<Room> getRoomsWithVacancy() {
        List<Room> vacantRooms = new ArrayList<>();
        for (Room room : getAllRooms()) {
            if (room.getVacantBeds() > 0) {
                vacantRooms.add(room);
            }
        }
        vacantRooms.sort(Comparator.comparing(Room::getRoomNumber));
        return vacantRooms;
    }

    public List<Allotment> getAllotmentHistory() {
        return new ArrayList<>(allotmentHistory);
    }


    // ---------- Reporting ----------

    public String generateReport() {
        StringBuilder report = new StringBuilder("========== HOSTEL OCCUPANCY REPORT ==========\n");

        report.append("Hostels: ").append(hostels.size())
                .append("\nStudents: ").append(students.size())
                .append("\nRooms: ").append(roomMap.size())
                .append("\nVacant Beds: ").append(getTotalVacantBeds())
                .append("\n\n");

        for (Hostel hostel : hostels) {
            report.append(hostel.getHostelId()).append(" | ")
                    .append(hostel.getHostelName()).append(" | Capacity: ")
                    .append(hostel.getTotalCapacity()).append(" | Occupied: ")
                    .append(hostel.getOccupiedBeds()).append(" | Vacant: ")
                    .append(hostel.getVacantBeds()).append("\n");
        }

        return report.toString();
    }


    // ---------- Demo data ----------

    /** Pre-populates the system with sample hostels, rooms, students, and allotments. */
    public void seedDemoData() {
        try {
            addHostel(new Hostel("H001", "Boys Hostel A", "Mr. Sharma", "North Campus"));
            addHostel(new Hostel("H002", "Boys Hostel B", "Mr. Patil", "South Campus"));

            addRoom(new Room("A101", "H001", 3));
            addRoom(new Room("A102", "H001", 2));
            addRoom(new Room("A103", "H001", 4));
            addRoom(new Room("B201", "H002", 3));
            addRoom(new Room("B202", "H002", 2));

            addStudent(new Student("S001", "Aarav Sharma", "CSE", 2, "9000000001", "aarav@example.com"));
            addStudent(new Student("S002", "Vihaan Patil", "CSE", 3, "9000000002", "vihaan@example.com"));
            addStudent(new Student("S003", "Rohan Mehta", "IT", 2, "9000000003", "rohan@example.com"));
            addStudent(new Student("S004", "Kabir Shah", "ECE", 1, "9000000004", "kabir@example.com"));

            allocateRoom("S001", "A101");
            allocateRoom("S002", "A101");
            allocateRoom("S003", "B201");
        } catch (HostelException ignored) {
            // Seed data is hardcoded and known-valid; safe to ignore here only.
        }
    }
}