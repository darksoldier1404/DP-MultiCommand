package com.darksoldier1404.dmc;

import com.darksoldier1404.dppc.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiCommand extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private static MultiCommand plugin;
    public static YamlConfiguration config;
    public static Map<String, List<String>> commands = new HashMap<>();

    public static MultiCommand getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        config = ConfigUtils.loadDefaultPluginConfig(plugin);
        config.getConfigurationSection("Settings.Commands").getKeys(false).forEach(key -> commands.put(key, config.getStringList("Settings.Commands." + key)));
        getServer().getPluginManager().registerEvents(plugin, plugin);
        getCommand("dmc").setExecutor(plugin);
    }

    @Override
    public void onDisable() {
        ConfigUtils.savePluginConfig(plugin, config);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().split(" ")[0].substring(1);
        Player p = e.getPlayer();
        if (commands.containsKey(cmd)) {
            e.setCancelled(true);
            long delay = 0;
            List<String> cmds = commands.get(cmd);
            for (String c : cmds) {
                if (c.contains("DELAY")) {
                    delay += Long.parseLong(c.split(":")[1]);
                } else {
                    c = c.replace("<player>", p.getName());
                    if(c.contains("<randomPlayer>")) {
                        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                        int random = (int) (Math.random() * players.size());
                        c = c.replace("<randomPlayer>", players.get(random).getName());
                    }
                    if(c.contains("<op>")) {
                        c = c.replace("<op>", "");
                        String finalC = c;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            p.setOp(true);
                            p.performCommand(finalC);
                            p.setOp(false);
                        }, delay);
                    }else{
                        if(c.contains("<cs_console>")) {
                            c = c.replace("<cs_console>", "");
                            String finalC1 = c;
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalC1);
                            }, delay);
                        }else{
                            String finalC = c;
                            Bukkit.getScheduler().runTaskLater(plugin, () -> p.performCommand(finalC), delay);
                        }
                    }
                    delay++;
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You don't have permission to use this command.");
            return false;
        }
        if (args.length == 0) {
            sender.sendMessage("/dmc reload - reload plugin config");
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            config = ConfigUtils.loadDefaultPluginConfig(plugin);
            commands.clear();
            config.getConfigurationSection("Settings.Commands").getKeys(false).forEach(key -> commands.put(key, config.getStringList("Settings.Commands." + key)));
            sender.sendMessage("DMC Config reloaded.");
            return false;
        }
        return false;
    }
}
