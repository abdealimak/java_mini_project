# 🏨 Hostel Room Allocation Management System

> **Java Semester III Mini Project**
> A comprehensive, desktop-based graphical application built to streamline and digitize the process of managing college hostels, tracking room capacities, and handling student allotments.

---

## ✨ Key Features
- **Hostel & Room Management:** Add multiple hostels and define rooms with custom capacities.
- **Student Registration:** Register students with full validation (ID, Email, Year).
- **Smart Room Allocation:** Automatically finds vacant beds in a specified room and assigns them. Rejects allocation if a room is full.
- **Room Transfers & Cancellations:** Seamlessly transfer a student from one room to another or cancel their allotment completely.
- **Vacancy Tracking:** Instantly view all rooms that have vacant beds.
- **Allotment History:** A complete log tracking every allocation, transfer, and cancellation over time.
- **Dynamic Sorting & Reporting:** Rooms are sorted automatically by occupancy using TreeMaps, and a full system console logs real-time reports.

---

## 🛠️ Technology Stack & Java Concepts

This project was built purely in Java and strictly adheres to Object-Oriented principles, leveraging the **Java Collections Framework (JCF)** for high performance:

*   **GUI Framework:** Java Swing (with a modernized aesthetic).
*   **Encapsulation & OOP:** Strong use of Constructors, private fields, and standard POJO models (`Student`, `Room`, `Hostel`, `Allotment`).
*   **Data Structures (JCF):**
    *   `ArrayList`: Standard lists for generic storage.
    *   `HashMap`: Used for instant, `O(1)` time-complexity lookups (e.g., finding a room by its ID).
    *   `LinkedList`: Maintains a chronological ledger of the allotment history.
    *   `TreeMap`: Custom comparators automatically sort rooms based on real-time occupancy.
    *   `Arrays`: Tracks individual bed statuses within a specific room.
*   **Custom Exception Handling:** Custom checked exceptions (`HostelException`) safely catch business logic violations (e.g. over-capacity).

---

## 📂 Project Structure

```text
src/com/hostel/
├── exception/
│   └── HostelException.java      # Custom error handling
├── gui/
│   └── HostelFrame.java          # Swing User Interface
├── model/
│   ├── Allotment.java            # Allotment state and history
│   ├── Hostel.java               # Hostel entity
│   ├── Room.java                 # Room entity (handles beds)
│   └── Student.java              # Student entity
├── service/
│   └── HostelService.java        # Core business logic & data structures
├── util/
│   └── ValidationUtil.java       # Input validation logic (Regex, null checks)
└── Main.java                     # Application Entry Point
```

---

## 🚀 How to Run

### Prerequisites
- **Java Development Kit (JDK) 8 or higher** installed on your system.

### Compilation and Execution

Open your terminal or command prompt, navigate to the root directory of the project, and run the following commands:

**For macOS / Linux:**
```bash
# 1. Compile all Java files into an 'out' directory
javac -d out $(find src -name "*.java")

# 2. Run the compiled application
java -cp out com.hostel.Main
```

**For Windows (Command Prompt / PowerShell):**
```cmd
# 1. Compile all Java files
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt

# 2. Run the application
java -cp out com.hostel.Main
```

> **Note:** The application automatically loads a set of seed demo data on startup, so you can immediately test the features without manually typing entries!
