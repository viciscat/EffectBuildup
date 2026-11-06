package io.github.viciscat.buildup.mixin.mods;

import io.github.viciscat.buildup.BuildupGunUpgrades;
import io.github.viciscat.buildup.config.BuildupConfig;
import io.github.viciscat.buildup.config.RawConfig;
import io.github.viciscat.buildup.effect.BuildupEffect;
import io.github.viciscat.buildup.enchantment.BuildupEnchantments;
import io.github.viciscat.buildup.injected.ExtendedProjectile;
import io.github.viciscat.kustweaks.capability.UpgradableGun;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
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

    @Unique
    private boolean buildup$hasImmunityBreaker = false;

    @Inject(method = "initProjectile", at = @At("TAIL"))
    private void onInitProjectile(World worldIn, double posX, double posY, double posZ, float yaw, float pitch, float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage, EnumBulletFirePos firePos, CallbackInfo ci) {
        if (this.shooter == null) return;
        ItemStack mainhand = this.shooter.getHeldItemMainhand();
        UpgradableGun gun = UpgradableGun.get(mainhand);
        if (gun == null) {
            gun = UpgradableGun.getOrEmpty(this.shooter.getHeldItemOffhand());
        }
        for (BuildupGunUpgrades.Upgrade upgrade : BuildupGunUpgrades.getUpgrades()) {
            int stack = gun.getUpgradeStack(upgrade.gunUpgrade());
            if (stack > 0) {
                buildup$addToEffect(upgrade.effect(), stack * 4);
            }
        }
        buildup$hasImmunityBreaker = gun.getUpgradeStack(BuildupGunUpgrades.IMMUNITY_BREAKER) > 0;
        BuildupConfig.get().itemAttack().get(mainhand.getItem()).ifPresent(attackConfig -> {
            buildup$addToEffect(null, attackConfig.main());
            for (Reference2IntMap.Entry<BuildupEffect> entry : attackConfig.extras().reference2IntEntrySet()) {
                buildup$addToEffect(entry.getKey(), entry.getIntValue());
            }
        });
        int bonusBuildup = EnchantmentHelper.getEnchantmentLevel(BuildupEnchantments.BUILDUP_OFFENSE, mainhand) * RawConfig.attack.offenseEnchantmentBuildup;
        buildup$addToEffect(null, bonusBuildup);
    }

    @Unique
    private void buildup$addToEffect(BuildupEffect effect, int amount) {
        if (buildup$effects.isEmpty()) buildup$effects = new Object2IntOpenHashMap<>();
        buildup$effects.put(effect, amount + buildup$effects.getInt(effect));
    }

    /**
     * null key contains global buildup
     */
    @Override
    public Object2IntMap<BuildupEffect> buildup$getEffectStacks() {
        return buildup$effects;
    }

    @Override
    public boolean buildup$hasImmunityBreaker() {
        return buildup$hasImmunityBreaker;
    }
}
