package io.github.viciscat.buildup.mixin;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {

    @Expression("? * 5")
    @ModifyExpressionValue(method = "applyPotionDamageCalculations", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int applyPotionDamageCalculations(int original) {
        EffectTracker tracker = EffectTracker.get((EntityLivingBase) (Object) this);
        if (tracker == null) return original;
        return tracker.getInstance(Effects.MELT).map(melt -> (int) (melt.getResistanceMultiplier() * original)).orElse(original);
    }

    @ModifyArg(method = "applyPotionDamageCalculations", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CombatRules;getDamageAfterMagicAbsorb(FF)F"), index = 1)
    private float reduceReduction(float original) {
        EffectTracker tracker = EffectTracker.get((EntityLivingBase) (Object) this);
        if (tracker == null) return original;
        return tracker.getInstance(Effects.MELT).map(melt -> melt.getResistanceMultiplier() * original).orElse(original);
    }
}
