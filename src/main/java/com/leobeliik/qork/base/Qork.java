package com.leobeliik.qork.base;

import com.leobeliik.qork.base.proxy.ClientProxy;
import com.leobeliik.qork.base.proxy.CommonProxy;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.multiloader.Env;
import org.violetmoon.zetaimplforge.ForgeZeta;

@Mod(Qork.MODID)
public class Qork {
    public static final String MODID = "qork";

    public static Qork instance;
    public static CommonProxy proxy;

    public static final Zeta QORK_ZETA = new ForgeZeta(MODID, LogManager.getLogger("qork-zeta"));

    public Qork() {
        instance = this;
        QORK_ZETA.start();
        proxy = Env.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        proxy.start();

    }

    public static ResourceLocation asResource(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static <T> ResourceKey<T> asResourceKey(ResourceKey<? extends Registry<T>> base, String name) {
        return ResourceKey.create(base, asResource(name));
    }
}
