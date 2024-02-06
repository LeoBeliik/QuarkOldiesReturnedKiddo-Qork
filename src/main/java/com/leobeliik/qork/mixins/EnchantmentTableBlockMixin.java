package com.leobeliik.qork.mixins;


import com.leobeliik.qork.base.Qork;
import com.leobeliik.qork.content.tweaks.module.EnchantingTableRangeModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

import static com.leobeliik.qork.content.tweaks.module.EnchantingTableRangeModule.*;

@Mixin(EnchantmentTableBlock.class)
public class EnchantmentTableBlockMixin {

    @Shadow
    @Final
    @Mutable
    public static List<BlockPos> BOOKSHELF_OFFSETS;

    @Inject(method = "Lnet/minecraft/world/level/block/EnchantmentTableBlock;newBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
            at = @At("RETURN"), remap = false)
    public BlockEntity qork_newBlockEntity(BlockPos pPos, BlockState pState, CallbackInfoReturnable cir) {
        changeBookshelveRange();
        return (BlockEntity) cir.getReturnValue();
    }

    @Inject(method = "Lnet/minecraft/world/level/block/EnchantmentTableBlock;use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At("RETURN"), remap = false)
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable cir) {
        if (!rangeChanged) {
            changeBookshelveRange();
        }
        return (InteractionResult) cir.getReturnValue();
    }

    @Unique
    private void changeBookshelveRange() {
        if (!Qork.QORK_ZETA.modules.isEnabled(EnchantingTableRangeModule.class)) return;
        setBookshelveRange();
        BOOKSHELF_OFFSETS = getBookshelfRange();
    }
}
