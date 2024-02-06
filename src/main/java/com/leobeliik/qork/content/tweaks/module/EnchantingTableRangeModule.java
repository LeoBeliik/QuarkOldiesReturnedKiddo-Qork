package com.leobeliik.qork.content.tweaks.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.violetmoon.quark.addons.oddities.block.MatrixEnchantingTableBlock;
import org.violetmoon.quark.addons.oddities.block.be.MatrixEnchantingTableBlockEntity;
import org.violetmoon.quark.addons.oddities.module.MatrixEnchantingModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.List;

import static net.minecraft.world.level.BlockGetter.traverseBlocks;

@ZetaLoadModule(category = "qork-tweaks")
public class EnchantingTableRangeModule extends ZetaModule {
//TODO make MatrixEnchantingTableBlock#animateTick do particles on the new range
    @Config(description = "Range of bookshelves check of the enchanting table\n" +
            "Keep in mind the larger the numbers the laggies can get\n" +
            "Numbers represent X, Y, Z")
    public static List<Integer> enchantingTableRange = List.of(2, 1, 2);

    private static List<BlockPos> BOOKSHELF_OFFSETS;
    public static boolean rangeChanged = false;

    @LoadEvent
    private void configReload(ZConfigChanged z) {
        if (!Quark.ZETA.modules.isEnabled(MatrixEnchantingModule.class))
            rangeChanged = false;
    }


    public static boolean isUninterrupted(Level level, BlockPos tablePos, BlockPos bookshelvePos) {
        return isBlockLined(new ClipBlockStateContext(tablePos.getCenter(), bookshelvePos.getCenter(), state -> {
            if (!Quark.ZETA.modules.isEnabled(MatrixEnchantingModule.class)) {
                return state.is(Blocks.AIR);
            } else {
                if (MatrixEnchantingModule.allowUnderwaterEnchanting && state.is(Blocks.WATER)) {
                    return true;
                }
            }
            return !state.is(Blocks.AIR) && !state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER) && !(state.is(Blocks.ENCHANTING_TABLE) || state.getBlock() instanceof MatrixEnchantingTableBlock);
        }), level);
    }

    //Slighlty modification of BlockGetter#isBlockInLine
    private static boolean isBlockLined(ClipBlockStateContext pContext, Level level) {
        return traverseBlocks(pContext.getFrom(), pContext.getTo(), pContext, (blockStateContext, blockPos) -> {
            BlockState blockstate = level.getBlockState(blockPos);
            Vec3 vec3 = blockStateContext.getFrom().subtract(blockStateContext.getTo());
            if (MatrixEnchantingModule.allowShortBlockEnchanting && MatrixEnchantingTableBlockEntity.isShortBlock(level, blockPos)) {
                return true;
            }

            Object o = blockStateContext.isTargetBlock().test(blockstate) ? new BlockHitResult(blockStateContext.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(blockStateContext.getTo()), false) : null;
            return o != null ? false : null;
        }, (stateContext) -> true);
    }

    public static void setBookshelveRange() {
        int xr = enchantingTableRange.get(0);
        int yr = enchantingTableRange.get(1);
        int zr = enchantingTableRange.get(2);

        BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-xr, -yr + 1, -zr, xr, yr, zr)
                .filter(pos -> Math.abs(pos.getX()) <= xr || pos.getY() <= yr || Math.abs(pos.getZ()) <= zr)
                .map(BlockPos::immutable)
                .toList();
        rangeChanged = true;
    }

    public static List<BlockPos> getBookshelfRange() {
        if (BOOKSHELF_OFFSETS == null) setBookshelveRange();
        return BOOKSHELF_OFFSETS;
    }
}
