package me.eccentric_nz.plugins.quickdraw;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class QuickdrawHitListener implements Listener {

    private Quickdraw plugin;

    public QuickdrawHitListener(Quickdraw plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Snowball) {
                Player p = (Player) event.getEntity();
                if (plugin.invites.containsKey(p.getName())) {
                    long systime = System.currentTimeMillis();
                    plugin.hittime.put(p.getName(), systime);
                }
            }
        }
    }
}
