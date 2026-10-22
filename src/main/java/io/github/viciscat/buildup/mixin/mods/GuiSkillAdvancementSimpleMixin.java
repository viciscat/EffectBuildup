package io.github.viciscat.buildup.mixin.mods;

import arekkuusu.enderskills.client.gui.widgets.GuiSkillAdvancementSimple;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GuiSkillAdvancementSimple.class, remap = false)
public class GuiSkillAdvancementSimpleMixin {

    @ModifyExpressionValue(method = "fillStatsWithLevel", at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;", ordinal = 1))
    private String modifyString(String s) {
        return s.replace(':', '_');
    }
}
