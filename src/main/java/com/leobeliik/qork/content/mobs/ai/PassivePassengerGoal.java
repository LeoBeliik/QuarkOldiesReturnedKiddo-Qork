package com.leobeliik.qork.content.mobs.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PassivePassengerGoal extends Goal {
    private final Mob entity;

    public PassivePassengerGoal(Mob entity) {
        this.entity = entity;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return entity.isPassenger();
    }
}
