package me.github.gavvydizzle.boosters.command.admin.start;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import me.github.gavvydizzle.boosters.boost.type.TimedPermissionBoost;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StartTimedPermissionBoostCommand extends StartBoostSubCommand {

    public StartTimedPermissionBoostCommand(CommandManager commandManager, BoostManager boostManager) {
        super(List.of(BoostType.LEVELS, BoostType.SHOPGUIPLUS_SELL_BOOST), boostManager);

        setSyntax("/" + commandManager.getCommandDisplayName() + " start <type> <duration> <target> <permission>");
    }

    @Override
    public Boost perform(CommandSender sender, String[] args, BoostType type, BoostTarget target, long completionMillis) {
        if (args.length < 5) {
            sender.sendMessage(getColoredSyntax());
            return null;
        }

        VisualBossBar visualBossBar = null;
        if (args.length >= 6) {
            visualBossBar = boostManager.getBossBarManager().createBoostBossBar(type, String.join(" ", Arrays.copyOfRange(args, 5, args.length)));
        }

        return new TimedPermissionBoost(type, target, completionMillis, args[4], visualBossBar);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 5) {
            return List.of("permission");
        }
        return Collections.emptyList();
    }
}