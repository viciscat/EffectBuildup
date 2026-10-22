package io.github.viciscat.buildup.mixin.mods;

import com.github.alexthe666.iceandfire.entity.ai.CyclopsAIAttackMelee;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = CyclopsAIAttackMelee.class, remap = false)
public class CyclopsAIAttackMeleeMixin extends EntityAIAttackMelee {

    public CyclopsAIAttackMeleeMixin(EntityCreature creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
    }

    @ModifyConstant(
            method = "checkAndPerformAttack",
            constant = @Constant(intValue = 20))
    private int increaseCooldown(int original) {
        return EffectTracker.getOptional(attacker)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> Math.round((1 + f) * original))
                .orElse(original);
    }
}
