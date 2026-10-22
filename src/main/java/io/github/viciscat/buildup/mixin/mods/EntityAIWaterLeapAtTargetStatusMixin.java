package io.github.viciscat.buildup.mixin.mods;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAIWaterLeapAtTargetStatus;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityAIWaterLeapAtTargetStatus.class, remap = false)
public class EntityAIWaterLeapAtTargetStatusMixin {

    @Shadow
    private EntityParasiteBase leaper;

    @ModifyExpressionValue(
            method = "updateTask",
            remap = true,
            at = @At(value = "FIELD", target = "Lcom/dhanantry/scapeandrunparasites/entity/ai/EntityAIWaterLeapAtTargetStatus;jCooldown:I", opcode = Opcodes.GETFIELD, remap = false)
    )
    private int increaseCooldown(int original) {
        return EffectTracker.getOptional(leaper)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> Math.round((1 + f) * original))
                .orElse(original);
    }
}
