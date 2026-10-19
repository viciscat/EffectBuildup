package io.github.viciscat.buildup;

import io.github.viciscat.buildup.config.BuildupConfig;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.effect.Effects;
import io.github.viciscat.buildup.enchantment.BuildupEnchantments;
import io.github.viciscat.buildup.mixin.mods.PartMaterialTypeAccessor;
import io.github.viciscat.buildup.tinkers.BuildupTinkerModifiers;
import io.github.viciscat.kustweaks.item.ItemGunUpgrade;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import yeelp.distinctdamagedescriptions.api.DDDAPI;

import java.util.List;
import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (ItemGunUpgrade upgrade : BuildupItems.gunUpgrades) {
            ResourceLocation location = new ResourceLocation(upgrade.gunUpgrade.id().getNamespace(), "upgrade/" + upgrade.gunUpgrade.id().getPath());
            ModelLoader.setCustomModelResourceLocation(upgrade, 0, new ModelResourceLocation(location, "inventory"));
        }
        for (ItemAffinityDust dust : BuildupItems.dusts) {
            ResourceLocation location = new ResourceLocation(dust.getRegistryName().getNamespace(), "dust/" + dust.effect.getId());
            ModelLoader.setCustomModelResourceLocation(dust, 0, new ModelResourceLocation(location, "inventory"));
        }
        ResourceLocation location = new ResourceLocation(BuildupItems.catalyst.getRegistryName().getNamespace(), "dust/" + BuildupItems.catalyst.getRegistryName().getPath());
        ModelLoader.setCustomModelResourceLocation(BuildupItems.catalyst, 0, new ModelResourceLocation(location, "inventory"));
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (EntityLiving.getSlotForItemStack(event.getItemStack()).getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            armorTooltip(event);
        } else {
            itemTooltip(event);
        }
    }

    private static void armorTooltip(ItemTooltipEvent event) {
        MutableInt global = new MutableInt();
        Object2IntMap<BuildupEffect> extras = new Object2IntArrayMap<>();
        BiConsumer<BuildupEffect, Integer> addEffect = (effect, f) -> extras.put(effect, f + extras.getInt(effect));
        //BiConsumer<BuildupEffect, Float> addEffectF = (effect, f) -> extras.put(effect, f + extras.getFloat(effect));
        ItemStack itemStack = event.getItemStack();
        BuildupConfig.get().armorDefense().get(itemStack.getItem()).ifPresent(defenseConfig -> {
            global.add(defenseConfig.global());
            defenseConfig.extras().forEach(addEffect);
        });
        global.add(EnchantmentHelper.getEnchantmentLevel(BuildupEnchantments.BUILDUP_DEFENSE, itemStack) * 100 + BuildupTinkerModifiers.getCurrent(itemStack, BuildupTinkerModifiers.BUILDUP_DEFENSE));
        Effects.forEach(effect -> {
            int i = BuildupTinkerModifiers.getCurrent(itemStack, BuildupTinkerModifiers.DEFENSE_AFFINITY.get(effect));
            if (i > 0) addEffect.accept(effect, i);
        });
        if (itemStack.getItem() instanceof TinkersItem tinkersItem) {
            List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(itemStack));
            List<PartMaterialType> components = tinkersItem.getRequiredComponents();
            if (materials.size() == components.size()) {
                for (int i = 0; i < materials.size(); i++) {
                    Material material = materials.get(i);
                    PartMaterialTypeAccessor type = (PartMaterialTypeAccessor) components.get(i);
                    for (String part : type.getNeededTypes()) {
                        MaterialAndPart materialAndPart = new MaterialAndPart(material, part);
                        BuildupConfig.get().materialPartDefense().get(materialAndPart).ifPresent(defenseConfig -> {
                            global.add(defenseConfig.global());
                            defenseConfig.extras().forEach(addEffect);
                        });
                    }
                }
            }
        }
        if (global.intValue() <= 0 && extras.isEmpty()) return;
        event.getToolTip().add("");
        if (global.intValue() > 0) {
            event.getToolTip().add("Buildup Defense: " + global.intValue());
        }
        if (!extras.isEmpty()) {
            event.getToolTip().add("Extra buildup:");
            for (Object2IntMap.Entry<BuildupEffect> entry : extras.object2IntEntrySet()) {
                event.getToolTip().add(" - " + I18n.translateToLocal("effect_buildup.ddd_effect_buildup." + entry.getKey().getId()) + ": " + entry.getIntValue());
            }
        }
        event.getToolTip().add("");
    }

    private static void itemTooltip(ItemTooltipEvent event) {
        MutableInt global = new MutableInt();
        Object2IntMap<BuildupEffect> extras = new Object2IntArrayMap<>();
        BiConsumer<BuildupEffect, Integer> addEffect = (effect, f) -> extras.put(effect, f + extras.getInt(effect));
        //BiConsumer<BuildupEffect, Float> addEffectF = (effect, f) -> extras.put(effect, f + extras.getFloat(effect));
        ItemStack itemStack = event.getItemStack();
        BuildupConfig.get().itemAttack().get(itemStack.getItem()).ifPresent(attackConfig -> {
            global.add(attackConfig.main());
            attackConfig.extras().forEach(addEffect);
        });
        global.add(EnchantmentHelper.getEnchantmentLevel(BuildupEnchantments.BUILDUP_OFFENSE, itemStack) * 100 + BuildupTinkerModifiers.getCurrent(itemStack, BuildupTinkerModifiers.BUILDUP_OFFENSE));
        Effects.forEach(effect -> {
            int i = BuildupTinkerModifiers.getCurrent(itemStack, BuildupTinkerModifiers.OFFENSE_AFFINITY.get(effect));
            if (i > 0) addEffect.accept(effect, i);
        });
        if (itemStack.getItem() instanceof TinkersItem tinkersItem) {
            List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(itemStack));
            List<PartMaterialType> components = tinkersItem.getRequiredComponents();
            if (materials.size() == components.size()) {
                for (int i = 0; i < materials.size(); i++) {
                    Material material = materials.get(i);
                    PartMaterialTypeAccessor type = (PartMaterialTypeAccessor) components.get(i);
                    for (String part : type.getNeededTypes()) {
                        MaterialAndPart materialAndPart = new MaterialAndPart(material, part);
                        BuildupConfig.get().materialPartAttack().get(materialAndPart).ifPresent(attackConfig -> {
                            global.add(attackConfig.main());
                            attackConfig.extras().forEach(addEffect);
                        });
                    }
                }
            }
        }
        if (global.intValue() <= 0 && extras.isEmpty()) return;
        event.getToolTip().add("");
        if (global.intValue() > 0) {
            event.getToolTip().add("Buildup: " + global.intValue());
        }
        if (!extras.isEmpty()) {
            event.getToolTip().add("Extra buildup:");
            for (Object2IntMap.Entry<BuildupEffect> entry : extras.object2IntEntrySet()) {
                event.getToolTip().add(" - " + I18n.translateToLocal("effect_buildup.ddd_effect_buildup." + entry.getKey().getId()) + ": " + entry.getIntValue());
            }
        }
        event.getToolTip().add("");
    }
}
