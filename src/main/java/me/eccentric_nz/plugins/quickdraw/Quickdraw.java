package me.eccentric_nz.plugins.quickdraw;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Quickdraw extends JavaPlugin implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");
    public static Permission permission = null;
    public static Economy economy = null;
    protected static Quickdraw plugin;
    HashMap<String, Long> rtpcooldown = new HashMap<String, Long>();
    private QuickdrawCommands commando;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                System.err.println("[NonSpecificOdyssey] Could not create directory!");
                System.out.println("[NonSpecificOdyssey] Requires you to manually make the NonSpecificOdyssey/ directory!");
            }
            getDataFolder().setWritable(true);
            getDataFolder().setExecutable(true);
        }
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
                log.log(Level.SEVERE, String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            setupPermissions();
        }
        getServer().getPluginManager().registerEvents(this, this);
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
}
