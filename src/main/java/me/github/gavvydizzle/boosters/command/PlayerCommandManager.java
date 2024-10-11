package me.github.gavvydizzle.boosters.command;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.command.HelpCommand;
import me.github.gavvydizzle.boosters.boost.target.PlayerTarget;
import me.github.gavvydizzle.boosters.command.player.OpenListMenuCommand;
import me.github.gavvydizzle.boosters.gui.InventoryManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PlayerCommandManager extends CommandManager {

    private final InventoryManager inventoryManager;

    public PlayerCommandManager(PluginCommand command, InventoryManager inventoryManager) {
        super(command);
        this.inventoryManager = inventoryManager;

        registerCommand(new HelpCommand.HelpCommandBuilder(this).build());
        registerCommand(new OpenListMenuCommand(this, inventoryManager));
    }

    @Override
    public void onNoSubcommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            super.onNoSubcommand(sender, args);
            return;
        }

        inventoryManager.openMenu(player, new PlayerTarget(player), false);
    }
}
