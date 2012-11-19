package me.eccentric_nz.plugins.quickdraw;

import org.bukkit.Location;
import org.bukkit.World;

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
    public static String[] insults = {"they didn't have manners enough to carry guts to a bear",
        "they couldn't hit a bull's rump with a handful of banjos",
        "they were as drunk as a fiddler's clerk",
        "they were as nervous as a long-tailed cat in a room full of rocking chairs",
        "they'd been in the desert so long, they knew all the lizards by their first names",
        "they ain't fit to shoot at when you want to unload and clean yo' gun",
        "their mustache smelled like a mildewed saddle blanket after it had been rid on a soreback hoss three hundred miles in August",
        "they was grittin' their teeth like they could bite the sights off a six-gun",
        "they were as as dead as a can of corned beef",
        "they were as welcome as a rattlesnake at a square dance",
        "they'd last as long as a pint of whiskey in a five-handed poker game",
        "they're as popular as a wet dog at a parlor social",
        "they move as slow as a crippled turtle",
        "their brain cavity wouldn't make a drinkin' cup for a canary",
        "they don't know dung from wild honey",
        "they don't know any more about it than a hog does a sidesaddle",
        "they can't tell skunks from house cats",
        "they had a ten dollar Stetson on a five-cent head",
        "they didn't have nuthin' under their hat but hair",
        "they wuz crazy enough to eat the devil with horns on",
        "they couldn't track a bed-wagon through a bog hole"};

    public static boolean inLocation(Location a, Location b) {
        int xMin = Math.min(a.getBlockX(), a.getBlockX() - 10);
        int xMax = Math.max(a.getBlockX(), a.getBlockX() + 10);
        int zMin = Math.min(a.getBlockZ(), a.getBlockZ());
        int zMax = Math.max(a.getBlockZ(), a.getBlockZ());
        World world = a.getWorld();
        if (b.getWorld() != world) {
            return false;
        }
        if (b.getBlockX() < xMin) {
            return false;
        }
        if (b.getBlockX() > xMax) {
            return false;
        }
        if (b.getBlockZ() < zMin) {
            return false;
        }
        if (b.getBlockZ() > zMax) {
            return false;
        }
        return true;
    }
}