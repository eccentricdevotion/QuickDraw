package me.eccentric_nz.plugins.quickdraw;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class QuickdrawCommands implements CommandExecutor {

    private Quickdraw plugin;
    String HELP;
    QuickdrawDatabase service = QuickdrawDatabase.getInstance();
    Random r = new Random();
    ItemStack is = new ItemStack(Material.SNOW_BALL);

    public QuickdrawCommands(Quickdraw plugin) {
        this.plugin = plugin;
        HELP = ChatColor.GOLD + "[QuickDraw] So you want to be a gunslinging cowboy?" + ChatColor.RESET + "\nBefore you can invite a player to a QuickDraw challenge\nyou must be within " + plugin.getConfig().getInt("invite_distance") + " blocks of the player.\nWhen you are, you can then type the command: " + ChatColor.BLUE + "/quickdraw invite [player]" + ChatColor.RESET + "\nMore instructions to come";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("quickdraw")) {
            if (args.length == 0) {
                sender.sendMessage(HELP.split("\n"));
                return true;
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("stats")) {
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            ResultSet rsPlayer = statement.executeQuery("SELECT * FROM quickdraw WHERE player = '" + player.getName() + "' ORDER BY time ASC LIMIT 5");
                            if (rsPlayer.isBeforeFirst()) {
                                player.sendMessage(ChatColor.GREEN + "Your top 5 fastest draws");
                                int i = 1;
                                while (rsPlayer.next()) {
                                    float t1 = rsPlayer.getInt("time") / 1000;
                                    player.sendMessage(i + ". " + t1 + " seconds - vs " + rsPlayer.getString("versus"));
                                    i++;
                                }
                            }
                            rsPlayer.close();
                        }
                        ResultSet rsAll = statement.executeQuery("SELECT * FROM quickdraw ORDER BY time ASC LIMIT 10");
                        if (rsAll.isBeforeFirst()) {
                            sender.sendMessage(ChatColor.AQUA + "The top 10 fastest draws");
                            int j = 1;
                            while (rsAll.next()) {
                                float t2 = rsAll.getInt("time") / 1000;
                                sender.sendMessage(j + ". " + t2 + " seconds - " + rsAll.getString("player") + " vs " + rsAll.getString("versus"));
                                j++;
                            }
                        }
                        rsAll.close();
                        statement.close();
                    } catch (SQLException e) {
                        plugin.debug("Could not get Stats");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("config")) {
                    Set<String> configNames = plugin.getConfig().getKeys(false);
                    sender.sendMessage(ChatColor.GRAY + QuickdrawConstants.MY_PLUGIN_NAME + ChatColor.RED + " Here are the current plugin getConfig() options!");
                    for (String cname : configNames) {
                        String value = plugin.getConfig().getString(cname);
                        sender.sendMessage(ChatColor.AQUA + cname + ": " + ChatColor.RESET + value);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("invite")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Only players can invite QuickDraw challenges!");
                        return true;
                    }
                    if (args.length < 2) {
                        return false;
                    }
                    Player player = (Player) sender;
                    if (plugin.getServer().getPlayer(args[1]) != null) {
                        // get invited players location
                        Player ip = plugin.getServer().getPlayer(args[1]);
                        Location iLoc = ip.getLocation();
                        Location cLoc = player.getLocation();
                        if (!inLocation(cLoc,iLoc)) {
                            player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "You must be within "+plugin.getConfig().getInt("invite_distance")+" blocks of the player you want to invite!");
                            return true;
                        }
                        ip.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + player.getName() + " has challenged you to a QuickDraw. Type: " + ChatColor.BLUE + "/quickdraw accept" + ChatColor.RESET + " to join in the gunslingin' fun!");
                        plugin.invites.put(ip.getName(), player.getName());
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("accept")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Only players can accept QuickDraw challenges!");
                        return true;
                    }
                    final Player player = (Player) sender;
                    if (!plugin.invites.containsKey(player.getName())) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "You must be invited to QuickDraw challenge before you can accept!");
                        return true;
                    }
                    // set up the game
                    final String challengerNameStr = plugin.invites.get(player.getName());
                    final Player challenger = plugin.getServer().getPlayer(challengerNameStr);

                    // get challengers location
                    Location cLoc = challenger.getLocation();
                    // get direction challenger is facing
                    int cYaw = (int) QuickdrawConstants.getYaw(cLoc);
                    cLoc.setYaw(cYaw);
                    int vYaw = ((cYaw - 180) < 0) ? ((cYaw - 180) + 360) : (cYaw - 180);
                    // move players into position
                    double vx, vz;
                    double draw = plugin.getConfig().getInt("draw_distance") + 0.5;
                    switch (cYaw) {
                        case 0: // facing south
                            vx = cLoc.getBlockX();
                            vz = cLoc.getBlockZ() + draw;
                            break;
                        case 90: // facing west
                            vx = cLoc.getBlockX() - draw;
                            vz = cLoc.getBlockZ();
                            break;
                        case 180: // facing north
                            vx = cLoc.getBlockX();
                            vz = cLoc.getBlockZ() - draw;
                            break;
                        default: // facing east
                            vx = cLoc.getBlockX() + draw;
                            vz = cLoc.getBlockZ();
                            break;
                    }
                    plugin.debug("x: " + vx + ", z:" + vz);
                    challenger.teleport(cLoc);
                    // put challenger in list
                    plugin.challengers.put(challengerNameStr, player.getName());
                    Location vLoc = new Location(cLoc.getWorld(), vx, cLoc.getY(), vz, vYaw, 0);
                    // save then clear inventories
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String challengerInv = QuickdrawInventory.toBase64(challenger.getInventory());
                        String challegerQuery = "INSERT INTO inventories (player, inventory) VALUES ('" + challengerNameStr + "','" + challengerInv + "')";
                        statement.executeUpdate(challegerQuery);
                        challenger.getInventory().clear();
                        String versusInv = QuickdrawInventory.toBase64(player.getInventory());
                        String versusQuery = "INSERT INTO inventories (player, inventory) VALUES ('" + player.getName() + "','" + versusInv + "')";
                        statement.executeUpdate(versusQuery);
                    } catch (SQLException e) {
                        plugin.debug("Could not save inventories");
                    }
                    player.getInventory().clear();
                    // ready, set, draw
                    challenger.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "READY...");
                    player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "READY...");
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            challenger.sendMessage("SET...");
                            player.sendMessage("SET...");
                        }
                    }, 40L);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            challenger.sendMessage("DRAW!");
                            player.sendMessage("DRAW!");
                            int r1 = r.nextInt(9);
                            int r2 = r.nextInt(9);
                            // add snowball to random slot in players inventory
                            challenger.getInventory().setItem(r1, is);
                            player.getInventory().setItem(r2, is);
                            long systime = System.currentTimeMillis();
                            plugin.drawtime.put(player.getName(), systime);
                            plugin.drawtime.put(challengerNameStr, systime);
                        }
                    }, 80L);
                }
                if (args[0].equalsIgnoreCase("decline")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Only players can decline QuickDraw challenges!");
                        return true;
                    }
                    Player player = (Player) sender;
                    String pNameString = player.getName();
                    if (!plugin.invites.containsKey(pNameString)) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "You must be invited to QuickDraw challenge before you can decline!");
                        return true;
                    }
                    String challengerNameStr = plugin.invites.get(pNameString);
                    Player challenger = plugin.getServer().getPlayer(challengerNameStr);
                    challenger.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + pNameString + " declined your Quickdraw challenge because "+QuickdrawConstants.insults[r.nextInt(QuickdrawConstants.insults.length)]+"!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("restore")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Only players can restore their inventories!");
                        return true;
                    }
                    Player player = (Player) sender;
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        ResultSet rsInv = statement.executeQuery("SELECT * FROM inventories WHERE player = '" + player.getName() + "'");
                        if (!rsInv.next()) {
                            sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Could not find your inventory!");
                            return true;
                        }
                        String base64 = rsInv.getString("inventory");
                        Inventory i = QuickdrawInventory.fromBase64(base64);
                        player.getInventory().setContents(i.getContents());
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Restoring your inventory!");
                        String queryDelete = "DELETE FROM inventories WHERE player = '" + player.getName() + "'";
                        statement.executeUpdate(queryDelete);
                    } catch (SQLException e) {
                        plugin.debug("Could not get players inventory");
                    }
                    return true;
                }
            }
        }
        return false;
    }
    public boolean inLocation(Location a, Location b) {
        int dist = plugin.getConfig().getInt("invite_distance");
        int xMin = a.getBlockX() - dist;
        int xMax = a.getBlockX() + dist;
        int zMin = a.getBlockZ() - dist;
        int zMax = a.getBlockZ() + dist;
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
