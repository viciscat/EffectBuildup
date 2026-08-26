package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class DecayedEffect extends EffectInstance {
    public DecayedEffect() {
        super(new StackDurationStrategy.RefreshStrategy(20 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, @Nullable DamageSource source) {
        super.proc(target, procDamage, source);
        if (getStacks() >= 20) {
            stackDurationStrategy.setStacks(8);
            target.attackEntityFrom(new DamageSource("fx_necrotic").setDamageBypassesArmor(), procDamage * 1.2f);
        }
    }

    public float getHealMultiplier() {
        return 1f - (getStacks() * 0.1f);
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
