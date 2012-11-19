package me.eccentric_nz.plugins.quickdraw;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            ResultSet rsPlayer = service.getRecords("SELECT * FROM quickdraw WHERE player = '" + player.getName() + "' ORDER BY time ASC LIMIT 5");
                            if (rsPlayer.isBeforeFirst()) {
                                player.sendMessage(ChatColor.GREEN + "Your top 5 fastest draws");
                                int i = 1;
                                while (rsPlayer.next()) {
                                    float t1 = rsPlayer.getInt("time") / 1000;
                                    player.sendMessage(i + ". " + t1 + "seconds - vs " + rsPlayer.getString("versus"));
                                    i++;
                                }
                            }
                            rsPlayer.close();
                        }
                        ResultSet rsAll = service.getRecords("SELECT * FROM quickdraw ORDER BY time ASC LIMIT 10");
                        if (rsAll.isBeforeFirst()) {
                            sender.sendMessage(ChatColor.GREEN + "The top 10 fastest draws");
                            int j = 1;
                            while (rsAll.next()) {
                                float t2 = rsAll.getInt("time") / 1000;
                                sender.sendMessage(j + ". " + t2 + "seconds - " + rsAll.getString("player") + " vs " + rsAll.getString("versus"));
                                j++;
                            }
                        }
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
                        plugin.getServer().getPlayer(args[1]).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + player.getName() + " has challenged yo to a QuickDraw. Type: " + ChatColor.BLUE + "/quickdraw accept" + ChatColor.RESET + " to join in the gunslingin' fun!");
                        plugin.invites.put(args[1], player.getName());
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
                    // put challenger in list
                    plugin.challengers.put(challengerNameStr,player.getName());
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
                    challenger.teleport(cLoc);
                    Location vLoc = new Location(cLoc.getWorld(), vx, cLoc.getY(), vz, vYaw, 0);
                    // save then clear inventories
                    String challengerInv = QuickdrawInventory.toBase64(challenger.getInventory());
                    service.doUpdate("INSERT INTO inventories (player,inventory) VALUES ('" + challengerNameStr + "','" + challengerInv + "')");
                    challenger.getInventory().clear();
                    String versusInv = QuickdrawInventory.toBase64(player.getInventory());
                    service.doUpdate("INSERT INTO inventories (player,inventory) VALUES ('" + player.getName() + "','" + versusInv + "')");
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
                    }, 20L);
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
                    }, 40L);
                }
            }
        }
        return false;
    }
}
