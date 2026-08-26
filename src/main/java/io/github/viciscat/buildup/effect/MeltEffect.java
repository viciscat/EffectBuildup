package io.github.viciscat.buildup.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class MeltEffect extends EffectInstance {
    private int ticks = 0;
    private static final UUID MODIFIER_UUID = UUID.fromString("33fad0d8-3802-4d5a-81eb-9a2383cd8ba3");
    private static final String[] ATTRIBUTES_TO_REDUCE  = new String[]{
            "generic.armor",
            "generic.armorToughness",
            "potioncore.damageResistance",
            "potioncore.magicShielding",
    };

    public MeltEffect() {
        super(new StackDurationStrategy.RefreshStrategy(120 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        applyModifiers(target);
    }

    @Override
    protected void onStackChange(EntityLivingBase target) {
        super.onStackChange(target);
        applyModifiers(target);
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        ticks++;
        if (ticks % 20 == 0) {
            int clamp = Math.clamp(getStacks() - 10, 0, 5);
            if (clamp != 0) target.attackEntityFrom(new DamageSource("fx_acid"), procDamage * (clamp * 0.05f));
        }
    }

    public float getResistanceMultiplier() {
        return 1 - Math.min(getStacks(), 10) * 0.05f;
    }

    private void applyModifiers(EntityLivingBase target) {
        AttributeModifier modifier = new AttributeModifier(MODIFIER_UUID, "Melt Effect", 1 - getResistanceMultiplier(), Constants.AttributeModifierOperation.MULTIPLY);
        Arrays.stream(ATTRIBUTES_TO_REDUCE)
                .map(attr -> target.getAttributeMap().getAttributeInstanceByName(attr))
                .filter(Objects::nonNull)
                .forEach(instance -> applyModifier(instance, modifier));
    }
}
