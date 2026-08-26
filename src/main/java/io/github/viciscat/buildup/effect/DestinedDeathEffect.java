package io.github.viciscat.buildup.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.UUID;

public class DestinedDeathEffect extends EffectInstance {
    private float combinedProcDamage;
    private static final UUID MODIFIER_UUID = UUID.fromString("ceae796f-6ab9-4265-8787-bba1e7ac498e");
    protected DestinedDeathEffect() {
        super(new StackDurationStrategy.RefreshStrategy(2 * 60 * 60 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, @Nullable DamageSource source) {
        super.proc(target, procDamage, source);
        combinedProcDamage += this.procDamage;
        applyModifier(target);
    }

    private void applyModifier(EntityLivingBase target) {
        applyModifier(
                target,
                SharedMonsterAttributes.MAX_HEALTH,
                new AttributeModifier(MODIFIER_UUID, "Destined Death Effect", -combinedProcDamage * 0.005, Constants.AttributeModifierOperation.ADD)
        );
    }

    @Override
    protected void onStackChange(EntityLivingBase target) {
        super.onStackChange(target);
        applyModifier(target);
    }

    @Override
    public void end(EntityLivingBase target) {
        super.end(target);
        target.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(MODIFIER_UUID);
    }
}
