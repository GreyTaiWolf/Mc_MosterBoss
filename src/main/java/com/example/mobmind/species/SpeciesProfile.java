package com.example.mobmind.species;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;

public interface SpeciesProfile {
    ResourceLocation id();

    boolean matches(Monster monster);
}
