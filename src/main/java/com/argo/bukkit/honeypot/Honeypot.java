package com.argo.bukkit.honeypot;


import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.argo.bukkit.honeypot.config.Config;
import com.argo.bukkit.honeypot.config.YMLFile;
import com.argo.bukkit.honeypot.listener.HoneypotBlockListener;
import com.argo.bukkit.honeypot.listener.HoneypotPlayerListener;
import com.argo.bukkit.util.BanHandler;
import com.argo.bukkit.util.BanHandlerFactory;
import com.argo.bukkit.util.PermissionSystem;

public class Honeypot extends JavaPlugin {

    private HoneyStack honeyStack;
    private Config config;
    private BanHandler bansHandler;
    private PermissionSystem perm;
    public Honeyfarm farm;

    public void onEnable() {
        honeyStack = new HoneyStack();
        farm = new Honeyfarm(this);

        perm = new PermissionSystem(this, getLogger());
        perm.setupPermissions();

        loadConfig();
        farm.setLogPath(config.getLogPath());

        if (!farm.refreshData()) {
        	getLogger().severe("An error occured while trying to load the honeypot list.");
        }

        BanHandlerFactory factory = new BanHandlerFactory(this, config);
        bansHandler = factory.getBansHandler();
        
        getServer().getPluginManager().registerEvents(new HoneypotBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new HoneypotPlayerListener(this), this);

        getCommand("honeypot").setExecutor(new CmdHoneypot(this));

        // schedule to run every minute (20 ticks * 60 seconds)
        getServer().getScheduler().scheduleSyncRepeatingTask(this, honeyStack, 1200, 1200);
    }

    public void onDisable() {
        if (!farm.saveData()) {
            getLogger().warning(("an error occured while trying to save the honeypot list."));
        }
        honeyStack.rollBackAll();
        getServer().getScheduler().cancelTasks(this);

    }

    private void loadConfig() {
    	saveDefaultConfig();
    	config = new YMLFile();
    	try {
			config.load(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	saveDefaultConfig();
    }

    public boolean hasPermission(CommandSender sender, String permissionNode) {
    	return perm.has(sender, permissionNode);
    }

    public Config getHPConfig() { return config; }
    public BanHandler getBansHandler() { return bansHandler; }

    public String getLogPath() {
        return config.getLogPath();
    }

    public HoneyStack getHoneyStack() {
        return honeyStack;
    }

    public static String prettyPrintLocation(Location l) {
        return "{world=" + l.getWorld().getName() + ", x=" + l.getBlockX() +
                ", y=" + l.getBlockY() + ", z=" + l.getBlockZ() + "}";
    }
}
