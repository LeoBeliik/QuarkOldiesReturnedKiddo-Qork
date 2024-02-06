package com.leobeliik.qork.mixins;

import com.leobeliik.qork.content.mobs.entity.Frogge;
import com.leobeliik.qork.content.mobs.module.FrogsModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.addons.oddities.item.TinyPotatoBlockItem;

@Mixin(Frog.class)
public abstract class FrogMixin extends Animal {
    protected FrogMixin(EntityType<? extends Animal> animal, Level level) {
        super(animal, level);
    }

    @Inject(method = "Lnet/minecraft/world/entity/animal/frog/Frog;isFood(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("RETURN"), remap = false)
    public boolean qorkIsFood(ItemStack item, CallbackInfoReturnable cir) {
        if (item.getItem() instanceof TinyPotatoBlockItem) {
            BlockPos pos = getOnPos();
            level().explode(this, pos.getX(), pos.getY(), pos.getZ(), 5, Level.ExplosionInteraction.MOB);
            this.setHealth(0);
            if (level() instanceof ServerLevel level) {
                FrogsModule.frogType.spawn(level, item, null, pos, MobSpawnType.MOB_SUMMONED, true, true);
            }
            return true;
        }
        return cir.getReturnValueZ();
    }
}
