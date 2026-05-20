package com.example.mobmind.species;

import com.example.mobmind.hive.HiveRole;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;

public interface SpeciesProfile {
    String speciesId();

    boolean isEligibleLeader(Entity entity);

    double groupRadius();

    int groupCapacity();

    List<HiveRole> roleTemplate();

    void applyLeaderPresentation(Zombie leader);

    void applyLeaderHighlight(Zombie leader);
}
