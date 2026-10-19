package io.github.viciscat.buildup.mixin.mods;

import c4.conarm.common.armor.modifiers.ModResistantType;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.MeltEffect;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ModResistantType.class, remap = false)
public class ModResistantTypeMixin {

    @Expression("? * ? * ?")
    @ModifyExpressionValue(method = "onHurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float onHurt(float original, @Local EntityPlayer player) {
        return EffectTracker.getOptional(player)
                .flatMap(tracker -> tracker.getInstance(Effects.MELT))
                .map(MeltEffect::getResistanceMultiplier)
                .map(f -> f * original)
                .orElse(original);
    }
}
