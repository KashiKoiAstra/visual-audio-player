package components;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

public class Frame extends JFrame {

    public Button playButton;
    public Button pauseButton;
    public Button stopButton;
    public Button openButton;
    public Button testButton;

    public Histogram histogram;

    public Frame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        initialize();

    }

    private void initialize() {
        histogram = new Histogram(0, 0, getWidth(), getHeight());

        histogram.add(playButton = new Button("Play", 50, 470, 80, 30));
        histogram.add(pauseButton = new Button("Pause", 150, 470, 80, 30));
        histogram.add(stopButton = new Button("Stop", 250, 470, 80, 30));
        histogram.add(openButton = new Button("Open", 350, 470, 80, 30));
        histogram.add(testButton = new Button("Test", 450, 470, 80, 30));

        add(histogram);

        histogram.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getY() > 60) {
                    playButton.setVisible(false);
                    pauseButton.setVisible(false);
                    stopButton.setVisible(false);
                    openButton.setVisible(false);
                    testButton.setVisible(false);
                } else {
                    playButton.setVisible(true);
                    pauseButton.setVisible(true);
                    stopButton.setVisible(true);
                    openButton.setVisible(true);
                    testButton.setVisible(true);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // nothing to do
            }
        });
    }
}