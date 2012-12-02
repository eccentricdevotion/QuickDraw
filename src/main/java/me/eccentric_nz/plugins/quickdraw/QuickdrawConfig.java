package me.eccentric_nz.plugins.quickdraw;

import java.util.HashMap;
import java.util.Map;

public class QuickdrawConfig {

    private Quickdraw plugin;

    public QuickdrawConfig(Quickdraw plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        HashMap<String, String> items = new HashMap<String, String>();

        items = loadConfigurables(items);

        int num = 0;
        for (Map.Entry item : items.entrySet()) {
            if (plugin.getConfig().get((String) item.getKey()) == null) {
                if (((String) item.getValue()).equalsIgnoreCase("true")) {
                    plugin.getConfig().set((String) item.getKey(), Boolean.valueOf(true));
                } else if (((String) item.getValue()).equalsIgnoreCase("false")) {
                    plugin.getConfig().set((String) item.getKey(), Boolean.valueOf(false));
                } else if (isNum((String) item.getValue())) {
                    plugin.getConfig().set((String) item.getKey(), parseNum((String) item.getValue()));
                } else {
                    plugin.getConfig().set((String) item.getKey(), item.getValue());
                }
                num++;
            }
        }
        if (num > 0) {
            System.out.println(QuickdrawConstants.MY_PLUGIN_NAME + num + " missing items added to config file.");
        }
        plugin.saveConfig();
    }

    private HashMap<String, String> loadConfigurables(HashMap<String, String> items) {
        items.put("use_economy", "true");
        items.put("invite_distance", "10");
        items.put("invite_timeout", "20");
        items.put("draw_distance", "5");
        items.put("unfreeze_after_miss", "10");
        items.put("replay_if_draw", "false");
        items.put("debug", "false");
        return items;
    }

    public boolean isNum(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public int parseNum(String input) {
        int num = 0;
        try {
            num = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            plugin.debug("Could not convert to int");
        }
        return num;
    }
}
