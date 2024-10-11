package me.github.gavvydizzle.boosters.boost.event;

import me.github.gavvydizzle.boosters.boost.BoostManager;
import me.wax.prisonenchants.events.AttemptEnchantActivationEvent;
import org.bukkit.event.EventHandler;

public class PrisonEnchantsListener extends SoftDependencyListener {

    public PrisonEnchantsListener(BoostManager boostManager) {
        super(boostManager);
    }

    @EventHandler(ignoreCancelled = true)
    private void onCustomEnchantAttempt(AttemptEnchantActivationEvent e) {
        boostManager.sendEventToBoosts(e);
    }
}
