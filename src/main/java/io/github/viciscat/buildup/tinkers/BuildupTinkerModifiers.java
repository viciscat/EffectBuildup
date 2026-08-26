package io.github.viciscat.buildup.tinkers;

import c4.conarm.lib.modifiers.ArmorModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public final class BuildupTinkerModifiers {
    public static final ArmorModifier BUILDUP_DEFENSE = new BuildupDefenseArmorModifier();
    public static final ToolModifier BUILDUP_OFFENSE = new BuildupOffenseToolModifier();

    @SubscribeEvent
    public static void registerModifiers(RegistryEvent.Register<Item> event) {
        // the modifiers get registered in their constructors
    }

    public static @Nullable ModifierNBT.IntegerNBT getModifierData(ItemStack stack, IModifier modifier) {
        NBTTagCompound safe = TagUtil.getTagSafe(stack);
        if (!TinkerUtil.hasModifier(safe, modifier.getIdentifier())) return null;
        return ModifierNBT.readInteger(TinkerUtil.getModifierTag(safe, modifier.getIdentifier()));
    }

    public static int getLevel(ItemStack stack, IModifier modifier) {
        ModifierNBT.IntegerNBT data = getModifierData(stack, modifier);
        return data == null ? 0 : data.current / data.max;
    }
}
