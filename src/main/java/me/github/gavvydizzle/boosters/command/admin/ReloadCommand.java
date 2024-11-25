package me.github.gavvydizzle.boosters.command.admin;

import com.github.mittenmc.serverutils.SubCommand;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.command.AdminCommandManager;
import me.github.gavvydizzle.boosters.gui.InventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ReloadCommand extends SubCommand {

    private final BoostPlugin instance;
    private final BoostManager boostManager;
    private final InventoryManager inventoryManager;

    private final List<String> args2 = List.of("bossbar", "menus");

    public ReloadCommand(AdminCommandManager adminCommandManager, BoostPlugin instance, BoostManager boostManager, InventoryManager inventoryManager) {
        this.instance = instance;
        this.boostManager = boostManager;
        this.inventoryManager = inventoryManager;

        setName("reload");
        setDescription("Reload plugin data");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " reload");
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        try {
            if (args.length == 1) {
                reloadBossBars();
                reloadMenus();
                sender.sendMessage(ChatColor.GREEN + "[Boosters] Successfully reloaded");
                return;
            }

            if (!args2.contains(args[1].toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "Invalid reload option: " + args[1]);
                return;
            }

            if (args[1].equalsIgnoreCase("bossbar")) {
                reloadBossBars();
            } else if (args[1].equalsIgnoreCase("menus")) {
                reloadMenus();
            }
            sender.sendMessage(ChatColor.GREEN + "[Boosters] Successfully reloaded " + args[1].toLowerCase());

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Reload error encountered. Please check the console for errors");
            instance.getLogger().log(Level.SEVERE, "Plugin reload failed", e);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], args2, list);
        }
        return list;
    }

    private void reloadBossBars() {
        instance.getConfigManager().reload("bossbar");
        boostManager.reload();
        instance.getConfigManager().save("bossbar");
    }

    private void reloadMenus() {
        instance.getConfigManager().reload("menus");
        inventoryManager.reload();
        instance.getConfigManager().save("menus");
    }
}