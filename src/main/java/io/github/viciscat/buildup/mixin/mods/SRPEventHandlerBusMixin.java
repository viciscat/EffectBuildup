package io.github.viciscat.buildup.mixin.mods;

import com.dhanantry.scapeandrunparasites.util.handlers.SRPEventHandlerBus;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.MeltEffect;
import net.minecraft.entity.EntityLivingBase;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SRPEventHandlerBus.class, remap = false)
public class SRPEventHandlerBusMixin {

    @ModifyExpressionValue(method = "entityHurt", at = {
            @At(value = "FIELD", target = "Lcom/dhanantry/scapeandrunparasites/util/config/SRPConfig;sentientPointReduction:F", opcode = Opcodes.GETSTATIC),
            @At(value = "FIELD", target = "Lcom/dhanantry/scapeandrunparasites/util/config/SRPConfig;livingPointReduction:F", opcode = Opcodes.GETSTATIC),
    }, require = 2)
    private float reduceReduction(float original, @Local(name = "entity") EntityLivingBase entity) {
        return EffectTracker.getOptional(entity)
                .flatMap(tracker -> tracker.getInstance(Effects.MELT))
                .map(MeltEffect::getResistanceMultiplier)
                .map(f -> f * original)
                .orElse(original);
    }
}
