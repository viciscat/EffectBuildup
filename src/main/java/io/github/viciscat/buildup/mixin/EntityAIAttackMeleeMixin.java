package io.github.viciscat.buildup.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = EntityAIAttackMelee.class)
public class EntityAIAttackMeleeMixin {
    @Shadow
    protected EntityCreature attacker;

    @ModifyConstant(method = "checkAndPerformAttack", constant = @Constant(intValue = 20))
    private int increaseAttackTime(int constant) {
        return EffectTracker.getOptional(attacker)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> Math.round((1 + f) * constant))
                .orElse(constant);
    }
}
