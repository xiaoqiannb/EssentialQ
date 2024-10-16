package com.xiaoqiannb;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class essentialq extends JavaPlugin implements Listener {

    private HashMap<Player, Location> homes = new HashMap<>();
    private HashMap<Player, Player> tpRequests = new HashMap<>();
    private HashMap<Player, Long> tpRequestTimes = new HashMap<>();
    private HashMap<Player, Location> lastLocation = new HashMap<>(); // 存储玩家上一次位置

    @Override
    public void onEnable() {
        getLogger().info("essentialQ已启用，小千祝您开服愉快！");
        getServer().getPluginManager().registerEvents(this, this); // 注册事件监听器
    }

    @Override
    public void onDisable() {
        getLogger().info("essentialQ已禁用");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("此命令只能由玩家使用");
            return true;
        }

        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {
            case "sethome":
                lastLocation.put(player, player.getLocation()); // 更新上次位置
                homes.put(player, player.getLocation());
                player.sendMessage("家位置已设置！");
                return true;

            case "home":
                Location home = homes.get(player);
                if (home != null) {
                    lastLocation.put(player, player.getLocation()); // 更新上次位置
                    player.teleport(home);
                    player.sendMessage("传送到你的家！");
                } else {
                    player.sendMessage("你还没有设置家！");
                }
                return true;

            case "tpa":
                if (args.length == 0) {
                    player.sendMessage("用法: /tpa <玩家名>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target != player) {
                    tpRequests.put(target, player);
                    tpRequestTimes.put(target, System.currentTimeMillis());
                    target.sendMessage(player.getName() + " 请求传送到你这里。输入 /tpaccept 来接受，30秒后请求将失效。");
                    player.sendMessage("请求已发送给 " + target.getName());
                } else {
                    player.sendMessage("玩家未在线或指定了自己！");
                }
                return true;

            case "tpaccept":
                if (tpRequests.containsKey(player)) {
                    Long requestTime = tpRequestTimes.get(player);
                    if (System.currentTimeMillis() - requestTime <= 30000) { // 30秒限制
                        Player requester = tpRequests.get(player);
                        lastLocation.put(player, player.getLocation()); // 更新上次位置
                        player.teleport(requester.getLocation());
                        player.sendMessage("你已传送到 " + requester.getName());
                        requester.sendMessage(player.getName() + " 已接受传送请求。");
                        tpRequests.remove(player);
                        tpRequestTimes.remove(player); // 移除时间记录
                    } else {
                        player.sendMessage("传送请求已过期。请重新请求。");
                        tpRequests.remove(player);
                        tpRequestTimes.remove(player); // 移除时间记录
                    }
                } else {
                    player.sendMessage("没有传送请求！");
                }
                return true;

            case "hub":
                Location spawnLocation = player.getWorld().getSpawnLocation(); // 获取重生点
                lastLocation.put(player, player.getLocation()); // 更新上次位置
                player.teleport(spawnLocation);
                player.sendMessage("你已传送到世界重生点！");
                return true;

            case "back":
                if (lastLocation.containsKey(player)) {
                    Location previousLocation = lastLocation.get(player);
                    player.teleport(previousLocation);
                    player.sendMessage("你已返回到上次的位置！");
                } else {
                    player.sendMessage("没有上次的位置记录！");
                }
                return true;

            default:
                return false;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        lastLocation.put(player, player.getLocation()); // 记录死亡前的位置
    }

    // 捕获潜在的传送命令
    public void onTeleportCommand(Player player, String commandName) {
        if (commandName.equalsIgnoreCase("spawn") || commandName.equalsIgnoreCase("warp")) {
            lastLocation.put(player, player.getLocation()); // 更新上次位置
        }
    }

    // 其他事件，可以在这里捕获原版相关命令的调用
}
