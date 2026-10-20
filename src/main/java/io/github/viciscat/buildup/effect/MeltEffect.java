package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class MeltEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("melt", 176, 64, 12, 12);
    private static final UUID MODIFIER_UUID = UUID.fromString("33fad0d8-3802-4d5a-81eb-9a2383cd8ba3");
    private static final String[] ATTRIBUTES_TO_REDUCE  = new String[]{
            "generic.armor",
            "generic.armorToughness",
            "potioncore.damageResistance",
            "potioncore.magicShielding",
    };

    public MeltEffect() {
        super(new StackDurationStrategy.RefreshStrategy(120 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        applyModifiers(target);
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 3 == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.MELT_PROC, 1, 1);
        for (int i = 0; i < 10; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d particlePos = BuildupParticleManager.randomPositionAround(target);
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                particlePos.x,
                particlePos.y + target.height * 0.2f + BuildupParticleManager.RANDOM.nextFloat() * target.height * 0.6f,
                particlePos.z
        );
        particle.setGravity(1f);
        particle.setMaxAge(20);
        particle.setParticleTexture(PARTICLE);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    @Override
    protected void onStackChange(EntityLivingBase target) {
        super.onStackChange(target);
        applyModifiers(target);
    }

    public float getResistanceMultiplier() {
        float multiplier = 1f;
        int stacks = getStacks();
        multiplier -= Math.clamp(stacks, 0, 3) * 0.1f;
        stacks -= 3;
        multiplier -= Math.clamp(stacks, 0, 4) * 0.05f;
        stacks -= 4;
        multiplier -= Math.clamp(stacks, 0, 15) * 0.02f;
        stacks -= 15;
        multiplier -= Math.max(stacks, 0) * 0.0125f;
        return multiplier;
    }

    private void applyModifiers(EntityLivingBase target) {
        AttributeModifier modifier = new AttributeModifier(MODIFIER_UUID, "Melt Effect", 1 - getResistanceMultiplier(), Constants.AttributeModifierOperation.MULTIPLY);
        Arrays.stream(ATTRIBUTES_TO_REDUCE)
                .map(attr -> target.getAttributeMap().getAttributeInstanceByName(attr))
                .filter(Objects::nonNull)
                .forEach(instance -> applyModifier(instance, modifier));
    }
}
