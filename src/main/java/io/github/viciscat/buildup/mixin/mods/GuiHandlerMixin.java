package io.github.viciscat.buildup.mixin.mods;

import arekkuusu.enderskills.api.configuration.DSLEvaluator;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.handler.GuiHandler;
import io.github.viciscat.buildup.effect.Effects;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

@Mixin(value = GuiHandler.class, remap = false)
public class GuiHandlerMixin {

    @Shadow
    public static void setValue(String name, BiFunction<Skill, Integer, Number> function) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public static void setSuffix(String name, String suffix) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "getClientGuiElement", at = @At(value = "INVOKE", target = "Larekkuusu/enderskills/common/handler/GuiHandler;setValue(Ljava/lang/String;Ljava/util/function/BiFunction;)V", ordinal = 0))
    private void addEffectSkills(int id, EntityPlayer player, World world, int x, int y, int z, CallbackInfoReturnable<Object> cir) {
        Effects.forEach(effect -> {
            String effectId = "BUILDUP:" + effect.getId();
            setValue(effectId, (skill, level) -> DSLEvaluator.evaluateFloat(skill, effectId, level, 1));
            setSuffix(effectId, "suffix_amount");
        });
    }
}
