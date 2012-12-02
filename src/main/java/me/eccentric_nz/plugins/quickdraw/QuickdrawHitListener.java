package me.eccentric_nz.plugins.quickdraw;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;

public class QuickdrawHitListener implements Listener {

    private Quickdraw plugin;
    QuickdrawDatabase service = QuickdrawDatabase.getInstance();

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
                    String hitNameStr = hitplayer.getName();
                    String throwerNameStr = throwplayer.getName();
                    if (plugin.drawtime.containsKey(throwerNameStr)) {
                        plugin.debug(hitNameStr + " was hit by a snowball thrown by " + throwerNameStr + "!");
                        long nanotime = System.nanoTime();
                        plugin.hittime.put(throwerNameStr, nanotime);
                        long time = plugin.hittime.get(throwerNameStr) - plugin.drawtime.get(throwerNameStr);
                        plugin.quicktime.put(throwerNameStr, time);
                        try {
                            Connection connection = service.getConnection();
                            Statement statement = connection.createStatement();
                            String queryTime = "INSERT INTO quickdraw (player, versus, time) VALUES ('" + throwerNameStr + "','" + hitNameStr + "'," + time + ")";
                            statement.executeUpdate(queryTime);
                            ResultSet rsThrowerInv = statement.executeQuery("SELECT * FROM inventories WHERE player = '" + throwerNameStr + "'");
                            if (rsThrowerInv.next()) {
                                String base64 = rsThrowerInv.getString("inventory");
                                Inventory ti = QuickdrawInventory.fromBase64(base64);
                                throwplayer.getInventory().setContents(ti.getContents());
                                String queryDelete = "DELETE FROM inventories WHERE player = '" + throwerNameStr + "'";
                                statement.executeUpdate(queryDelete);
                            }
                            statement.close();
                        } catch (SQLException e) {
                            plugin.debug("Could not get players inventory");
                        }
                        double seconds = (double) time / 1000000000.0;
                        throwplayer.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Your time against " + hitNameStr + " was " + seconds + " seconds");
                        plugin.drawtime.remove(throwerNameStr);
                    }
                }
            }
        }
    }
}
