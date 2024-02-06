package com.leobeliik.qork.base.proxy;

import com.leobeliik.qork.base.Qork;
import com.leobeliik.qork.base.handler.QorkSounds;
import net.minecraft.world.item.Items;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;
import org.violetmoon.zeta.module.ZetaCategory;
import org.violetmoon.zetaimplforge.module.ModFileScanDataModuleFinder;

import java.util.List;

import static com.leobeliik.qork.base.Qork.MODID;

public class CommonProxy {
    public void start() {
        Qork.QORK_ZETA.loadBus
                .subscribe(QorkSounds.class)
                .subscribe(this);

        Qork.QORK_ZETA.loadModules(List.of(
                        new ZetaCategory("mobs", Items.PIG_SPAWN_EGG),
                        new ZetaCategory("tweaks", Items.TNT)),
                new ModFileScanDataModuleFinder(MODID), QuarkGeneralConfig.INSTANCE); //TODO
    }
}
