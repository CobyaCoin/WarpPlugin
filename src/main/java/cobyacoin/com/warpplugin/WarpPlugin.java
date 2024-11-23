package cobyacoin.com.warpplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WarpPlugin extends JavaPlugin {

    private final Map<String, Location> warps = new HashMap<>();
    private File warpsFile;

    @Override
    public void onEnable() {
        getLogger().info("WarpPlugin enabled!");

        // Создание папки плагина и файла для хранения данных
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        warpsFile = new File(getDataFolder(), "warps.dat");

        // Загрузка данных
        loadWarps();
    }

    @Override
    public void onDisable() {
        // Сохранение данных
        saveWarps();
        getLogger().info("WarpPlugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Используйте: /warp create <название>");
                    return true;
                }
                String warpName = args[1];
                warps.put(warpName, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Точка '" + ChatColor.AQUA + warpName + ChatColor.GREEN + "' создана!");
                break;

            case "list":
                if (warps.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Нет доступных точек.");
                } else {
                    player.sendMessage(ChatColor.GOLD + "Список точек:");
                    for (Map.Entry<String, Location> entry : warps.entrySet()) {
                        Location loc = entry.getValue();
                        player.sendMessage(ChatColor.AQUA + "- " + entry.getKey() + ChatColor.WHITE + " (" +
                                ChatColor.GREEN + loc.getWorld().getName() + ChatColor.WHITE + ", " +
                                ChatColor.GREEN + loc.getBlockX() + ChatColor.WHITE + ", " +
                                ChatColor.GREEN + loc.getBlockY() + ChatColor.WHITE + ", " +
                                ChatColor.GREEN + loc.getBlockZ() + ChatColor.WHITE + ")");
                    }
                }
                break;

            case "goto":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Используйте: /warp goto <название>");
                    return true;
                }
                warpName = args[1];
                if (!warps.containsKey(warpName)) {
                    player.sendMessage(ChatColor.RED + "Точка '" + ChatColor.AQUA + warpName + ChatColor.RED + "' не найдена.");
                } else {
                    player.teleport(warps.get(warpName));
                    player.sendMessage(ChatColor.GREEN + "Телепортировано к точке '" + ChatColor.AQUA + warpName + ChatColor.GREEN + "'.");
                }
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== WarpPlugin Команды ===");
        player.sendMessage(ChatColor.AQUA + "/warp create <название>" + ChatColor.WHITE + " - Создать точку.");
        player.sendMessage(ChatColor.AQUA + "/warp list" + ChatColor.WHITE + " - Показать список точек.");
        player.sendMessage(ChatColor.AQUA + "/warp goto <название>" + ChatColor.WHITE + " - Телепортироваться к точке.");
    }

    private void saveWarps() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(warpsFile))) {
            oos.writeObject(warps);
            getLogger().info("Точки сохранены.");
        } catch (IOException e) {
            getLogger().severe("Ошибка сохранения точек: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadWarps() {
        if (!warpsFile.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(warpsFile))) {
            Map<String, Location> loadedWarps = (Map<String, Location>) ois.readObject();
            warps.putAll(loadedWarps);
            getLogger().info("Точки загружены.");
        } catch (IOException | ClassNotFoundException e) {
            getLogger().severe("Ошибка загрузки точек: " + e.getMessage());
        }
    }
}