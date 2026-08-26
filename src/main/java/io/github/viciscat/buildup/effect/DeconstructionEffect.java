package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DeconstructionEffect extends EffectInstance{
    protected DeconstructionEffect() {
        super(new StackDurationStrategy.RefreshStrategy(13 * 20));
    }

    float getDamageMultiplier() {
        return 1 + Math.min(30, getStacks()) * 0.03f;
    }

    public static final class EventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void increaseDamage(LivingHurtEvent event) {
            EffectTracker tracker = EffectTracker.get(event.getEntityLiving());
            if (tracker == null) return;
            float v = tracker.getInstance(Effects.DECONSTRUCTION).map(DeconstructionEffect::getDamageMultiplier).orElse(1f);
            event.setAmount(event.getAmount() * v);
        }
    }
}
