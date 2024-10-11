package me.github.gavvydizzle.boosters.boost.event;

import me.github.gavvydizzle.boosters.boost.BoostManager;
import org.bukkit.event.Listener;

public class SoftDependencyListener implements Listener {

    protected final BoostManager boostManager;

    protected SoftDependencyListener(BoostManager boostManager) {
        this.boostManager = boostManager;
    }
}
