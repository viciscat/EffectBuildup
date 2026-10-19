package io.github.viciscat.buildup.mixin.mods;

import c4.champions.common.affix.affix.AffixAdaptable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.MeltEffect;
import net.minecraft.entity.EntityLiving;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AffixAdaptable.class, remap = false)
public class AffixAdaptableMixin {

    @ModifyExpressionValue(method = "onHurt", at = @At(value = "FIELD", target = "Lc4/champions/common/config/ConfigHandler$Affix$Adaptable;damageReductionIncrement:D", opcode = Opcodes.GETFIELD))
    private double reduceReduction(double original, @Local(argsOnly = true) EntityLiving living) {
        return EffectTracker.getOptional(living)
                .flatMap(tracker -> tracker.getInstance(Effects.MELT))
                .map(MeltEffect::getResistanceMultiplier)
                .map(f -> f * original)
                .orElse(original);
    }
}
