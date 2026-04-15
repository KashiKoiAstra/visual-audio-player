import components.*;
import data.Rainbow;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Rainbow.initialize();
        SwingUtilities.invokeLater(() -> {
            Frame frame = new Frame("Visual Audio Player");
            new events.MainController(frame);
            frame.setVisible(true);
        });
    }
}
