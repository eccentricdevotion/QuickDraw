package me.eccentric_nz.plugins.quickdraw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Quickdraw extends JavaPlugin implements Listener {

    QuickdrawDatabase service = QuickdrawDatabase.getInstance();
    public static Permission permission = null;
    public static Economy economy = null;
    protected static Quickdraw plugin;
    public static HashMap<String, ItemStack[]> invents = new HashMap<String, ItemStack[]>();
    HashMap<String, String> invites = new HashMap<String, String>();
    HashMap<String, String> challengers = new HashMap<String, String>();
    HashMap<String, String> accepted = new HashMap<String, String>();
    HashMap<String, Long> drawtime = new HashMap<String, Long>();
    HashMap<String, Long> hittime = new HashMap<String, Long>();
    private QuickdrawCommands commando;
    private QuickdrawConfig qdc;
    PluginManager pm = Bukkit.getServer().getPluginManager();
    QuickdrawThrowListener throwListener = new QuickdrawThrowListener(this);
    QuickdrawHitListener hitListener = new QuickdrawHitListener(this);
    QuickdrawMoveListener moveListener = new QuickdrawMoveListener(this);
    public int invdist;

    @Override
    public void onEnable() {
        plugin = this;
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                System.out.println(QuickdrawConstants.MY_PLUGIN_NAME + "Could not create directory!");
                System.out.println(QuickdrawConstants.MY_PLUGIN_NAME + "Requires you to manually make the NonSpecificOdyssey/ directory!");
            }
            getDataFolder().setWritable(true);
            getDataFolder().setExecutable(true);
        }
        this.saveDefaultConfig();
        qdc = new QuickdrawConfig(plugin);
        qdc.updateConfig();

        invdist = getConfig().getInt("invite_distance");
        debug(invdist);

        try {
            String path = getDataFolder() + File.separator + "QuickDraw.db";
            service.setConnection(path);
            service.createTables();
        } catch (Exception e) {
            debug("Connection and Tables Error: " + e);
        }

        pm.registerEvents(hitListener, this);
        pm.registerEvents(throwListener, this);
        pm.registerEvents(moveListener, this);
        commando = new QuickdrawCommands(plugin);
        getCommand("quickdraw").setExecutor(commando);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        if (getConfig().getBoolean("use_economy") && getServer().getPluginManager().isPluginEnabled("Vault")) {
            if (!setupEconomy()) {
                System.out.println(QuickdrawConstants.MY_PLUGIN_NAME + "- Disabled due to no Vault dependency found!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            setupPermissions();
        }

    }

    @Override
    public void onDisable() {
        this.saveConfig();
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public static void saveContents(Player player) {
        if (!invents.containsKey(player.getName())) {
            invents.put(player.getName(), player.getInventory().getContents());
        } else if (invents.containsKey(player.getName())) {
            return;
        }
    }

    public static void retrieveContents(Player player) {
        if (!invents.containsKey(player.getName())) {
            return;
        } else if (invents.containsKey(player.getName())) {
            player.getInventory().setContents(invents.get(player.getName()));
        }
    }

    public void debug(Object o) {
        if (getConfig().getBoolean("debug") == true) {
            System.out.println("[QuickDraw Debug] " + o);
        }
    }
}
