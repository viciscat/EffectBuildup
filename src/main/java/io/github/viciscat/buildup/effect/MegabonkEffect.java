package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.ddd_effect_buildup.Tags;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yeelp.distinctdamagedescriptions.event.classification.DetermineDamageEvent;

public class MegabonkEffect extends StunningEffect {
    static {
        MinecraftForge.EVENT_BUS.register(new StunningEffect.EventHandler(Effects.MEGABONK));
    }

    public MegabonkEffect() {
        super(new StackDurationStrategy.RefreshStrategy(2 * 60 * 20)); // 2 minutes
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        stunTime = 16;
        target.attackEntityFrom(new DamageSource("fx_bludgeoning"), procDamage * 4.25f);
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
