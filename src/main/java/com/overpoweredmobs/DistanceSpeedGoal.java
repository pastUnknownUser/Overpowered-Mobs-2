package com.overpoweredmobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DistanceSpeedGoal extends Goal {

    private static final double FAR_THRESHOLD = 20.0;
    private static final double MID_THRESHOLD = 10.0;
    private static final double NEAR_THRESHOLD = 5.0;

    private static final double FAR_SPEED = 3.0;
    private static final double MID_SPEED = 2.0;
    private static final double NEAR_SPEED = 1.5;
    private static final double CLOSE_SPEED = 1.0;

    private static final int FAR_RECALC = 40;
    private static final int MID_RECALC = 20;
    private static final int NEAR_RECALC = 10;
    private static final int CLOSE_RECALC = 5;

    private static final float FAR_TURN = 4.0f;
    private static final float MID_TURN = 8.0f;
    private static final float NEAR_TURN = 12.0f;
    private static final float CLOSE_TURN = 15.0f;

    private static final double SMOOTHING = 0.15;
    private static final double STUCK_THRESHOLD_SQ = 0.01;
    private static final int STUCK_TICK_LIMIT = 20;

    private final Mob mob;
    private Vec3 lastPos;
    private int stuckTicks;
    private int recalcCounter;
    private double currentSpeed = 1.0;

    public DistanceSpeedGoal(Mob mob) {
        this.mob = mob;
        this.lastPos = mob.position();
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Player nearest = level.getNearestPlayer(mob, FAR_THRESHOLD + 5.0);
        return nearest != null && mob.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Player nearest = level.getNearestPlayer(mob, FAR_THRESHOLD + 10.0);
        return nearest != null && mob.isAlive();
    }

    @Override
    public void tick() {
        if (!(mob.level() instanceof ServerLevel level)) return;

        Player player = level.getNearestPlayer(mob, FAR_THRESHOLD + 10.0);
        if (player == null) return;

        double dist = mob.distanceTo(player);

        double targetSpeed;
        int recalcInterval;
        float turnRate;

        if (dist >= FAR_THRESHOLD) {
            targetSpeed = FAR_SPEED;
            recalcInterval = FAR_RECALC;
            turnRate = FAR_TURN;
        } else if (dist >= MID_THRESHOLD) {
            targetSpeed = MID_SPEED;
            recalcInterval = MID_RECALC;
            turnRate = MID_TURN;
        } else if (dist >= NEAR_THRESHOLD) {
            targetSpeed = NEAR_SPEED;
            recalcInterval = NEAR_RECALC;
            turnRate = NEAR_TURN;
        } else {
            targetSpeed = CLOSE_SPEED;
            recalcInterval = CLOSE_RECALC;
            turnRate = CLOSE_TURN;
        }

        currentSpeed += (targetSpeed - currentSpeed) * SMOOTHING;

        mob.getNavigation().setSpeedModifier(currentSpeed);

        recalcCounter++;
        boolean stuck = checkStuck();
        if (recalcCounter >= recalcInterval || stuck) {
            recalcCounter = 0;
            mob.getNavigation().moveTo(player, currentSpeed);
        }

        capYaw(player, turnRate);
    }

    private boolean checkStuck() {
        Vec3 currentPos = mob.position();
        if (currentPos.distanceToSqr(lastPos) < STUCK_THRESHOLD_SQ) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
        lastPos = currentPos;
        return stuckTicks >= STUCK_TICK_LIMIT;
    }

    private void capYaw(Player player, float maxTurn) {
        double dx = player.getX() - mob.getX();
        double dz = player.getZ() - mob.getZ();
        float targetYaw = (float) Mth.atan2(dz, dx) * Mth.RAD_TO_DEG - 90.0f;

        float currentYaw = mob.getYRot();
        float diff = Mth.wrapDegrees(targetYaw - currentYaw);
        diff = Mth.clamp(diff, -maxTurn, maxTurn);

        mob.setYRot(currentYaw + diff);
        mob.setYHeadRot(mob.getYRot());
    }
}
