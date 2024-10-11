package me.github.gavvydizzle.boosters.boost.target;

import lombok.Getter;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupTarget extends BoostTarget {

    @Getter private final List<Group> groups;
    private final List<String> groupPermissions;
    private String targetString;

    public GroupTarget(Group group) {
        this(List.of(group));
    }

    public GroupTarget(List<Group> groups) {
        super(TargetType.GROUP);

        this.groups = groups;
        groupPermissions = new ArrayList<>();

        for (Group group : groups) {
            groupPermissions.add("group." + group.getName());
        }
    }

    @Override
    public boolean isTarget(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;
        return isTarget(player);
    }

    @Override
    public boolean isTarget(Player player) {
        for (String permission : groupPermissions) {
            if (!player.hasPermission(permission)) return false;
        }
        return true;
    }

    @Override
    public boolean doesDataMatch(BoostTarget target) {
        return target instanceof GroupTarget t && groupPermissions.equals(t.groupPermissions);
    }

    @Override
    public String getStringTargets() {
        if (targetString == null) {
            StringBuilder sb = new StringBuilder();

            for (Group group : groups) {
                sb.append("g:").append(group.getName()).append(",");
            }
            sb.deleteCharAt(sb.length()-1);

            targetString = sb.toString();
        }

        return targetString;
    }

    @Override
    public Map<String, Object> serializeData() {
        return Map.of("target.list", groups.stream().map(Group::getName).toList());
    }
}
