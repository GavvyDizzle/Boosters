package me.github.gavvydizzle.boosters.boost.target;

import com.github.mittenmc.serverutils.utils.GeneralUtils;
import lombok.Getter;
import me.github.gavvydizzle.boosters.BoostPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the targets of a boost which resolve to {@link org.bukkit.entity.Player} objects.
 */
@Getter
public abstract class BoostTarget {

    private final TargetType type;

    protected BoostTarget(TargetType type) {
        this.type = type;
    }

    public abstract boolean isTarget(UUID uuid);

    public abstract Map<String, Object> serializeData();

    public boolean isTarget(Player player) {
        return isTarget(player.getUniqueId());
    }

    public Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream().filter(this::isTarget).toList();
    }

    public boolean matches(BoostTarget target) {
        return this.type == target.type && doesDataMatch(target);
    }

    public abstract boolean doesDataMatch(BoostTarget target);

    public abstract String getStringTargets();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof BoostTarget boostTarget && this.matches(boostTarget);
    }

    /**
     * Determines if two boost targets have overlapping targets.
     * Any {@link PlayerTarget} is assumed to have a single player attached.
     * A {@link GlobalTarget} matches any other target.
     * @param t1 Target one
     * @param t2 Target two
     * @return If the targets have any overlap, false otherwise
     */
    public static boolean overlap(BoostTarget t1, BoostTarget t2) {
        if (t1 instanceof GlobalTarget || t2 instanceof GlobalTarget) return true;
        else if (t1 instanceof PlayerTarget pt1) {
            Optional<UUID> uuid = pt1.getUuids().stream().findFirst();
            return uuid.filter(t2::isTarget).isPresent();
        }
        else if (t2 instanceof PlayerTarget pt2) {
            Optional<UUID> uuid = pt2.getUuids().stream().findFirst();
            return uuid.filter(t1::isTarget).isPresent();
        }

        return false;
    }

    @Override
    public String toString() {
        return getStringTargets();
    }

    /**
     * Parses a {@link BoostTarget} from the given string.
     * <p>
     * The input string can be in one of the following formats:
     * <ul>
     *   <li>The "*" character to represent all players.</li>
     *   <li>A single player name or a comma-delimited list of player names.</li>
     *   <li>A group or a comma-delimited list of groups, where each group is prefixed by "g:", e.g., "g:vip".</li>
     * </ul>
     * </p>
     *
     * @param str The input string representing the boost target (players or groups).
     * @return The parsed {@link BoostTarget} object, or {@code null} if the input is invalid.
     */
    @Nullable
    public static BoostTarget parseTarget(String str) {
        if (str.equals("*")) return new GlobalTarget();

        String[] arr = str.split(",");

        if (arr[0].contains(":")) {
            LuckPerms luckPerms = BoostPlugin.getInstance().getLuckPerms();
            if (luckPerms == null) return null;

            List<Group> groups = new ArrayList<>();

            for (String s : arr) {
                String[] arr2 = s.split(":");
                if (arr2.length < 2) continue;

                String groupName = arr2[1];
                if (luckPerms.getGroupManager().isLoaded(groupName)) groups.add(luckPerms.getGroupManager().getGroup(groupName));
            }

            if (groups.isEmpty()) return null;
            return new GroupTarget(groups);
        }
        else {
            List<OfflinePlayer> players = new ArrayList<>();

            for (String s : arr) {
                OfflinePlayer offlinePlayer = GeneralUtils.getOfflinePlayer(s);
                if (offlinePlayer != null) players.add(offlinePlayer);
            }

            if (players.isEmpty()) return null;
            return new PlayerTarget(players);
        }
    }

    /**
     * Parses a {@link BoostTarget} from the given ConfigurationSection.
     * @param section The ConfigurationSection to parse
     * @return The target or null
     */
    @Nullable
    public static BoostTarget parseTarget(@Nullable ConfigurationSection section) {
        if (section == null) return null;

        TargetType type = TargetType.get(section.getString("type"));
        if (type == null) return null;

        switch (type) {
            case GLOBAL -> {
                return new GlobalTarget();
            }
            case GROUP -> {
                LuckPerms luckPerms = BoostPlugin.getInstance().getLuckPerms();
                if (luckPerms == null) return null;

                List<Group> groups = new ArrayList<>();

                for (String s : section.getStringList("list")) {
                    if (luckPerms.getGroupManager().isLoaded(s)) groups.add(luckPerms.getGroupManager().getGroup(s));
                }

                if (groups.isEmpty()) return null;
                return new GroupTarget(groups);
            }
            case PLAYER -> {
                Set<UUID> uuids = new HashSet<>();

                for (String s : section.getStringList("list")) {
                    try {
                        uuids.add(UUID.fromString(s));
                    } catch (Exception ignored) {}
                }

                if (uuids.isEmpty()) return null;
                return new PlayerTarget(uuids);
            }
        }
        return null;
    }
}
