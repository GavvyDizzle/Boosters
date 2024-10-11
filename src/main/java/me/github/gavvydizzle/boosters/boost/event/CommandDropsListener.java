package me.github.gavvydizzle.boosters.boost.event;

import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.github.gavvydizzle.commanddrops.event.AttemptRewardRollEvent;
import org.bukkit.event.EventHandler;

public class CommandDropsListener extends SoftDependencyListener {

    public CommandDropsListener(BoostManager boostManager) {
        super(boostManager);
    }

    @EventHandler(ignoreCancelled = true)
    private void onRewardAttempt(AttemptRewardRollEvent e) {
        boostManager.sendEventToBoosts(e);
    }
}
