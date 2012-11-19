package me.eccentric_nz.plugins.quickdraw;

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
        if (plugin.invites.containsKey(event.getPlayer().getName()) || plugin.challengers.containsKey(event.getPlayer().getName())) {
            event.setTo(event.getFrom());
        }
    }
}
