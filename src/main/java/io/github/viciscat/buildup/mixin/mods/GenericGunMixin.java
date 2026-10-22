package io.github.viciscat.buildup.mixin.mods;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.FrozenEffect;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import techguns.items.guns.GenericGun;

@Mixin(value = GenericGun.class, remap = false)
public class GenericGunMixin {


    @ModifyArg(method = "shootGunPrimary", at = @At(value = "INVOKE", target = "Ltechguns/capabilities/TGExtendedPlayer;setFireDelay(Lnet/minecraft/util/EnumHand;I)V"), index = 1)
    private int slowDownCowboy(int delay, @Local EntityPlayer entity) {
        return EffectTracker.getOptional(entity)
                .flatMap(tracker -> tracker.getInstance(Effects.FROZEN))
                .map(FrozenEffect::getReduction)
                .map(f -> Math.round((1 + f) * delay))
                .orElse(delay);
    }
}
