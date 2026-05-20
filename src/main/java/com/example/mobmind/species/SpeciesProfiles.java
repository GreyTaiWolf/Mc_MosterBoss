package com.example.mobmind.species;

import java.util.Map;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class SpeciesProfiles {
    private static final SpeciesProfile ZOMBIE = new ZombieSpeciesProfile();
    private static final Map<String, SpeciesProfile> PROFILES = Map.of(
            ZOMBIE.speciesId(), ZOMBIE);

    private SpeciesProfiles() {
    }

    public static SpeciesProfile zombie() {
        return ZOMBIE;
    }

    @Nullable
    public static SpeciesProfile resolve(Entity entity) {
        return ZOMBIE.isEligibleLeader(entity) ? ZOMBIE : null;
    }

    public static Optional<SpeciesProfile> byId(String speciesId) {
        return Optional.ofNullable(PROFILES.get(speciesId));
    }
}
