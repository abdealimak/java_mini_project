package com.hostel.gui;

import com.hostel.exception.HostelException;
import com.hostel.model.*;
import com.hostel.service.HostelService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Comparator;

public class HostelFrame extends JFrame {
    private final HostelService service;
    private final DefaultTableModel hostelModel = model("Hostel ID", "Hostel Name", "Warden", "Location", "Capacity", "Occupied", "Vacant");
    private final DefaultTableModel roomModel = model("Room", "Hostel", "Capacity", "Occupied", "Vacant", "Beds");
    private final DefaultTableModel studentModel = model("Student ID", "Name", "Course", "Year", "Phone", "Email");
    private final DefaultTableModel allotmentModel = model("Allotment", "Student", "Hostel", "Room", "Bed", "Status", "Time");
    
    private final JTable hostelTable = createTable(hostelModel);
    private final JTable roomTable = createTable(roomModel);
    private final JTable studentTable = createTable(studentModel);
    private final JTable allotmentTable = createTable(allotmentModel);
    
    private final JTextArea output = new JTextArea();

    public HostelFrame(HostelService service) {
        this.service = service;
        
        // Apply modern Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        setTitle("Hostel Room Allocation Management System");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Main Container
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(245, 246, 250));
        setContentPane(root);
        
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Hostel Room Allocation Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Sidebar Panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel actionLabel = new JLabel("ACTIONS");
        actionLabel.setForeground(new Color(149, 165, 166));
        actionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        actionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(actionLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[] actions = {"Add Hostel", "Add Room", "Add Student", "Allocate Room", "Transfer Student", "Cancel Allotment"};
        Runnable[] actionListeners = {this::addHostel, this::addRoom, this::addStudent, this::allocate, this::transfer, this::cancel};
        
        for (int i = 0; i < actions.length; i++) {
            sidebar.add(createSidebarButton(actions[i], actionListeners[i]));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel reportsLabel = new JLabel("REPORTS & VIEWS");
        reportsLabel.setForeground(new Color(149, 165, 166));
        reportsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(reportsLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[] reports = {"Vacancy Check", "Allotment History", "Generate Report", "Refresh All"};
        Runnable[] reportListeners = {this::vacancy, this::history, this::report, this::refresh};
        
        for (int i = 0; i < reports.length; i++) {
            sidebar.add(createSidebarButton(reports[i], reportListeners[i]));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        root.add(sidebar, BorderLayout.WEST);

        // Center Area (Tabs + Output)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        centerPanel.setOpaque(false);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("Hostels", createScrollPane(hostelTable));
        tabs.addTab("Rooms", createScrollPane(roomTable));
        tabs.addTab("Students", createScrollPane(studentTable));
        tabs.addTab("Allotments", createScrollPane(allotmentTable));
        centerPanel.add(tabs, BorderLayout.CENTER);
        
        // Output Console
        output.setEditable(false);
        output.setRows(7);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        output.setBackground(new Color(236, 240, 241));
        output.setForeground(new Color(44, 62, 80));
        output.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane outputScroll = new JScrollPane(output);
        TitledBorder tb = BorderFactory.createTitledBorder("System Console");
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        tb.setTitleColor(new Color(52, 73, 94));
        outputScroll.setBorder(BorderFactory.createCompoundBorder(tb, BorderFactory.createLineBorder(new Color(189, 195, 199))));
        centerPanel.add(outputScroll, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);
        
        refresh();
    }

    private JButton createSidebarButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 38));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(223, 230, 233));
        table.setShowVerticalLines(false);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(189, 195, 199));
        header.setForeground(new Color(45, 52, 54));
        header.setPreferredSize(new Dimension(100, 35));
        
        return table;
    }

    private JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(223, 230, 233)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private static DefaultTableModel model(String... c) {
        return new DefaultTableModel(c, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private JPanel form(Object... v) {
        JPanel p = new JPanel(new GridLayout(v.length / 2, 2, 10, 10));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));
        for (int i = 0; i < v.length; i += 2) {
            JLabel label = new JLabel(v[i] + ":");
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            p.add(label);
            
            JComponent comp = (JComponent) v[i + 1];
            if (comp instanceof JTextField) {
                comp.setPreferredSize(new Dimension(220, 32));
                ((JTextField) comp).setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
            p.add(comp);
        }
        return p;
    }

    private boolean ok(JPanel p, String t) {
        return JOptionPane.showConfirmDialog(this, p, t, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private void success(String s) {
        output.setText(s);
        refreshTables();
    }

    private void error(String s) {
        JOptionPane.showMessageDialog(this, s, "Operation Failed", JOptionPane.ERROR_MESSAGE);
    }

    private void refresh() {
        refreshTables();
        output.setText("System ready. Vacant beds: " + service.getTotalVacantBeds());
    }

    private void refreshTables() {
        hostelModel.setRowCount(0);
        for (Hostel h : service.getAllHostels()) {
            hostelModel.addRow(new Object[]{h.getHostelId(), h.getHostelName(), h.getWardenName(), h.getLocation(), h.getTotalCapacity(), h.getOccupiedBeds(), h.getVacantBeds()});
        }
        roomModel.setRowCount(0);
        for (Room r : service.getRoomsSortedByNumber()) {
            roomModel.addRow(new Object[]{r.getRoomNumber(), r.getHostelId(), r.getCapacity(), r.getOccupiedBeds(), r.getVacantBeds(), r.getBedSummary()});
        }
        studentModel.setRowCount(0);
        service.getAllStudents().stream().sorted(Comparator.comparing(Student::getStudentId)).forEach(s -> studentModel.addRow(new Object[]{s.getStudentId(), s.getName(), s.getCourse(), s.getYear(), s.getPhone(), s.getEmail()}));
        allotmentModel.setRowCount(0);
        for (Allotment a : service.getAllotmentHistory()) {
            allotmentModel.addRow(new Object[]{a.getAllotmentId(), a.getStudentId(), a.getHostelId(), a.getRoomNumber(), a.getBedNumber(), a.getStatus(), a.getAllotmentTime()});
        }
    }

    private void addHostel() {
        JTextField id = new JTextField(), name = new JTextField(), warden = new JTextField(), loc = new JTextField();
        if (ok(form("Hostel ID", id, "Hostel Name", name, "Warden", warden, "Location", loc), "Add Hostel")) {
            try {
                service.addHostel(new Hostel(id.getText().trim(), name.getText().trim(), warden.getText().trim(), loc.getText().trim()));
                success("Hostel added successfully.");
            } catch (HostelException e) {
                error(e.getMessage());
            }
        }
    }

    private void addRoom() {
        JTextField id = new JTextField(), hid = new JTextField(), cap = new JTextField();
        if (ok(form("Room Number", id, "Hostel ID", hid, "Capacity", cap), "Add Room")) {
            try {
                service.addRoom(new Room(id.getText().trim(), hid.getText().trim(), Integer.parseInt(cap.getText().trim())));
                success("Room added successfully.");
            } catch (NumberFormatException e) {
                error("Capacity must be an integer.");
            } catch (IllegalArgumentException | HostelException e) {
                error(e.getMessage());
            }
        }
    }

    private void addStudent() {
        JTextField id = new JTextField(), name = new JTextField(), course = new JTextField(), year = new JTextField(), phone = new JTextField(), email = new JTextField();
        if (ok(form("Student ID", id, "Name", name, "Course", course, "Year", year, "Phone", phone, "Email", email), "Add Student")) {
            try {
                service.addStudent(new Student(id.getText().trim(), name.getText().trim(), course.getText().trim(), Integer.parseInt(year.getText().trim()), phone.getText().trim(), email.getText().trim()));
                success("Student added successfully.");
            } catch (NumberFormatException e) {
                error("Year must be an integer.");
            } catch (HostelException e) {
                error(e.getMessage());
            }
        }
    }

    private void allocate() {
        JTextField sid = new JTextField(), room = new JTextField();
        if (ok(form("Student ID", sid, "Room Number", room), "Allocate Room")) {
            try {
                Allotment a = service.allocateRoom(sid.getText().trim(), room.getText().trim());
                success("Room allocated. Allotment ID: " + a.getAllotmentId() + " | Bed: " + a.getBedNumber());
            } catch (HostelException e) {
                error(e.getMessage());
            }
        }
    }

    private void transfer() {
        JTextField sid = new JTextField(), room = new JTextField();
        if (ok(form("Student ID", sid, "New Room", room), "Transfer Student")) {
            try {
                Allotment a = service.transferStudent(sid.getText().trim(), room.getText().trim());
                success("Transfer completed. New allotment: " + a.getAllotmentId() + " | Bed: " + a.getBedNumber());
            } catch (HostelException e) {
                error(e.getMessage());
            }
        }
    }

    private void cancel() {
        String sid = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (sid == null || sid.trim().isEmpty()) return;
        try {
            service.cancelAllotment(sid.trim());
            success("Allotment cancelled successfully.");
        } catch (HostelException e) {
            error(e.getMessage());
        }
    }

    private void vacancy() {
        StringBuilder b = new StringBuilder("========== VACANCY CHECK ==========\nTotal vacant beds: " + service.getTotalVacantBeds() + "\n\n");
        for (Room r : service.getRoomsWithVacancy()) {
            b.append(r.getRoomNumber()).append(" | Hostel ").append(r.getHostelId())
             .append(" | Vacant: ").append(r.getVacantBeds()).append("/").append(r.getCapacity()).append("\n");
        }
        output.setText(b.toString());
    }

    private void history() {
        StringBuilder b = new StringBuilder("========== ALLOTMENT / TRANSFER HISTORY ==========\n");
        for (Allotment a : service.getAllotmentHistory()) {
            b.append(a.getAllotmentId()).append(" | ").append(a.getStudentId())
             .append(" | ").append(a.getHostelId()).append(" / ").append(a.getRoomNumber())
             .append(" / Bed ").append(a.getBedNumber()).append(" | ").append(a.getStatus())
             .append("\n");
        }
        output.setText(b.toString());
    }

    private void report() {
        output.setText(service.generateReport());
    }
}
