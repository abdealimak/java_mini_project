package com.hostel;

import com.hostel.gui.HostelFrame;
import com.hostel.service.HostelService;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        HostelService service = new HostelService();
        service.seedDemoData();
        SwingUtilities.invokeLater(() -> new HostelFrame(service).setVisible(true));
    }
}
