package io.github.viciscat.buildup.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChainLightningEffect extends EffectInstance {
    private @Nullable UUID sourceUUID;
    private int ticks;
    protected ChainLightningEffect() {
        super(new StackDurationStrategy.RefreshStrategy(3 * 20));
    }

    @Override
    public void proc(EntityLivingBase target, int procDamage, @Nullable DamageSource source) {
        super.proc(target, procDamage, source);
        sourceUUID = Optional.ofNullable(source).map(DamageSource::getTrueSource).map(Entity::getUniqueID).orElse(null);
    }

    @Override
    public void tick(EntityLivingBase target) {
        super.tick(target);
        World world = target.getEntityWorld();
        if (! world.isRemote && ticks % 20 == 0) {
            Entity source = sourceUUID != null ? ((WorldServer)world).getEntityFromUuid(sourceUUID) : null;
            List<Entity> entities = world.getEntitiesInAABBexcluding(source,
                    new AxisAlignedBB(target.getPositionVector(), target.getPositionVector()).grow(10),
                    entity -> entity instanceof EntityLivingBase && entity.getDistanceSq(target) < 100);
            entities.stream()
                    .sorted(Comparator.comparingDouble(entity -> entity.getDistanceSq(target)))
                    .limit(7)
                    .forEach(entity -> entity.attackEntityFrom(new DamageSource("fx_lightning"), procDamage * 0.4f));
        }
        ticks++;
    }
}
