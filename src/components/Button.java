package components;

import java.awt.Graphics;

import javax.swing.JButton;

public class Button extends JButton {
    public Button(String text) {
        super(text);
        initialize();
    }

    public Button(String text, int x, int y, int width, int height) {
        super(text);
        setBounds(x, y, width, height);
        // setBorder(null);
        initialize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void initialize() {

    }
}