package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yeelp.distinctdamagedescriptions.event.classification.DetermineDamageEvent;

public class MegabonkEffect extends StunningEffect {
    private static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("megabonk", 176, 124, 48, 48);
    static {
        MinecraftForge.EVENT_BUS.register(new StunningEffect.EventHandler(Effects.MEGABONK));
    }

    public MegabonkEffect() {
        super(new StackDurationStrategy.RefreshStrategy(2 * 60 * 20)); // 2 minutes
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        setStunTime(4, target);
        target.attackEntityFrom(new DamageSource("fx_bludgeoning"), procDamage * 4.25f);
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        if (getDuration() % 10 != 0) return;
        BasicParticle particle = new BasicParticle(
                target.getEntityWorld(),
                target.getEntityBoundingBox().minX + target.width * BuildupParticleManager.RANDOM.nextFloat(),
                target.getEntityBoundingBox().maxY,
                target.getEntityBoundingBox().minZ + target.width * BuildupParticleManager.RANDOM.nextFloat()
        );
        particle.setMaxAge(2);
        particle.setParticleTexture(PARTICLE);
        BuildupParticleManager.INSTANCE.addParticle(particle);
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.MEGABONK_PROC, 1, 1);
        for (int i = 0; i < 30; i++) {
            float angle = i / 30f * (float) Math.TAU;
            float cos = MathHelper.cos(angle);
            float sin = MathHelper.sin(angle);
            double dx = cos * 0.1;
            double dz = sin * 0.1;
            BasicParticle particle = new BasicParticle(
                    target.getEntityWorld(),
                    target.posX,
                    target.getEyeHeight() + target.posY,
                    target.posZ,
                    dx,
                    0,
                    dz
            );
            particle.setParticleTexture(PARTICLE);
            particle.setMaxAge(10);
            BuildupParticleManager.INSTANCE.addParticle(particle);
        }
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static final class EventHandler {
        @SubscribeEvent
        public static void increaseDamage(DetermineDamageEvent event) {
            EffectTracker tracker = EffectTracker.get(event.getDefender());
            if (tracker == null) return;
            tracker.getInstance(Effects.MEGABONK).ifPresent(megabonk -> {
                float mult = 1 + megabonk.getStacks() * 0.02f;
                event.setDamage(Effects.MEGABONK.getDamageType(), event.getDamage(Effects.MEGABONK.getDamageType()) * mult);
            });
        }
    }
}
