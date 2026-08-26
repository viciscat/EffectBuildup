package io.github.viciscat.buildup.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

public class FrozenEffect extends StunningEffect {
    private static final UUID MODIFIER_UUID = UUID.fromString("caa167ec-9443-437a-a424-d886419b53fb");
    static {
        MinecraftForge.EVENT_BUS.register(new EventHandler(Effects.FROZEN));
    }

    public FrozenEffect() {
        super(new StackDurationStrategy.DecayingStackStrategy(8 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, DamageSource source) {
        super.proc(target, procDamage, source);
        applyModifier(target);
        if (getStacks() >= 20) {
            stackDurationStrategy.setStacks(8);
            stunTime = 4 * 20;
        }
    }

    private void applyModifier(EntityLivingBase target) {
        float reduction = Math.min(8, getStacks()) * 0.1f;
        applyModifier(
                target,
                SharedMonsterAttributes.MOVEMENT_SPEED,
                new AttributeModifier(MODIFIER_UUID, "Frozen Effect", -reduction, Constants.AttributeModifierOperation.MULTIPLY)
        );
    }

    @Override
    protected void onStackChange(EntityLivingBase target) {
        applyModifier(target);
    }

    @Override
    public void end(EntityLivingBase target) {
        super.end(target);
        target.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MODIFIER_UUID);
    }
}
