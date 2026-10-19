package io.github.viciscat.buildup.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.viciscat.buildup.capabilities.EffectTracker;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.effect.StunningEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapMethod(method = "turn")
    private void onTurn(float yaw, float pitch, Operation<Void> original) {
        if ((Object)this != Minecraft.getMinecraft().player) {
            original.call(yaw, pitch);
            return;
        }
        EffectTracker tracker = EffectTracker.get(Minecraft.getMinecraft().player);
        if (tracker == null) {
            original.call(yaw, pitch);
            return;
        }

        if (Stream.of(Effects.FROZEN, Effects.MEGABONK).map(tracker::getInstance).flatMap(Optional::stream).anyMatch(StunningEffect::shouldBeStunned)) return;
        original.call(yaw, pitch);
    }
}
