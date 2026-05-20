package com.example.mobmind.species;

import com.example.mobmind.hive.HiveRole;
import com.example.mobmind.hive.HiveSelector;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.Nullable;

public final class SpeciesProfiles {
    private static final SpeciesProfile ZOMBIE = new SpeciesProfile() {
        private static final List<HiveRole> ROLE_TEMPLATE = List.of(
                HiveRole.GUARD,
                HiveRole.GUARD,
                HiveRole.MINER,
                HiveRole.PATROL,
                HiveRole.BUILDER);

        @Override
        public String speciesId() {
            return "zombie";
        }

        @Override
        public boolean isEligibleLeader(Entity entity) {
            return entity instanceof Zombie zombie && zombie.getType() == EntityType.ZOMBIE;
        }

        @Override
        public double groupRadius() {
            return HiveSelector.GROUP_RADIUS;
        }

        @Override
        public int groupCapacity() {
            return HiveSelector.LEVEL_ONE_CAPACITY;
        }

        @Override
        public List<HiveRole> roleTemplate() {
            return ROLE_TEMPLATE;
        }

        @Override
        public void applyLeaderPresentation(Zombie leader) {
            HiveSelector.applyLeaderPresentation(leader);
        }

        @Override
        public void applyLeaderHighlight(Zombie leader) {
            HiveSelector.applyLeaderHighlight(leader);
        }
    };

    private SpeciesProfiles() {
    }

    public static SpeciesProfile zombie() {
        return ZOMBIE;
    }

    @Nullable
    public static SpeciesProfile resolve(Entity entity) {
        return ZOMBIE.isEligibleLeader(entity) ? ZOMBIE : null;
    }
}
