package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntropyEffect extends EffectInstance {
    private static final DamageSource DAMAGE_SOURCE = new DamageSource("fx_psychic").setDamageBypassesArmor();
    public EntropyEffect() {
        super(new StackDurationStrategy.RefreshStrategy(8 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        stackDurationStrategy.setStacks(Math.min(8, stackDurationStrategy.getStacks()));
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void onHurt(LivingHurtEvent event) {
            if (event.getSource().getDamageType().equals(DAMAGE_SOURCE.getDamageType())) return;
            EntityLivingBase target = event.getEntityLiving();
            EffectTracker tracker = EffectTracker.get(target);
            if (tracker == null) return;
            tracker.getInstance(Effects.ENTROPY).ifPresent(entropy -> {
                float amount = event.getAmount();
                target.attackEntityFrom(DAMAGE_SOURCE, amount * (1 + 0.05f * entropy.getStacks()));
            });
        }
    }
}
