package io.github.viciscat.buildup.mixin.mods;

import chumbanotz.mutantbeasts.entity.ai.MBEntityAIAttackMelee;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MBEntityAIAttackMelee.class, remap = false)
public class MBEntityAIAttackMeleeMixin extends EntityAIAttackMelee {

    public MBEntityAIAttackMeleeMixin(EntityCreature creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
    }

    @ModifyExpressionValue(
            method = "checkAndPerformAttack",
            remap = true,
            at = @At(value = "FIELD", target = "Lchumbanotz/mutantbeasts/entity/ai/MBEntityAIAttackMelee;maxAttackTick:I", opcode = Opcodes.GETFIELD, remap = false)
    )
    private int increaseCooldown(int original) {
        return EffectTracker.getOptional(attacker)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> Math.round((1 + f) * original))
                .orElse(original);
    }
}
