package data;

import java.awt.Color;

public class Rainbow {
    public static int HUE_STAGE = 360;
    public static Color[] COLORS = new Color[HUE_STAGE];

    static {
        for (int i = 0; i < HUE_STAGE; i++) {
            float hue = i / (float) HUE_STAGE;
            COLORS[i] = Color.getHSBColor(hue, 1.0f, 1.0f);
        }
    }

    public static void initialize() {
        // static initialize
    }
}
