package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class DecayedEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("decayed", 160, 76, 16, 16);
    public DecayedEffect() {
        super(new StackDurationStrategy.RefreshStrategy(20 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, @Nullable DamageSource source) {
        super.proc(target, procDamage, source);
        if (getStacks() >= 14) {
            stackDurationStrategy.setStacks(8);
            target.attackEntityFrom(new DamageSource("fx_necrotic").setDamageBypassesArmor(), procDamage * 12.5f);
            BuildupSounds.playAt(target, BuildupSounds.DECAYED_20_PROC, 1, 1);
        } else {
            BuildupSounds.playAt(target, BuildupSounds.DECAYED_PROC, 1, 1);
        }
    }

    public float getHealMultiplier() {
        return 1f - (getStacks() * 0.1f);
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 10 == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);

        for (int i = 0; i < 5; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d particlePos = BuildupParticleManager.randomPositionAround(target);
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                particlePos.x,
                particlePos.y + target.height * 0.8f + BuildupParticleManager.RANDOM.nextFloat() * target.height * 0.1f,
                particlePos.z,
                0,
                0.02,
                0
        );
        particle.setMaxAge(20);
        particle.setParticleTexture(PARTICLE);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            EffectTracker tracker = EffectTracker.get(event.getEntityLiving());
            if (tracker == null) return;
            event.setAmount(tracker.getInstance(Effects.DECAYED).map(DecayedEffect::getHealMultiplier).orElse(1f) * event.getAmount());
        }
    }
}
