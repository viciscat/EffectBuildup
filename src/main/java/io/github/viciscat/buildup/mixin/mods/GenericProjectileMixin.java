package io.github.viciscat.buildup.mixin.mods;

import io.github.viciscat.buildup.BuildupGunUpgrades;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.injected.ExtendedProjectile;
import io.github.viciscat.kustweaks.capability.UpgradableGun;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;

@Mixin(value = GenericProjectile.class, remap = false)
public class GenericProjectileMixin implements ExtendedProjectile {

    @Shadow
    protected EntityLivingBase shooter;

    @Unique
    private Object2IntMap<BuildupEffect> buildup$effects = Object2IntMaps.emptyMap();

    @Inject(method = "initProjectile", at = @At("TAIL"))
    private void onInitProjectile(World worldIn, double posX, double posY, double posZ, float yaw, float pitch, float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage, EnumBulletFirePos firePos, CallbackInfo ci) {
        UpgradableGun gun = UpgradableGun.get(this.shooter.getHeldItemMainhand());
        if (gun == null) {
            gun = UpgradableGun.getOrEmpty(this.shooter.getHeldItemOffhand());
        }
        for (BuildupGunUpgrades.Upgrade upgrade : BuildupGunUpgrades.getUpgrades()) {
            int stack = gun.getUpgradeStack(upgrade.gunUpgrade());
            if (stack > 0) {
                if (buildup$effects.isEmpty()) buildup$effects = new Object2IntOpenHashMap<>();
                buildup$effects.put(upgrade.effect(), stack);
            }
        }
    }

    @Override
    public Object2IntMap<BuildupEffect> buildup$getEffectStacks() {
        return null;
    }
}
