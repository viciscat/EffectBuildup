package io.github.viciscat.buildup.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.monster.EntityMob;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityAIAttackRangedBow.class)
public class EntityAIAttackRangedBowMixin<T extends EntityMob & IRangedAttackMob> {
    @Shadow
    @Final
    private T entity;

    @ModifyExpressionValue(method = "updateTask", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/EntityAIAttackRangedBow;attackCooldown:I", opcode = Opcodes.GETFIELD))
    private int increaseCooldown(int original) {
        return EffectTracker.getOptional(entity)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> Math.round((1 + f) * original))
                .orElse(original);
    }
}
