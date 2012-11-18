package me.eccentric_nz.plugins.quickdraw;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuickdrawCommands implements CommandExecutor {

    private Quickdraw plugin;

    public QuickdrawCommands(Quickdraw plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command!");
            return true;
        }
        Player player = (Player) sender;
        if(cmd.getName().equalsIgnoreCase("quickdraw")) {

        }
        return true;
    }
}
