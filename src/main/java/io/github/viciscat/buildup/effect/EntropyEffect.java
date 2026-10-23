package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntropyEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("entropy", 128, 76, 16, 16);
    private static final String DAMAGE_TYPE = "fx_psychic";

    public EntropyEffect() {
        super(new StackDurationStrategy.RefreshStrategy(60 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        stackDurationStrategy.setStacks(Math.min(8, stackDurationStrategy.getStacks()));
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 2 == 0) {
            makeParticle(target);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.ENTROPY_PROC, 1, 1);
        for (int i = 0; i < 10; i++) {
            makeParticle(target);
        }
    }

    private static void makeParticle(EntityLivingBase target) {
        Vec3d particlePos = BuildupParticleManager.randomPositionAround(target, 0.2f);
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                particlePos.x,
                particlePos.y + BuildupParticleManager.RANDOM.nextFloat() * target.height,
                particlePos.z
        );
        particle.setMaxAge(2);
        particle.setParticleTexture(PARTICLE);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void onHurt(LivingHurtEvent event) {
            if (event.getSource().getDamageType().equals(DAMAGE_TYPE)) return;
            EntityLivingBase target = event.getEntityLiving();
            EffectTracker tracker = EffectTracker.get(target);
            if (tracker == null) return;
            tracker.getInstance(Effects.ENTROPY).ifPresent(entropy -> {
                float amount = event.getAmount();
                float multiplier = 1.1f;
                if (entropy.getStacks() >= 10) {
                    multiplier += 0.5f + (entropy.getStacks() - 10) * 0.00125f;
                } else {
                    multiplier += entropy.getStacks() * 0.05f;
                }
                target.attackEntityFrom(entropy.dmgSrc(DAMAGE_TYPE).setDamageBypassesArmor(), amount * multiplier);
            });
        }
    }
}
