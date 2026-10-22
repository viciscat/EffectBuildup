package io.github.viciscat.buildup.mixin;

import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import io.github.viciscat.buildup.effect.StunningEffect;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityAIAttackRanged.class)
public class EntityAIAttackRangedMixin {
    @Shadow
    @Final
    private EntityLiving entityHost;

    @ModifyArg(method = "updateTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(F)I"), index = 0)
    private float increaseDelay(float value) {
        return EffectTracker.getOptional(entityHost)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> (1 + f) * value)
                .orElse(value);
    }
}
