package me.github.gavvydizzle.boosters.command.admin.start;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import me.github.gavvydizzle.boosters.boost.type.VanillaFishingBoost;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StartVanillaFishingBoostCommand extends StartBoostSubCommand {

    private final List<String> hookTypeArgs = Arrays.stream(VanillaFishingBoost.WaitType.values()).map(VanillaFishingBoost.WaitType::name).toList();

    public StartVanillaFishingBoostCommand(CommandManager commandManager, BoostManager boostManager) {
        super(List.of(BoostType.VANILLA_FISHING), boostManager);

        setSyntax("/" + commandManager.getCommandDisplayName() + " start <type> <duration> <target> <multiplier> <hook_target>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
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

        VanillaFishingBoost.WaitType waitType = VanillaFishingBoost.WaitType.get(args[5]);
        if (waitType == null) {
            sender.sendMessage(ChatColor.RED + "Invalid hook type: " + args[5]);
            return null;
        }

        VisualBossBar visualBossBar = null;
        if (args.length >= 7) {
            visualBossBar = boostManager.getBossBarManager().createBoostBossBar(type, String.join(" ", Arrays.copyOfRange(args, 6, args.length)));
        }

        return new VanillaFishingBoost(target, completionMillis, multiplier, waitType, visualBossBar);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 5) {
            return List.of("multiplier");
        } else if (args.length == 6) {
            List<String> list = new ArrayList<>();
            StringUtil.copyPartialMatches(args[5], hookTypeArgs, list);
            return list;
        }
        return Collections.emptyList();
    }
}