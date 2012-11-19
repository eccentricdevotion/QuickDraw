package me.eccentric_nz.plugins.quickdraw;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
                Snowball snowball = (Snowball) event.getDamager();
                LivingEntity shooter = snowball.getShooter();
                if (shooter instanceof Player) {
                    Player hitplayer = (Player) event.getEntity();
                    Player throwplayer = (Player) shooter;
                    if (plugin.invites.containsKey(hitplayer.getName()) || plugin.challengers.containsKey(hitplayer.getName())) {
                        plugin.debug(hitplayer.getName() + " was hit by a snowball thrown by " + throwplayer.getName() + "!");
                        long systime = System.currentTimeMillis();
                        if (plugin.invites.get(hitplayer.getName()).equalsIgnoreCase(throwplayer.getName()) || plugin.challengers.get(hitplayer.getName()).equalsIgnoreCase(throwplayer.getName())) {
                            plugin.hittime.put(throwplayer.getName(), systime);
                        }
                    }
                }
            }
        }
    }
}
