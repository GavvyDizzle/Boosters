package me.github.gavvydizzle.boosters.command.admin.start;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.bossbar.VisualBossBar;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import me.github.gavvydizzle.boosters.boost.type.PotionEffectBoost;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartPotionEffectBoostCommand extends StartBoostSubCommand {

    private final List<String> potionEffectStrings = Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).toList();

    public StartPotionEffectBoostCommand(CommandManager commandManager, BoostManager boostManager) {
        super(List.of(BoostType.POTION_EFFECT), boostManager);

        setSyntax("/" + commandManager.getCommandDisplayName() + " start <type> <duration> <target> <effect> <amplifier>");
    }

    @Override
    public Boost perform(CommandSender sender, String[] args, BoostType type, BoostTarget target, long completionMillis) {
        if (args.length < 6) {
            sender.sendMessage(getColoredSyntax());
            return null;
        }

        PotionEffectType effectType = PotionEffectType.getByName(args[4]);
        if (effectType == null) {
            sender.sendMessage(ChatColor.RED + "Invalid effect type: " + args[4]);
            return null;
        }

        int amplifier;
        try {
            amplifier = Integer.parseInt(args[5]);
        } catch (Exception ignored) {
            sender.sendMessage("Invalid amplifier: " + args[5]);
            return null;
        }

        if (amplifier < 0) {
            sender.sendMessage("Non-positive amplifier: " + args[5]);
            return null;
        }

        VisualBossBar visualBossBar = null;
        if (args.length >= 7) {
            visualBossBar = boostManager.getBossBarManager().createBoostBossBar(type, String.join(" ", Arrays.copyOfRange(args, 6, args.length)));
        }

        return new PotionEffectBoost(target, completionMillis, effectType, amplifier, visualBossBar);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 5) {
            StringUtil.copyPartialMatches(args[4], potionEffectStrings, list);
        } else if (args.length == 6) {
            return List.of("amplifier");
        }
        return list;
    }
}
