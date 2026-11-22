package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DeconstructionEffect extends EffectInstance{
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("deconstruction", 144, 76, 16, 16);
    protected DeconstructionEffect() {
        super(new StackDurationStrategy.RefreshStrategy(60 * 20));
    }

    float getDamageMultiplier() {
        float multiplier = 1f;
        int stacks = getStacks();
        multiplier += Math.clamp(stacks, 0, 20) * 0.05f;
        stacks -= 20;
        multiplier += Math.max(stacks, 0) * 0.00125f;
        return multiplier;
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
        BuildupSounds.playAt(target, BuildupSounds.DECONSTRUCTION_PROC, 1, 1);
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

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static final class EventHandler {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void increaseDamage(LivingHurtEvent event) {
            EffectTracker tracker = EffectTracker.get(event.getEntityLiving());
            if (tracker == null) return;
            float v = tracker.getInstance(Effects.DECONSTRUCTION).map(DeconstructionEffect::getDamageMultiplier).orElse(1f);
            event.setAmount(event.getAmount() * v);
        }
    }
}
