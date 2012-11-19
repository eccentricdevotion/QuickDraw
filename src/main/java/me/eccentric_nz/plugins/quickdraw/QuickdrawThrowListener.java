package me.eccentric_nz.plugins.quickdraw;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class QuickdrawThrowListener implements Listener {

    private Quickdraw plugin;

    public QuickdrawThrowListener(Quickdraw plugin) {
        this.plugin = plugin;
    }

    @EventHandler
		public void onProjectileThrownEvent(ProjectileLaunchEvent event) {
  			if(event.getEntity() instanceof Snowball) {
    		//Snowball was thrown
                Snowball snowball = (Snowball) event.getEntity();
                LivingEntity shooter = snowball.getShooter();
                if (shooter instanceof Player) {
                    Player p = (Player) shooter;
                    if (plugin.challengers.containsKey(p.getName()) || plugin.invites.containsKey(p.getName()) ) {
                        // a quick draw challenge has started
                        plugin.debug("A quick draw challenge has started...");
                    }
                }
  		}
	}
}
