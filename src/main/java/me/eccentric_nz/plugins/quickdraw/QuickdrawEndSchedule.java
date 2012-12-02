package me.eccentric_nz.plugins.quickdraw;

import net.milkbowl.vault.economy.EconomyResponse;

public class QuickdrawEndSchedule implements Runnable {

    private Quickdraw plugin;
    private String pNameStr;

    public QuickdrawEndSchedule(Quickdraw instance, String p) {
        plugin = instance;
        pNameStr = p;
    }

    public void run() {
        String versus = pNameStr;
        String challenger = plugin.accepted.get(pNameStr);

        // if one of the players missed...
        if (plugin.drawtime.containsKey(challenger)) {
            plugin.challengers.remove(pNameStr);
            plugin.quicktime.put(pNameStr, Long.MAX_VALUE);
        }
        if (plugin.drawtime.containsKey(versus)) {
            plugin.accepted.remove(pNameStr);
            plugin.quicktime.put(pNameStr, Long.MAX_VALUE);
        }
        // find out who won
        if (plugin.quicktime.get(versus) > plugin.quicktime.get(challenger)) {
            // Challenger won
            plugin.getServer().getPlayer(versus).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + challenger + " won!");
            plugin.getServer().getPlayer(challenger).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + challenger + " won!");
            if (plugin.purse.containsKey(challenger) && plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                double d = plugin.purse.get(challenger);
                EconomyResponse deposit = Quickdraw.economy.depositPlayer(challenger, d);
                Quickdraw.economy.withdrawPlayer(versus, d);
                if (deposit.transactionSuccess()) {
                    plugin.getServer().getPlayer(challenger).sendMessage(String.format(QuickdrawConstants.MY_PLUGIN_NAME + "You won %s from the QuickDraw challenge", Quickdraw.economy.format(deposit.amount)));
                } else {
                    plugin.getServer().getPlayer(challenger).sendMessage(String.format("An error occured: %s", deposit.errorMessage));
                }
            }
        }
        if (plugin.quicktime.get(versus) < plugin.quicktime.get(challenger)) {
            // Versus won
            plugin.getServer().getPlayer(versus).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + versus + " won!");
            plugin.getServer().getPlayer(challenger).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + versus + " won!");
            if (plugin.purse.containsKey(challenger) && plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                double d = plugin.purse.get(challenger);
                EconomyResponse deposit = Quickdraw.economy.depositPlayer(versus, d);
                Quickdraw.economy.withdrawPlayer(challenger, d);
                if (deposit.transactionSuccess()) {
                    plugin.getServer().getPlayer(versus).sendMessage(String.format(QuickdrawConstants.MY_PLUGIN_NAME + "You won %s from the QuickDraw challenge", Quickdraw.economy.format(deposit.amount)));
                } else {
                    plugin.getServer().getPlayer(versus).sendMessage(String.format("An error occured: %s", deposit.errorMessage));
                }
            }
        }
        if (plugin.quicktime.get(versus) == plugin.quicktime.get(challenger)) {
            // it was a draw
            plugin.getServer().getPlayer(versus).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "It was a draw!");
            plugin.getServer().getPlayer(challenger).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + "It was a draw!");
        }
        // clear accepted, challengers, drawtime, hittime and quicktime
        if (plugin.drawtime.containsKey(versus)) {
            plugin.drawtime.remove(versus);
        }
        if (plugin.drawtime.containsKey(challenger)) {
            plugin.drawtime.remove(challenger);
        }
        if (plugin.hittime.containsKey(versus)) {
            plugin.hittime.remove(versus);
        }
        if (plugin.hittime.containsKey(challenger)) {
            plugin.hittime.remove(challenger);
        }
        if (plugin.quicktime.containsKey(versus)) {
            plugin.quicktime.remove(versus);
        }
        if (plugin.quicktime.containsKey(challenger)) {
            plugin.quicktime.remove(challenger);
        }
        if (plugin.accepted.containsKey(versus)) {
            plugin.accepted.remove(versus);
        }
        if (plugin.challengers.containsKey(challenger)) {
            plugin.challengers.remove(challenger);
        }
    }
}
