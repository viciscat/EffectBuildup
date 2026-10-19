package io.github.viciscat.buildup.mixin.mods;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.srpcotesia.capability.ParasitePlayer;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.MeltEffect;
import net.minecraft.entity.player.EntityPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ParasitePlayer.class, remap = false)
public class ParasitePlayerMixin {

    @ModifyExpressionValue(method = "tryAdapt", at = @At(value = "FIELD", target = "Lcom/srpcotesia/capability/ParasitePlayer;pointReduction:F", opcode = Opcodes.GETFIELD))
    private float pointReduction(float original, @Local EntityPlayer player) {
        return EffectTracker.getOptional(player)
                .flatMap(tracker -> tracker.getInstance(Effects.MELT))
                .map(MeltEffect::getResistanceMultiplier)
                .map(f -> f * original)
                .orElse(original);
    }
}
