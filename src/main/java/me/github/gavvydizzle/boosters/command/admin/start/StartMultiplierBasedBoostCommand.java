package me.github.gavvydizzle.boosters.command.admin.start;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import me.github.gavvydizzle.boosters.boost.type.CommandDropsBoost;
import me.github.gavvydizzle.boosters.boost.type.PrisonEnchantsBoost;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StartMultiplierBasedBoostCommand extends StartBoostSubCommand {

    public StartMultiplierBasedBoostCommand(CommandManager commandManager, BoostManager boostManager) {
        super(List.of(BoostType.COMMAND_DROPS, BoostType.PRISON_ENCHANTS), boostManager);

        setSyntax("/" + commandManager.getCommandDisplayName() + " start <type> <duration> <target> <multiplier>");
    }

    @Override
    public Boost perform(CommandSender sender, String[] args, BoostType type, BoostTarget target, long completionMillis) {
        if (args.length < 5) {
            sender.sendMessage(getColoredSyntax());
            return null;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[4]);
        } catch (Exception ignored) {
            sender.sendMessage(ChatColor.RED + "Invalid multiplier: " + args[4]);
            return null;
        }

        if (multiplier < 0) {
            sender.sendMessage(ChatColor.RED + "Non-positive multiplier: " + args[4]);
            return null;
        }

        if (multiplier <= 1) {
            sender.sendMessage(ChatColor.YELLOW + "A multiplier below 1.0 will slow this boost!");
        }

        VisualBossBar visualBossBar = null;
        if (args.length >= 6) {
            visualBossBar = boostManager.getBossBarManager().createBoostBossBar(type, String.join(" ", Arrays.copyOfRange(args, 5, args.length)));
        }

        return switch (type) {
            case COMMAND_DROPS -> new CommandDropsBoost(target, completionMillis, multiplier, visualBossBar);
            case PRISON_ENCHANTS -> new PrisonEnchantsBoost(target, completionMillis, multiplier, visualBossBar);
            default -> null;
        };
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 5) {
            return List.of("multiplier");
        }
        return Collections.emptyList();
    }
}