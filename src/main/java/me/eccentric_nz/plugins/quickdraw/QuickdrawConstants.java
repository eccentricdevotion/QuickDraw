package me.eccentric_nz.plugins.quickdraw;

import org.bukkit.Location;

public class QuickdrawConstants {

    public static String MY_PLUGIN_NAME = "¤6[QuickDraw]¤r ";

    public static int getYaw(Location l) {
        float yaw = l.getYaw();
        int rightangle = 0;
        if (yaw >= 0) {
            yaw = (yaw % 360);
        } else {
            yaw = (360 + (yaw % 360));
        }
        if (yaw >= 315 || yaw < 45) {
            rightangle = 0;
        }
        if (yaw >= 225 && yaw < 315) {
            rightangle = 270;
        }
        if (yaw >= 135 && yaw < 225) {
            rightangle = 180;
        }
        if (yaw >= 45 && yaw < 135) {
            rightangle = 90;
        }
        return rightangle;
    }
}