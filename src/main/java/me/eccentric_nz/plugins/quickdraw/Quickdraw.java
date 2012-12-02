package me.eccentric_nz.plugins.quickdraw;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Quickdraw extends JavaPlugin implements Listener {

    QuickdrawDatabase service = QuickdrawDatabase.getInstance();
    public static Permission permission = null;
    public static Economy economy = null;
    protected static Quickdraw plugin;
    HashMap<String, String> invites = new HashMap<String, String>();
    HashMap<String, String> challengers = new HashMap<String, String>();
    HashMap<String, String> accepted = new HashMap<String, String>();
    HashMap<String, Long> drawtime = new HashMap<String, Long>();
    HashMap<String, Long> hittime = new HashMap<String, Long>();
    HashMap<String, Long> quicktime = new HashMap<String, Long>();
    HashMap<String, Double> purse = new HashMap<String, Double>();
    private QuickdrawCommands commando;
    private QuickdrawConfig qdc;
    PluginManager pm = Bukkit.getServer().getPluginManager();
    QuickdrawThrowListener throwListener = new QuickdrawThrowListener(this);
    QuickdrawHitListener hitListener = new QuickdrawHitListener(this);
    QuickdrawMoveListener moveListener = new QuickdrawMoveListener(this);

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
        try {
            service.connection.close();
        } catch (Exception e) {
            System.err.println(QuickdrawConstants.MY_PLUGIN_NAME + " Could not close database connection: " + e);
        }
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

    public void debug(Object o) {
        if (getConfig().getBoolean("debug") == true) {
            System.out.println("[QuickDraw Debug] " + o);
        }
    }
}
