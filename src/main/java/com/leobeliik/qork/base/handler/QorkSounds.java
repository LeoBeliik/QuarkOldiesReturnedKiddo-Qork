package com.leobeliik.qork.base.handler;

import com.google.common.collect.Lists;
import com.leobeliik.qork.base.Qork;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;

import java.util.List;

import static com.leobeliik.qork.base.Qork.MODID;

public class QorkSounds {
    private static final List<SoundEvent> REGISTRY_DEFERENCE = Lists.newArrayList();

    public static final SoundEvent ENTITY_FROG_WEDNESDAY = register("entity.frog.wednesday");
    public static final SoundEvent ENTITY_FROG_JUMP = register("entity.frog.jump");
    public static final SoundEvent ENTITY_FROG_DIE = register("entity.frog.die");
    public static final SoundEvent ENTITY_FROG_HURT = register("entity.frog.hurt");
    public static final SoundEvent ENTITY_FROG_IDLE = register("entity.frog.idle");
    public static final SoundEvent ENTITY_FROG_SHEAR = register("entity.frog.shear");

    @LoadEvent
    public static void start(ZRegister e) {
        for(SoundEvent event : REGISTRY_DEFERENCE)
            Qork.QORK_ZETA.registry.register(event, event.getLocation(), Registries.SOUND_EVENT);
        REGISTRY_DEFERENCE.clear();
    }

    public static SoundEvent register(String name) {
        SoundEvent event = SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, name));
        REGISTRY_DEFERENCE.add(event);
        return event;
    }
}
