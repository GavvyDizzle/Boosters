package me.github.gavvydizzle.boosters.command.admin;

import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerNameCache;
import com.github.mittenmc.serverutils.SubCommand;
import me.github.gavvydizzle.boosters.BoostPlugin;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.target.GlobalTarget;
import me.github.gavvydizzle.boosters.boost.target.GroupTarget;
import me.github.gavvydizzle.boosters.boost.target.PlayerTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import me.github.gavvydizzle.boosters.command.admin.start.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StartBoostCommand extends SubCommand {

    private static final int MINIMUM_BOOST_MILLIS = 1000;

    private final BoostManager boostManager;
    private final List<StartBoostSubCommand> subCommands;
    private final List<String> activeBoostTypeStrings;

    private final List<String> durationArgs = List.of("duration");

    public StartBoostCommand(CommandManager commandManager, BoostManager boostManager) {
        this.boostManager = boostManager;

        setName("start");
        setDescription("Start a new boost");
        setSyntax("/" + commandManager.getCommandDisplayName() + " start <type> <duration> <target> ...");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());

        subCommands = new ArrayList<>();
        activeBoostTypeStrings = new ArrayList<>();

        subCommands.add(new StartMultiplierBasedBoostCommand(commandManager, boostManager));
        subCommands.add(new StartPotionEffectBoostCommand(commandManager, boostManager));
        subCommands.add(new StartTimedPermissionBoostCommand(commandManager, boostManager));
        subCommands.add(new StartVanillaFishingBoostCommand(commandManager, boostManager));

        // Only keep BoostTypes which are enabled and
        // sub commands which have at least one enabled type
        subCommands.removeIf(StartBoostSubCommand::hasNoTypesEnabled);
        for (StartBoostSubCommand subCommand : subCommands) {
            activeBoostTypeStrings.addAll(subCommand.getEnabledTypes().stream().map(BoostType::name).toList());
        }
    }

    @Nullable
    private StartBoostSubCommand getSubCommand(BoostType type) {
        for (StartBoostSubCommand subCommand : subCommands) {
            if (subCommand.matchesType(type)) return subCommand;
        }
        return null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        BoostType type = BoostType.get(args[1]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Invalid boost type: " + args[1]);
            return;
        } else if (!type.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Disabled boost type: " + args[1]);
            return;
        }

        StartBoostSubCommand command = getSubCommand(type);
        if (command == null) {
            sender.sendMessage(ChatColor.RED + "Invalid boost type: " + args[1]);
            return;
        }

        long completionMillis = Boost.parseCompletionTime(args[2]);
        long now = System.currentTimeMillis();
        if (completionMillis == -1) {
            sender.sendMessage(ChatColor.RED + "Invalid duration: " + args[2]);
            return;
        } else if (completionMillis < now) {
            sender.sendMessage(ChatColor.RED + "The boost must end in the future (current unix time is " +  (now/1000) + ")");
            return;
        } else if (completionMillis - now < MINIMUM_BOOST_MILLIS) {
            sender.sendMessage(ChatColor.RED + "Duration of " + (completionMillis - now) + "ms is too short");
            return;
        }

        BoostTarget target = BoostTarget.parseTarget(args[3]);
        if (target == null) {
            if (args[3].contains(":") && BoostPlugin.getInstance().getLuckPerms() == null) {
                sender.sendMessage(ChatColor.RED + "Group targets are disabled. LuckPerms is not running");
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid targets provided: " + args[3]);
            }
            return;
        }

        Boost boost = command.perform(sender, args, type, target, completionMillis);
        if (boost == null) return;

        // Add a few milliseconds to keep the integer division correct
        int seconds = (int) Math.floorDiv(completionMillis - now + 5, 1000);

        if (boostManager.registerBoost(boost) && sender instanceof Player) {
            sender.sendMessage(ChatColor.YELLOW + "Your boost matches a running boost. Time has been added to that instead of creating a new boost");
        }

        if (sender instanceof Player) {
            if (target instanceof GlobalTarget) {
                sender.sendMessage(ChatColor.GREEN + "Successfully started a " + Numbers.getTimeFormatted(seconds) + " " + type.name() + " boost for all players");
                return;
            }

            StringBuilder sb = new StringBuilder();

            if (target instanceof PlayerTarget playerTarget) {
                playerTarget.getUuids().forEach(uuid -> sb.append(PlayerNameCache.get(uuid)).append(","));
            } else if (target instanceof GroupTarget groupTarget) {
                groupTarget.getGroups().forEach(group -> sb.append("g:").append(group.getName()).append(","));
            }

            if (!sb.isEmpty()) sb.deleteCharAt(sb.length()-1);
            sender.sendMessage(ChatColor.GREEN + "Successfully started a " + Numbers.getTimeFormatted(seconds) + " " + type.name() + " boost for " + sb);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length > 4) {
            BoostType type = BoostType.get(args[1]);
            if (type == null || !type.isEnabled()) return Collections.emptyList();

            StartBoostSubCommand command = getSubCommand(type);
            if (command == null) return Collections.emptyList();

            return command.getSubcommandArguments(sender, args);
        }

        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            StringUtil.copyPartialMatches(args[1], activeBoostTypeStrings, list);
            return list;
        } else if (args.length == 3) {
            return durationArgs;
        } else if (args.length == 4) {
            return null;
        }

        return Collections.emptyList();
    }
}
