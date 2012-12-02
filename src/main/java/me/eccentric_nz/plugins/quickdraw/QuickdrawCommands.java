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
        HELP = ChatColor.GOLD + "[QuickDraw] So you want to be a gunslinging cowboy?" + ChatColor.RESET + "\nBefore you can invite a player to a QuickDraw challenge\nyou must be within " + plugin.getConfig().getInt("invite_distance") + " blocks of the player.\nWhen you are, you can then type the command: " + ChatColor.BLUE + "/quickdraw invite [player]" + ChatColor.RESET + "\nIf Vault and an economy plugin are enabled on the server, you can challenge a player for money with:\n" + ChatColor.BLUE + "/quickdraw invite [player] [amount]" + ChatColor.RESET + "\nA message will be sent to the invited player asking them whether they want to accept the challenge.\nTo accept a challenge, type: " + ChatColor.BLUE + "/quickdraw accept" + ChatColor.RESET + "\nOnce the player has accepted the challenge, they will be teleported into position,\n both players inventories will be saved and cleared, and the players will be prevented from moving.\nA short countdown will begin - READY, SET, DRAW! - and then a snowball will appear in a random hotkey slot.\nThe first player to throw the snowball and hit the other player is the QuickDraw winner.\nInventories and movement are restored, QuickDraw times are displayed, and prize money awarded when the match is over.\nYou can decline a QuickDraw inviation with the command: " + ChatColor.BLUE + "/quickdraw decline" + ChatColor.RESET + "\nor if no response is received within" + plugin.getConfig().getInt("invite_timeout") + " seconds the invitation will be cancelled.\n" + ChatColor.GOLD + "QuickDraw statistics" + ChatColor.RESET + "\nTo view QuickDraw statistics type the command: " + ChatColor.BLUE + "/quickdraw stats" + ChatColor.RESET + "Your best draws and the 10 alltime fastest draws will be displayed.\nIn the unlikely event that your inventory does not get restored, type: " + ChatColor.BLUE + "/quickdraw restore";
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
                                    double t1 = rsPlayer.getDouble("time") / 1000000000.0;
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
                                double t2 = rsAll.getDouble("time") / 1000000000.0;
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
                    final Player player = (Player) sender;
                    if (plugin.getServer().getPlayer(args[1]) != null) {
                        // get invited players location
                        final Player ip = plugin.getServer().getPlayer(args[1]);
                        Location iLoc = ip.getLocation();
                        Location cLoc = player.getLocation();
                        if (!inLocation(cLoc, iLoc)) {
                            player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "You must be within " + plugin.getConfig().getInt("invite_distance") + " blocks of the player you want to invite!");
                            return true;
                        }
                        String amount = "";
                        if (plugin.getServer().getPluginManager().isPluginEnabled("Vault") && plugin.getConfig().getBoolean("use_economy") && args.length > 2) {
                            double purse = Double.parseDouble(args[2]);
                            if (Quickdraw.economy.getBalance(player.getName()) < purse) {
                                player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "You don't have enough money to make that challenge!");
                                return false;
                            }
                            plugin.purse.put(player.getName(), purse);
                            amount = " for " + Quickdraw.economy.format(purse);
                        }
                        ip.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + player.getName() + " has challenged you to a QuickDraw" + amount + ". Type: " + ChatColor.BLUE + "/quickdraw accept" + ChatColor.RESET + " to join in the gunslingin' fun!");
                        plugin.invites.put(ip.getName(), player.getName());
                        player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Inviting player...");
                        long invite_timeout = plugin.getConfig().getInt("invite_timeout") * 20;
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                if (plugin.invites.containsKey(ip.getName())) {
                                    player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "There was no response to your challenge!");
                                    plugin.invites.remove(ip.getName());
                                    plugin.purse.remove(player.getName());
                                }
                            }
                        }, invite_timeout);
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("accept")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Only players can accept QuickDraw challenges!");
                        return true;
                    }
                    final Player player = (Player) sender;
                    final String pNameStr = player.getName();
                    if (!plugin.invites.containsKey(pNameStr)) {
                        sender.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "You must be invited to QuickDraw challenge before you can accept!");
                        return true;
                    }
                    // set up the game
                    final String challengerNameStr = plugin.invites.get(pNameStr);
                    final Player challenger = plugin.getServer().getPlayer(challengerNameStr);
                    plugin.invites.remove(pNameStr);
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
                    final Location vLoc = new Location(cLoc.getWorld(), vx, cLoc.getY(), vz, vYaw, 0);
                    challenger.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Teleporting your opponent into position...");
                    player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "Teleporting you into position...");
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            player.teleport(vLoc);
                        }
                    }, 40L);
                    // put challenger in list
                    plugin.challengers.put(challengerNameStr, pNameStr);
                    // put accpetor in list
                    plugin.accepted.put(pNameStr, challengerNameStr);
                    // save then clear inventories
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String challengerInv = QuickdrawInventory.toBase64(challenger.getInventory());
                        String challegerQuery = "INSERT INTO inventories (player, inventory) VALUES ('" + challengerNameStr + "','" + challengerInv + "')";
                        statement.executeUpdate(challegerQuery);
                        challenger.getInventory().clear();
                        String versusInv = QuickdrawInventory.toBase64(player.getInventory());
                        String versusQuery = "INSERT INTO inventories (player, inventory) VALUES ('" + pNameStr + "','" + versusInv + "')";
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
                            challenger.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "SET...");
                            player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "SET...");
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    challenger.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "DRAW!");
                                    player.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "DRAW!");
                                    int r1 = r.nextInt(9);
                                    int r2 = r.nextInt(9);
                                    // add snowball to random slot in players inventory
                                    challenger.getInventory().setItem(r1, is);
                                    player.getInventory().setItem(r2, is);
                                    long nanotime = System.nanoTime();
                                    plugin.drawtime.put(pNameStr, nanotime);
                                    plugin.drawtime.put(challengerNameStr, nanotime);
                                    startDelayedEndCode(pNameStr);
                                }
                            }, 40L);
                        }
                    }, 40L);

                    return true;
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
                    if (plugin.purse.containsKey(challengerNameStr)) {
                        plugin.purse.remove(challengerNameStr);
                    }
                    Player challenger = plugin.getServer().getPlayer(challengerNameStr);
                    challenger.sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + pNameString + " declined your Quickdraw challenge because " + QuickdrawConstants.insults[r.nextInt(QuickdrawConstants.insults.length)] + "!");
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

    public void startDelayedEndCode(String p) {
        long unfreeze = (plugin.getConfig().getLong("unfreeze_after_miss") * 20) + 50L;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new QuickdrawEndSchedule(plugin, p), unfreeze);
    }
}
