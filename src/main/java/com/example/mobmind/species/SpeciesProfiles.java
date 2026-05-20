package com.example.mobmind.species;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;

public final class SpeciesProfiles {
    private static final List<SpeciesProfile> PROFILES = List.of(ZombieSpeciesProfile.INSTANCE);

    private SpeciesProfiles() {
    }

    public static SpeciesProfile forMonster(Monster monster) {
        return PROFILES.stream()
                .filter(profile -> profile.matches(monster))
                .findFirst()
                .orElse(null);
    }

    public static boolean matchesSpecies(Monster monster, ResourceLocation speciesId) {
        SpeciesProfile profile = forMonster(monster);
        return profile != null && profile.id().equals(speciesId);
    }
}
