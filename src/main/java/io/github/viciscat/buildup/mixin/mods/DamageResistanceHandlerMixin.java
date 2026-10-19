package io.github.viciscat.buildup.mixin.mods;

import com.hbm.util.DamageResistanceHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.MeltEffect;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DamageResistanceHandler.class, remap = false)
public class DamageResistanceHandlerMixin {

    @ModifyExpressionValue(method = "calculateDamage", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"),
            @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F")
    })
    private static float calculateDamage(float original, @Local EntityLivingBase entity) {
        return EffectTracker.getOptional(entity)
                .flatMap(tracker -> tracker.getInstance(Effects.MELT))
                .map(MeltEffect::getResistanceMultiplier)
                .map(f -> f * original)
                .orElse(original);
    }
}
