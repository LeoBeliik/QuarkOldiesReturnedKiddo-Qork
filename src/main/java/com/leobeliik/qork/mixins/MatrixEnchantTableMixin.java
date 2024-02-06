package com.leobeliik.qork.mixins;

import com.leobeliik.qork.base.Qork;
import com.leobeliik.qork.content.tweaks.module.EnchantingTableRangeModule;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.violetmoon.quark.addons.oddities.block.be.AbstractEnchantingTableBlockEntity;
import org.violetmoon.quark.addons.oddities.block.be.MatrixEnchantingTableBlockEntity;
import static com.leobeliik.qork.content.tweaks.module.EnchantingTableRangeModule.getBookshelfRange;
import static com.leobeliik.qork.content.tweaks.module.EnchantingTableRangeModule.isUninterrupted;

@Mixin(MatrixEnchantingTableBlockEntity.class)
public abstract class MatrixEnchantTableMixin extends AbstractEnchantingTableBlockEntity {

    @Shadow
    public int bookshelfPower;

    @Shadow
    protected abstract float getEnchantPowerAt(Level world, BlockPos pos);

    public MatrixEnchantTableMixin(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @ModifyConstant(method = "Lorg/violetmoon/quark/addons/oddities/block/be/MatrixEnchantingTableBlockEntity;updateEnchantPower()V",
            constant = @Constant(floatValue = 0.0F), remap = false)
    public float updatePower(float power) {
        return getExtendedPower();
    }

    @Unique
    private float getExtendedPower() {
        float power = 0.0F;
        if (!Qork.QORK_ZETA.modules.isEnabled(EnchantingTableRangeModule.class)) return power;

        for (BlockPos pos : getBookshelfRange()) {
            if (level.getBlockState(this.worldPosition.offset(pos)).is(BlockTags.ENCHANTMENT_POWER_PROVIDER) && isUninterrupted(level, this.worldPosition, this.worldPosition.offset(pos))) {
                if ((pos.getX() < -2 || pos.getX() > 2) || (pos.getZ() < -2 || pos.getZ() > 2)) {
                    power += level.getBlockState(this.worldPosition.offset(pos)).getEnchantPowerBonus(level, this.worldPosition.offset(pos));
                }
            }
        }
        return power;
    }


}