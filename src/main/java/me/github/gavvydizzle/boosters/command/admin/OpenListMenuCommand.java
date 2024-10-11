package me.github.gavvydizzle.boosters.command.admin;

import com.github.mittenmc.serverutils.SubCommand;
import me.github.gavvydizzle.boosters.boost.target.GlobalTarget;
import me.github.gavvydizzle.boosters.boost.target.PlayerTarget;
import me.github.gavvydizzle.boosters.command.AdminCommandManager;
import me.github.gavvydizzle.boosters.gui.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class OpenListMenuCommand extends SubCommand {

    private final InventoryManager inventoryManager;

    public OpenListMenuCommand(AdminCommandManager adminCommandManager, InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;

        setName("list");
        setDescription("Opens the boost list menu");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " list [player]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        if (args.length == 1 || args[1].equals("*")) {
            inventoryManager.openMenu(player, new GlobalTarget(), true);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player: " + args[1]);
            return;
        }

        inventoryManager.openMenu(player, new PlayerTarget(target), true);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) return null;
        return Collections.emptyList();
    }

}