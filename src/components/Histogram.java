package components;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.*;

import data.Rainbow;

public class Histogram extends JPanel {
    private int[] frequencies;
    private final Color[] rainbow = Rainbow.COLORS;
    private final int HUE_Stage = Rainbow.HUE_STAGE;
    private double[] currentHeights;
    private Timer animationTimer;
    // interpolation factor per frame (0..1). Larger -> faster transition.
    private double smoothingFactor = 0.2; // default smoothness

    public Histogram() {
        initialize();
    }
    
    public Histogram(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
        initialize();
    }

    private void initialize() {
        setBackground(Color.BLACK);
        frequencies = new int[0];
    }

    public void update(int[] frequencies) {
        if (frequencies == null) {
            this.frequencies = null;
            this.currentHeights = null;
            stopTimer();
            repaint();
            return;
        }

        if (this.frequencies == null || this.frequencies.length != frequencies.length || this.currentHeights == null) {
            this.frequencies = frequencies;
            this.currentHeights = new double[frequencies.length];
            for (int i = 0; i < frequencies.length; i++) {
                this.currentHeights[i] = frequencies[i];
            }
            stopTimer();
            repaint();
            return;
        }

        this.frequencies = frequencies;
        startTimer();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (frequencies == null || frequencies.length == 0) {
            return;
        }

        g.setColor(Color.CYAN);
        int barWidth = getWidth() / frequencies.length;
        for (int i = 0; i < frequencies.length; i++) {
            int barHeight;
            if (currentHeights != null && i < currentHeights.length) {
                barHeight = (int) Math.round(currentHeights[i]);
            } else {
                barHeight = frequencies[i];
            }
            g.setColor(rainbow[(i + HUE_Stage) % rainbow.length]);
            g.fillRect(i * barWidth, getHeight() - barHeight, barWidth, barHeight + 1);

        }
    }

    private void startTimer() {
        if (animationTimer != null && animationTimer.isRunning()) {
            return;
        }

        // enigmatic magic number: 16
        animationTimer = new javax.swing.Timer(16, e -> {
            boolean anyMoving = false;
            for (int i = 0; i < currentHeights.length; i++) {
                double target = frequencies[i];
                double cur = currentHeights[i];
                double delta = target - cur;
                if (Math.abs(delta) > 0.5) {
                    currentHeights[i] = cur + delta * smoothingFactor;
                    anyMoving = true;
                } else {
                    currentHeights[i] = target;
                }
            }
            repaint();
            if (!anyMoving) {
                stopTimer();
            }
        });
        animationTimer.setCoalesce(true);
        animationTimer.start();
    }

    private void stopTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    public void setSmoothingFactor(double factor) {
        if (factor <= 0) return;
        if (factor > 1) factor = 1;
        this.smoothingFactor = factor;
    }
}