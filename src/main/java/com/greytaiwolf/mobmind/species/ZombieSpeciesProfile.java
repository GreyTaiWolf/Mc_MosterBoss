package com.greytaiwolf.mobmind.species;

import com.greytaiwolf.mobmind.MobMindMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;

public final class ZombieSpeciesProfile implements SpeciesProfile {
    public static final ZombieSpeciesProfile INSTANCE = new ZombieSpeciesProfile();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MobMindMod.MODID, "zombie");

    private ZombieSpeciesProfile() {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean matches(Monster monster) {
        return monster.getType() == EntityType.ZOMBIE;
    }
}
