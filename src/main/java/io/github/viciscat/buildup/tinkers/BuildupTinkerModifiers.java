package io.github.viciscat.buildup.tinkers;

import c4.conarm.lib.modifiers.ArmorModifier;
import io.github.viciscat.buildup.BuildupItems;
import io.github.viciscat.buildup.BuildupMod;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

import javax.annotation.Nullable;
import java.util.Map;

@Mod.EventBusSubscriber
public final class BuildupTinkerModifiers {
    public static final ArmorModifier BUILDUP_DEFENSE = new BuildupDefenseArmorModifier();
    public static final ToolModifier BUILDUP_OFFENSE = new BuildupOffenseToolModifier();
    public static final Map<BuildupEffect, BuildupOffenseAffinityModifier> OFFENSE_AFFINITY = new Reference2ObjectOpenHashMap<>();
    public static final Map<BuildupEffect, BuildupDefenseAffinityModifier> DEFENSE_AFFINITY = new Reference2ObjectOpenHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerModifiers(RegistryEvent.Register<Item> event) {
        if (BuildupMod.debug()) {
            BuildupMod.LOGGER.info("Effect to dust: {}", BuildupItems.effectToDust);
        }
        // the modifiers get registered in their constructors
        Effects.forEach(effect -> {
            OFFENSE_AFFINITY.put(effect, new BuildupOffenseAffinityModifier(effect));
            DEFENSE_AFFINITY.put(effect, new BuildupDefenseAffinityModifier(effect));
        });
        if (BuildupMod.debug()) {
            BuildupMod.LOGGER.info("Registered offense modifiers: {}", OFFENSE_AFFINITY);
        }
    }

    public static @Nullable ModifierNBT.IntegerNBT getModifierData(ItemStack stack, IModifier modifier) {
        NBTTagCompound safe = TagUtil.getTagSafe(stack);
        if (!TinkerUtil.hasModifier(safe, modifier.getIdentifier())) return null;
        return ModifierNBT.readInteger(TinkerUtil.getModifierTag(safe, modifier.getIdentifier()));
    }

    public static int getLevel(ItemStack stack, IModifier modifier) {
        ModifierNBT.IntegerNBT data = getModifierData(stack, modifier);
        return data == null ? 0 : data.level;
    }
    public static int getCurrent(ItemStack stack, IModifier modifier) {
        ModifierNBT.IntegerNBT data = getModifierData(stack, modifier);
        return data == null ? 0 : data.current;
    }
}
