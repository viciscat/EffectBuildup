package io.github.viciscat.buildup.mixin.mods;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.skill.effect.Stunned;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.viciscat.buildup.capabilities.StunTracker;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(value = Stunned.class, remap = false)
public class StunnedMixin {

    @WrapMethod(method = "set(Lnet/minecraft/entity/EntityLivingBase;Larekkuusu/enderskills/api/capability/data/SkillData;I)V")
    private void setStunTime(EntityLivingBase entity, SkillData data, int time, Operation<Void> original) {
        original.call(
                entity,
                data,
                Optional.ofNullable(entity)
                        .flatMap(StunTracker::getOptional)
                        .map(tracker -> Math.round(tracker.tryStun(time) * time))
                        .orElse(time)
        );
    }
}
