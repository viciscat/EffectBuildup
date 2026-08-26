package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public class JudgementEffect extends EffectInstance {
    public JudgementEffect() {
        super(new StackDurationStrategy.DecayingStackStrategy(10));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, @Nullable DamageSource source) {
        super.proc(target, procDamage, source);
        if (getStacks() > 6) stackDurationStrategy.setStacks(6);
    }

    public float getDamageReduction() {
        return 1 - (getStacks() * 0.1f);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingHurt(LivingHurtEvent event) {
            // optional chain BECAUSE I CAN HAHAHAHA
            Optional.ofNullable(event.getSource().getTrueSource())
                    .filter(EntityLivingBase.class::isInstance)
                    .map(EntityLivingBase.class::cast)
                    .map(EffectTracker::get)
                    .flatMap(tracker -> tracker.getInstance(Effects.JUDGEMENT))
                    .ifPresent(judgement -> event.setAmount(judgement.getDamageReduction() * event.getAmount()));
        }
    }
}
