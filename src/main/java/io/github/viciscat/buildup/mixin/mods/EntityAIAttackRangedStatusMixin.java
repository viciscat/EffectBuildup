package io.github.viciscat.buildup.mixin.mods;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAIAttackRangedStatus;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = EntityAIAttackRangedStatus.class, remap = false)
public class EntityAIAttackRangedStatusMixin {

    @Shadow
    @Final
    private EntityParasiteBase entityHost;

    @ModifyArg(method = "updateTask",
            remap = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(F)I", remap = false),
            index = 0)
    private float increaseDelay(float value) {
        return EffectTracker.getOptional(entityHost)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> (1 + f) * value)
                .orElse(value);
    }
}
