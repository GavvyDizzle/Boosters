package me.github.gavvydizzle.boosters.command.player;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import me.github.gavvydizzle.boosters.boost.target.PlayerTarget;
import me.github.gavvydizzle.boosters.gui.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class OpenListMenuCommand extends SubCommand {

    private static final String viewOtherPlayerBoostsPermission = "boosters.player.list.other";

    private final InventoryManager inventoryManager;

    public OpenListMenuCommand(CommandManager commandManager, InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;

        setName("list");
        setDescription("View active boosts");
        setSyntax("/" + commandManager.getCommandDisplayName() + " list [player]");
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        if (args.length == 1 || args[1].equalsIgnoreCase(player.getName())) {
            inventoryManager.openMenu(player, new PlayerTarget(player), false);
            return;
        }

        if (!player.hasPermission(viewOtherPlayerBoostsPermission)) {
            sender.sendMessage(ChatColor.RED + "You are unable to view other players' boosts");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player: " + args[1]);
            return;
        }

        inventoryManager.openMenu(player, new PlayerTarget(target), false);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) return null;
        return Collections.emptyList();
    }

}