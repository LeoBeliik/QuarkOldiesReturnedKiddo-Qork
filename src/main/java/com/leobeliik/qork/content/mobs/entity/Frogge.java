package com.leobeliik.qork.content.mobs.entity;

import com.google.common.collect.Lists;
import com.leobeliik.qork.base.handler.QorkSounds;
import com.leobeliik.qork.content.mobs.ai.PassivePassengerGoal;
import com.leobeliik.qork.content.mobs.module.FrogsModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.violetmoon.quark.base.proxy.CommonProxy;
import org.violetmoon.quark.content.mobs.ai.FavorBlockGoal;
import org.violetmoon.zeta.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Frogge extends Animal implements IEntityAdditionalSpawnData, IForgeShearable {
    public static final ResourceLocation FROG_LOOT_TABLE = new ResourceLocation("qork", "entities/frog");

    private static final EntityDataAccessor<Integer> TALK_TIME = SynchedEntityData.defineId(Frogge.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE_MODIFIER = SynchedEntityData.defineId(Frogge.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_SWEATER = SynchedEntityData.defineId(Frogge.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> VOID = SynchedEntityData.defineId(Frogge.class, EntityDataSerializers.BOOLEAN);

    private static final UUID VOID_MODIFIER_UUID = UUID.fromString("212dbecc-7525-4137-a74b-361cc128d24f");

    public int spawnCd = -1;
    public int spawnChain = 30;

    public boolean isDuplicate = false;
    private boolean sweatered = false;

    private Ingredient[] temptationItems;

    public Frogge(EntityType<? extends Frogge> type, Level worldIn) {
        this(type, worldIn, 1);
    }

    public Frogge(EntityType<? extends Frogge> type, Level worldIn, float sizeModifier) {
        super(type, worldIn);
        if (sizeModifier != 1)
            entityData.set(SIZE_MODIFIER, sizeModifier);

        this.jumpControl = new FrogJumpController();
        this.moveControl = new FrogMoveController();
        this.setMovementSpeed(0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        entityData.define(TALK_TIME, 0);
        entityData.define(SIZE_MODIFIER, 1f);
        entityData.define(HAS_SWEATER, false);
        entityData.define(VOID, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new PassivePassengerGoal(this));
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new FrogPanicGoal(1.25));
        goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        goalSelector.addGoal(4, new TemptGoal(this, 1.2, getTemptationItems(false), false));
        goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        goalSelector.addGoal(6, new FavorBlockGoal(this, 1, Blocks.LILY_PAD));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1, 0.5F));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(ForgeMod.ENTITY_GRAVITY.get());
    }

    @Nonnull
    @Override
    public MoveControl getMoveControl() {
        return moveControl;
    }

    @Nonnull
    @Override
    public JumpControl getJumpControl() {
        return jumpControl;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, @Nonnull DamageSource source) {
        return false;
    }

    @Override
    protected float getStandingEyeHeight(@Nonnull Pose pose, EntityDimensions size) {
        return 0.2f * size.height;
    }
//
//	@Override
//	public boolean isEntityInsideOpaqueBlock() {
//		return MiscUtil.isEntityInsideOpaqueBlock(this);
//	}

    public int getTalkTime() {
        return entityData.get(TALK_TIME);
    }

    public float getSizeModifier() {
        return entityData.get(SIZE_MODIFIER);
    }

    public static boolean canBeSweatered() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }


    @Override
    public void tick() {
        if (!level().isClientSide && !sweatered) {
            setSweater(CommonProxy.jingleTheBells && (getUUID().getLeastSignificantBits() % 10 == 0));
            sweatered = true;
        }

        if (this.jumpTicks != this.jumpDuration) ++this.jumpTicks;
        else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }

        if (!isVoid() && hasCustomName() && getY() <= level().getMinBuildHeight()) {
            Component name = getCustomName();
            if (name != null && name.getContents().equals("Jack")) {
                setVoid(true);
            }
        }

        super.tick();

        int talkTime = getTalkTime();
        if (talkTime > 0)
            entityData.set(TALK_TIME, talkTime - 1);

        if (FrogsModule.enableBigFunny && spawnCd > 0 && spawnChain > 0) {
            spawnCd--;
            if (spawnCd == 0 && !level().isClientSide) {
                float multiplier = 0.8F;
                Frogge newFrog = new Frogge(FrogsModule.frogType, level());
                Vec3 pos = position();
                newFrog.setPos(pos.x, pos.y, pos.z);
                newFrog.setDeltaMovement((Math.random() - 0.5) * multiplier, (Math.random() - 0.5) * multiplier, (Math.random() - 0.5) * multiplier);
                newFrog.isDuplicate = true;
                newFrog.spawnCd = 2;
                newFrog.spawnChain = spawnChain - 1;
                level().addFreshEntity(newFrog);
                spawnChain = 0;
            }
        }

        this.yRotO = this.yHeadRotO;
        this.setYRot(this.yHeadRot);
    }

    @Override
    public boolean shouldDropExperience() {
        return !isDuplicate && super.shouldDropExperience();
    }

    @Nonnull
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return FROG_LOOT_TABLE;
    }

    private int droppedLegs = -1;

    @Override
    protected void dropFromLootTable(@Nonnull DamageSource source, boolean damagedByPlayer) {
        droppedLegs = 0;
        super.dropFromLootTable(source, damagedByPlayer);
        droppedLegs = -1;
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(@Nonnull ItemStack stack, float offsetY) {
        if (droppedLegs >= 0 && (stack.is(FrogsModule.frogLeg) || stack.is(FrogsModule.frogLeg))) {
            int count = Math.max(4 - droppedLegs, 0);
            droppedLegs += stack.getCount();

            if (stack.getCount() > count) {
                ItemStack copy = stack.copy();
                copy.shrink(count);
                ItemNBTHelper.setBoolean(copy, "sus", true);

                stack = stack.copy();
                stack.shrink(copy.getCount());

                ItemEntity spawned = super.spawnAtLocation(copy, offsetY);
                if (stack.isEmpty())
                    return spawned;
            }
        }

        return super.spawnAtLocation(stack, offsetY);
    }

    @Nonnull
    @Override // processInteract
    public InteractionResult mobInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
        InteractionResult parent = super.mobInteract(player, hand);
        if (parent == InteractionResult.SUCCESS)
            return parent;

        ItemStack stack = player.getItemInHand(hand);

        LocalDate date = LocalDate.now();
        if (DayOfWeek.from(date) == DayOfWeek.WEDNESDAY && stack.getItem() == Items.CLOCK) {
            if (!level().isClientSide && spawnChain > 0 && !isDuplicate) {
                if (FrogsModule.enableBigFunny) {
                    spawnCd = 50;
                    entityData.set(TALK_TIME, 80);
                }

                Vec3 pos = position();
                level().playSound(null, pos.x, pos.y, pos.z, QorkSounds.ENTITY_FROG_WEDNESDAY, SoundSource.NEUTRAL, 1F, 1F);
            }

            return InteractionResult.SUCCESS;
        }

        if (stack.is(ItemTags.WOOL) && !hasSweater()) {
            if (!level().isClientSide) {
                setSweater(true);
                Vec3 pos = position();
                level().playSound(null, pos.x, pos.y, pos.z, SoundType.WOOL.getPlaceSound(), SoundSource.PLAYERS, 1F, 1F);
                stack.shrink(1);
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isShearable(@Nonnull ItemStack item, Level world, BlockPos pos) {
        return hasSweater();
    }

    @Nonnull
    @Override
    public List<ItemStack> onSheared(Player player, @Nonnull ItemStack item, Level iworld, BlockPos pos, int fortune) {
        setSweater(false);
        Vec3 epos = position();
        level().playSound(null, epos.x, epos.y, epos.z, QorkSounds.ENTITY_FROG_SHEAR, SoundSource.PLAYERS, 1F, 1F);

        return Lists.newArrayList();
    }

    @Override // createChild
    public Frogge getBreedOffspring(@Nonnull ServerLevel sworld, @Nonnull AgeableMob otherParent) {
        if (isDuplicate)
            return null;

        float sizeMod = getSizeModifier();
        if (otherParent instanceof Frogge otherFrog) {
            if (otherFrog.isDuplicate)
                return null;

            sizeMod += otherFrog.getSizeModifier();
            sizeMod /= 2;
        }

        double regression = random.nextGaussian() / 20;
        regression *= Math.abs((sizeMod + regression) / sizeMod);

        return new Frogge(FrogsModule.frogType, level(), Mth.clamp(sizeMod + (float) regression, 0.25f, 2.0f));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        LocalDate date = LocalDate.now();
        return !stack.isEmpty() &&
                (FrogsModule.enableBigFunny && DayOfWeek.from(date) == DayOfWeek.WEDNESDAY ?
                        getTemptationItems(true) : getTemptationItems(false)).test(stack);
    }

    private Ingredient getTemptationItems(boolean nice) {
        if (temptationItems == null)
            temptationItems = new Ingredient[]{
                    Ingredient.merge(Lists.newArrayList(
                            Ingredient.of(Items.SPIDER_EYE),
                            Ingredient.of(ItemTags.FISHES)
                    )),
                    Ingredient.merge(Lists.newArrayList(
                            Ingredient.of(Items.SPIDER_EYE, Items.CLOCK),
                            Ingredient.of(ItemTags.FISHES)
                    ))
            };

        return temptationItems[nice ? 1 : 0];
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        spawnCd = compound.getInt("Cooldown");
        if (compound.contains("Chain"))
            spawnChain = compound.getInt("Chain");
        entityData.set(TALK_TIME, compound.getInt("DudeAmount"));

        float sizeModifier = compound.contains("FrogAmount") ? compound.getFloat("FrogAmount") : 1f;
        entityData.set(SIZE_MODIFIER, sizeModifier);

        isDuplicate = compound.getBoolean("FakeFrog");

        sweatered = compound.getBoolean("SweaterComp");
        setSweater(compound.getBoolean("Sweater"));

        setVoid(compound.getBoolean("Jack"));
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("FrogAmount", getSizeModifier());
        compound.putInt("Cooldown", spawnCd);
        compound.putInt("Chain", spawnChain);
        compound.putInt("DudeAmount", getTalkTime());
        compound.putBoolean("FakeFrog", isDuplicate);
        compound.putBoolean("SweaterComp", sweatered);
        compound.putBoolean("Sweater", hasSweater());
        compound.putBoolean("Jack", isVoid());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return QorkSounds.ENTITY_FROG_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(@Nonnull DamageSource damageSourceIn) {
        return QorkSounds.ENTITY_FROG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return QorkSounds.ENTITY_FROG_DIE;
    }

    protected SoundEvent getJumpSound() {
        return QorkSounds.ENTITY_FROG_JUMP;
    }

    public boolean hasSweater() {
        return entityData.get(HAS_SWEATER);
    }

    public void setSweater(boolean sweater) {
        entityData.set(HAS_SWEATER, sweater);
    }

    public boolean isVoid() {
        return entityData.get(VOID);
    }

    @Override
    protected float getJumpPower() {
        float motion = super.getJumpPower();
        if (isVoid())
            return -motion;
        else
            return motion;
    }

    public void setVoid(boolean jack) {
        if (jack && this.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).getModifier(VOID_MODIFIER_UUID) == null)
            this.getAttribute(ForgeMod.ENTITY_GRAVITY.get())
                    .addPermanentModifier(new AttributeModifier(VOID_MODIFIER_UUID, "Void gravity", -2, AttributeModifier.Operation.MULTIPLY_BASE));
        else
            this.getAttribute(ForgeMod.ENTITY_GRAVITY.get())
                    .removeModifier(VOID_MODIFIER_UUID);

        entityData.set(VOID, jack);
    }

    // Begin copypasta from EntityRabbit

    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int currentMoveTypeDuration;

    @Override
    public void customServerAiStep() {
        if (this.currentMoveTypeDuration > 0) --this.currentMoveTypeDuration;

        if (this.onGround()) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }

            FrogJumpController jumpHelper = (FrogJumpController) this.jumpControl;

            if (!jumpHelper.getIsJumping()) {
                if (this.moveControl.hasWanted() && this.currentMoveTypeDuration == 0) {
                    Path path = this.navigation.getPath();
                    Vec3 Vector3d = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());

                    if (path != null && path.getNextNodeIndex() < path.getNodeCount())
                        Vector3d = path.getNextEntityPos(this);

                    this.calculateRotationYaw(Vector3d.x, Vector3d.z);
                    this.startJumping();
                }
            } else if (!jumpHelper.canJump()) this.enableJumpControl();
        }

        this.wasOnGround = this.onGround();
    }

    @Override // spawnRunningParticles
    public boolean canSpawnSprintParticle() {
        return false;
    }

    private void calculateRotationYaw(double x, double z) {
        Vec3 pos = position();
        setYRot((float) (Mth.atan2(z - pos.z, x - pos.x) * (180D / Math.PI)) - 90.0F);
    }

    private void enableJumpControl() {
        ((FrogJumpController) this.jumpControl).setCanJump(true);
    }

    private void disableJumpControl() {
        ((FrogJumpController) this.jumpControl).setCanJump(false);
    }

    private void updateMoveTypeDuration() {
        if (this.moveControl.getSpeedModifier() < 2.2D)
            this.currentMoveTypeDuration = 10;
        else
            this.currentMoveTypeDuration = 1;
    }

    private void checkLandingDelay() {
        this.updateMoveTypeDuration();
        this.disableJumpControl();
    }

    @Override
    public void onSyncedDataUpdated(@Nonnull EntityDataAccessor<?> parameter) {
        if (parameter.equals(SIZE_MODIFIER))
            refreshDimensions();

        super.onSyncedDataUpdated(parameter);
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        double d0 = this.moveControl.getSpeedModifier();

        if (d0 > 0.0D) {
            Vec3 motion = getDeltaMovement();
            double d1 = motion.x * motion.x + motion.z * motion.z;

            if (d1 < 0.01) {
                this.moveRelative(0.1F, new Vec3(0.0F, 0.0F, 1.0F));
            }
        }

        if (!this.level().isClientSide)
            this.level().broadcastEntityEvent(this, (byte) 1);
    }

    public void setMovementSpeed(double newSpeed) {
        this.getNavigation().setSpeedModifier(newSpeed);
        this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), newSpeed);
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping);

        if (jumping)
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte id) {
        if (id == 1) {
//			this.createRunningParticles();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else
            super.handleEntityEvent(id);
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(getSizeModifier());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        entityData.set(SIZE_MODIFIER, buffer.readFloat());
    }

    @Nonnull
    @Override
    public EntityDimensions getDimensions(@Nonnull Pose poseIn) {
        return super.getDimensions(poseIn).scale(this.getSizeModifier());
    }

    public class FrogJumpController extends JumpControl {
        private boolean canJump;

        public FrogJumpController() {
            super(Frogge.this);
        }

        public boolean getIsJumping() {
            return this.jump;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean canJumpIn) {
            this.canJump = canJumpIn;
        }

        @Override
        public void tick() {
            if (this.jump) {
                startJumping();
                this.jump = false;
            }
        }
    }

    public class FrogMoveController extends MoveControl {
        private double nextJumpSpeed;

        public FrogMoveController() {
            super(Frogge.this);
        }

        @Override
        public void tick() {
            if (onGround() && !jumping && !((FrogJumpController) jumpControl).getIsJumping())
                setMovementSpeed(0.0D);
            else if (this.hasWanted()) setMovementSpeed(this.nextJumpSpeed);

            super.tick();
        }

        @Override
        public void setWantedPosition(double x, double y, double z, double speedIn) {
            if (isInWater()) speedIn = 1.5D;

            super.setWantedPosition(x, y, z, speedIn);

            if (speedIn > 0.0D) this.nextJumpSpeed = speedIn;
        }
    }

    public class FrogPanicGoal extends PanicGoal {

        public FrogPanicGoal(double speedIn) {
            super(Frogge.this, speedIn);
        }

        @Override
        public void tick() {
            super.tick();
            setMovementSpeed(this.speedModifier);
        }
    }

}