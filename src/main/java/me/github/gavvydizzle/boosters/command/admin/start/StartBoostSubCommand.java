package me.github.gavvydizzle.boosters.command.admin.start;

import com.github.mittenmc.serverutils.SubCommand;
import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.boosters.boost.target.BoostTarget;
import me.github.gavvydizzle.boosters.boost.type.Boost;
import me.github.gavvydizzle.boosters.boost.type.BoostType;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class StartBoostSubCommand extends SubCommand {

    private final List<BoostType> boostTypes;
    protected final BoostManager boostManager;

    protected StartBoostSubCommand(List<BoostType> boostTypes, BoostManager boostManager) {
        this.boostTypes = boostTypes.stream().filter(BoostType::isEnabled).toList();
        this.boostManager = boostManager;
    }

    @Nullable
    public abstract Boost perform(CommandSender sender, String[] args, BoostType type, BoostTarget target, long completionMillis);

    public boolean matchesType(BoostType type) {
        return boostTypes.contains(type);
    }

    public boolean hasNoTypesEnabled() {
        return boostTypes.isEmpty();
    }

    public List<BoostType> getEnabledTypes() {
        return boostTypes;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException();
    }
}
