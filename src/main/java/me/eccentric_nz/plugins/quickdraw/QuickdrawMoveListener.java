package me.eccentric_nz.plugins.quickdraw;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class QuickdrawMoveListener implements Listener {

    private Quickdraw plugin;

    public QuickdrawMoveListener(Quickdraw plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // maybe need plugin.accepted HashMap ?
        if (plugin.accepted.containsKey(event.getPlayer().getName()) || plugin.challengers.containsKey(event.getPlayer().getName())) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
                Location loc = event.getFrom();
                loc.setPitch(event.getTo().getPitch());
                loc.setYaw(event.getTo().getYaw());
                event.getPlayer().teleport(loc);
            }
        }
    }
}
