package com.example.mobmind.species;

import com.example.mobmind.MobMindMod;
import com.example.mobmind.hive.HiveRole;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ZombieSpeciesProfile implements SpeciesProfile {
    private static final int LEADER_GLOW_DURATION_TICKS = 240;
    private static final Component LEADER_NAME = Component.literal("觉醒僵尸首领");
    private static final ResourceLocation LEADER_HEALTH_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(MobMindMod.MODID, "awakened_zombie_leader_health");
    private static final ResourceLocation LEADER_ATTACK_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(MobMindMod.MODID, "awakened_zombie_leader_attack");
    private static final ResourceLocation LEADER_SCALE_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(MobMindMod.MODID, "awakened_zombie_leader_scale");
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
        return 32.0D;
    }

    @Override
    public int groupCapacity() {
        return 6;
    }

    @Override
    public List<HiveRole> roleTemplate() {
        return ROLE_TEMPLATE;
    }

    @Override
    public void applyLeaderPresentation(Zombie leader) {
        leader.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        leader.setDropChance(EquipmentSlot.HEAD, 0.0F);
        applyLeaderHighlight(leader);
        leader.setCustomName(LEADER_NAME);
        leader.setCustomNameVisible(true);
        addLeaderAttribute(leader.getAttribute(Attributes.MAX_HEALTH), LEADER_HEALTH_MODIFIER, 0.20D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addLeaderAttribute(leader.getAttribute(Attributes.ATTACK_DAMAGE), LEADER_ATTACK_MODIFIER, 1.0D, AttributeModifier.Operation.ADD_VALUE);
        addLeaderAttribute(leader.getAttribute(Attributes.SCALE), LEADER_SCALE_MODIFIER, 0.35D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        leader.refreshDimensions();
        leader.setHealth(Math.min(leader.getMaxHealth(), leader.getHealth() + 4.0F));
    }

    @Override
    public void applyLeaderHighlight(Zombie leader) {
        leader.setGlowingTag(true);
        leader.addEffect(new MobEffectInstance(MobEffects.GLOWING, LEADER_GLOW_DURATION_TICKS, 0, true, false, false));
    }

    private static void addLeaderAttribute(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        if (attribute != null) {
            attribute.addOrReplacePermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }
}
