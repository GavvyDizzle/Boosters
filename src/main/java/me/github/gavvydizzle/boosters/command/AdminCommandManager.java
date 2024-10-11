package me.github.gavvydizzle.boosters.command;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.command.HelpCommand;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.command.admin.OpenListMenuCommand;
import me.github.gavvydizzle.boosters.command.admin.ReloadCommand;
import me.github.gavvydizzle.boosters.command.admin.StartBoostCommand;
import me.github.gavvydizzle.boosters.gui.InventoryManager;
import org.bukkit.command.PluginCommand;

public class AdminCommandManager extends CommandManager {

    public AdminCommandManager(PluginCommand command, BoostPlugin instance, BoostManager boostManager, InventoryManager inventoryManager) {
        super(command);

        registerCommand(new HelpCommand.HelpCommandBuilder(this).build());
        registerCommand(new OpenListMenuCommand(this, inventoryManager));
        registerCommand(new ReloadCommand(this, instance, boostManager, inventoryManager));
        registerCommand(new StartBoostCommand(this, boostManager));
    }
}
