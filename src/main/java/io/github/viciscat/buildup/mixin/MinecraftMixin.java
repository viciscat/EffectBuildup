package io.github.viciscat.buildup.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.StunningEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    public EntityPlayerSP player;

    @WrapMethod(method = {"clickMouse", "rightClickMouse", "middleClickMouse"})
    private void onLeftClick(Operation<Void> original) {
        EffectTracker tracker = EffectTracker.get(player);
        if (tracker == null) {
            original.call();
            return;
        }

        if (Stream.of(Effects.FROZEN, Effects.MEGABONK).map(tracker::getInstance).flatMap(Optional::stream).anyMatch(StunningEffect::shouldBeStunned)) return;
        original.call();
    }
}
