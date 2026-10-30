package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import io.github.viciscat.buildup.particle.ParticleDestinedDeath;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.UUID;

public class DestinedDeathEffect extends EffectInstance {
    private float combinedProcDamage;
    private static final UUID MODIFIER_UUID = UUID.fromString("ceae796f-6ab9-4265-8787-bba1e7ac498e");
    protected DestinedDeathEffect() {
        super(new StackDurationStrategy.RefreshStrategy(2 * 60 * 60 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, float procDamage, @Nullable DamageSource source) {
        super.proc(target, procDamage, source);
        combinedProcDamage += this.procDamage;
        applyModifier(target);
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 5 == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.DESTINED_DEATH_PROC, 1, 1);
        for (int i = 0; i < 10; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d positionAround = BuildupParticleManager.randomPositionAround(target);
        double randomY = target.height * BuildupParticleManager.RANDOM.nextDouble() * 0.6;
        BuildupParticleManager.INSTANCE.addParticle(new ParticleDestinedDeath(target.getEntityWorld(), positionAround.add(0, randomY, 0)));
    }

    private void applyModifier(EntityLivingBase target) {
        applyModifier(
                target,
                SharedMonsterAttributes.MAX_HEALTH,
                new AttributeModifier(MODIFIER_UUID, "Destined Death Effect", -combinedProcDamage * 0.01, Constants.AttributeModifierOperation.ADD)
        );
    }

    @Override
    protected void onStackChange(EntityLivingBase target) {
        super.onStackChange(target);
        applyModifier(target);
    }

    @Override
    public void end(EntityLivingBase target) {
        super.end(target);
        target.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(MODIFIER_UUID);
    }
}
