package io.github.viciscat.buildup.effect;

import io.github.viciscat.buildup.BuildupSounds;
import io.github.viciscat.buildup.particle.BasicParticle;
import io.github.viciscat.buildup.particle.BuildupParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChainLightningEffect extends EffectInstance {
    public static final TextureAtlasSprite PARTICLE = BuildupParticleManager.createSprite("chain_lightning", 152, 64, 12, 12);
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
        if (!world.isRemote && ticks % 20 == 0) {
            final int radius = 10 + (getStacks() - 1) * 2;
            final int targetCount = 3 + getStacks();
            final float multiplier = 0.5f + (getStacks() - 1) * 0.04f;
            Entity source = sourceUUID != null ? ((WorldServer)world).getEntityFromUuid(sourceUUID) : null;
            List<Entity> entities = world.getEntitiesInAABBexcluding(source,
                    new AxisAlignedBB(target.getPositionVector(), target.getPositionVector()).grow(radius),
                    entity -> entity instanceof EntityLivingBase && entity.getDistanceSq(target) < radius * radius);
            entities.stream()
                    .sorted(Comparator.comparingDouble(entity -> entity.getDistanceSq(target)))
                    .limit(targetCount)
                    .forEach(entity -> entity.attackEntityFrom(new DamageSource("fx_lightning"), procDamage * multiplier));
        }
        ticks++;
    }

    @Override
    public void clientTick(EntityLivingBase target) {
        super.clientTick(target);
        if (target.ticksExisted % 8 == 0) {
            List<AxisAlignedBB> boxes = target.getEntityWorld().getCollisionBoxes(null, target.getEntityBoundingBox().grow(1));
            makeParticle(target, boxes);
        }
    }

    @Override
    public void clientProc(EntityLivingBase target) {
        super.clientProc(target);
        BuildupSounds.playAt(target, BuildupSounds.CHAIN_LIGHTNING_PROC, 1, 1);
        List<AxisAlignedBB> boxes = target.getEntityWorld().getCollisionBoxes(null, target.getEntityBoundingBox().grow(1));
        for (int i = 0; i < 5; i++) {
            makeParticle(target, boxes);
        }
    }

    private static void makeParticle(EntityLivingBase target, List<AxisAlignedBB> boxes) {
        AxisAlignedBB bb = boxes.get(BuildupParticleManager.RANDOM.nextInt(boxes.size()));
        Vec3d point = new Vec3d(
                bb.minX + (bb.maxX - bb.minX) * BuildupParticleManager.RANDOM.nextDouble(),
                bb.minY + (bb.maxY - bb.minY) * BuildupParticleManager.RANDOM.nextDouble(),
                bb.minZ + (bb.maxZ - bb.minZ) * BuildupParticleManager.RANDOM.nextDouble()
        );
        Vec3d entityCenter = target.getPositionVector().add(0, target.height / 2, 0);
        RayTraceResult result = target.getEntityWorld().rayTraceBlocks(entityCenter, point, false, true, false);
        if (result != null) {
            Vec3d normalize = result.hitVec.subtract(entityCenter).normalize();
            double distance = entityCenter.distanceTo(result.hitVec);
            for (double i = 0; i <= distance; i+=0.2) {
                Vec3d particlePos = entityCenter.add(normalize.scale(i));
                BasicParticle particle = new BasicParticle(
                        target.getEntityWorld(),
                        particlePos
                );
                particle.setMaxAge(2);
                particle.setParticleTexture(PARTICLE);
                BuildupParticleManager.INSTANCE.addParticle(particle);
            }
        }
    }
}
