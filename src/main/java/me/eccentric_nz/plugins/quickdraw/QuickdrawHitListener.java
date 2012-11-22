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
                    String hNameStr = hitplayer.getName();
                    String tNameStr = throwplayer.getName();
                    if (plugin.accepted.containsKey(hNameStr) || plugin.challengers.containsKey(hNameStr)) {
                        plugin.debug(hNameStr + " was hit by a snowball thrown by " + tNameStr + "!");
                        long nanotime = System.nanoTime();
                        if (tNameStr.equalsIgnoreCase(plugin.accepted.get(hNameStr)) || tNameStr.equalsIgnoreCase(plugin.challengers.get(hNameStr))) {
                            plugin.hittime.put(tNameStr, nanotime);
                            long time = plugin.hittime.get(tNameStr) - plugin.drawtime.get(tNameStr);
                            plugin.quicktime.put(tNameStr, time);
                            try {
                                Connection connection = service.getConnection();
                                Statement statement = connection.createStatement();
                                String queryTime = "INSERT INTO quickdraw (player, versus, time) VALUES ('" + tNameStr + "','" + hNameStr + "'," + time + ")";
                                statement.executeUpdate(queryTime);
                                ResultSet rsHitInv = statement.executeQuery("SELECT * FROM inventories WHERE player = '" + hNameStr + "'");
                                if (rsHitInv.next()) {
                                    String base64 = rsHitInv.getString("inventory");
                                    Inventory hi = QuickdrawInventory.fromBase64(base64);
                                    hitplayer.getInventory().setContents(hi.getContents());
                                    String queryDelete = "DELETE FROM inventories WHERE player = '" + hNameStr + "'";
                                    statement.executeUpdate(queryDelete);
                                }
                                statement.close();
                            } catch (SQLException e) {
                                plugin.debug("Could not get players inventory");
                            }
                            double seconds = (double) time / 1000000000.0;
                            throwplayer.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Your time against " + hNameStr + " was " + seconds + " seconds");
                            plugin.drawtime.remove(tNameStr);
                            plugin.hittime.remove(tNameStr);
                            if (plugin.challengers.containsKey(hNameStr)) {
                                plugin.challengers.remove(hNameStr);
                            }
                            if (plugin.accepted.containsKey(hNameStr)) {
                                plugin.accepted.remove(hNameStr);
                            }
                        }
                    }
                }
            }
        }
    }
}
