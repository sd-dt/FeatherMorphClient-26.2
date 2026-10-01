package xyz.nifeather.morph.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nifeather.morph.client.DisguiseInstanceTracker;
import xyz.nifeather.morph.client.EntityCache;
import xyz.nifeather.morph.client.FeatherMorphClientBootstrap;
import xyz.nifeather.morph.client.entities.IHasOverrideGlowing;
import xyz.nifeather.morph.client.entities.IMorphClientEntity;
import xyz.nifeather.morph.client.mixin.accessors.LivingEntityAccessor;
import xyz.nifeather.morph.client.network.ServerHandler;
import xyz.nifeather.morph.client.syncers.DisguiseSyncer;
import xyz.nifeather.morph.client.utilties.ClientSyncerUtils;
import xyz.nifeather.morph.client.utilties.EntityCacheUtils;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Entity.class)
public abstract class EntityMixin implements IMorphClientEntity, IHasOverrideGlowing
{
    @Shadow public abstract Pose getPose();

    @Shadow public abstract void remove(Entity.RemovalReason reason);

    @Shadow protected abstract void setSharedFlag(int index, boolean value);

    @Shadow public abstract void setPose(Pose pose);

    //region IHasOverrideGlowing

    @Unique
    private boolean morphclient$overrideGlowing = false;

    @Override
    public void morphclient$overrideClientGlowing(boolean glowing)
    {
        morphclient$overrideGlowing = glowing;
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    public void morphclient$overrideGlowing(CallbackInfoReturnable<Boolean> cir)
    {
        if (morphclient$overrideGlowing)
            cir.setReturnValue(true);
    }

    //endregion IHasOverrideGlowing

    //region IMorphClientEntity

    @Unique
    private boolean featherMorph$isDisguiseEntity;

    @Unique
    private int featherMorph$masterId = -1;

    @Unique
    private DisguiseSyncer morphclient$masterSyncer;

    @Override
    public void featherMorph$setIsDisguiseEntity(int masterId, DisguiseSyncer masterSyncer)
    {
        this.featherMorph$masterId = masterId;
        this.morphclient$masterSyncer = masterSyncer;
        this.featherMorph$isDisguiseEntity = true;
    }

    @Override
    public boolean featherMorph$isDisguiseEntity()
    {
        return this.featherMorph$isDisguiseEntity;
    }

    @Override
    public int featherMorph$getMasterEntityId()
    {
        return this.featherMorph$masterId;
    }

    @Override
    public DisguiseSyncer featherMorph$getMasterSyncer()
    {
        return this.morphclient$masterSyncer;
    }

    //endregion IMorphClientEntity

    @Unique
    private Entity featherMorph$entityInstance;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void featherMorph$onInit(EntityType<?> type, Level world, CallbackInfo ci)
    {
        featherMorph$entityInstance = (Entity) (Object) this;
    }

    @Inject(method = "setGlowingTag", at = @At("RETURN"))
    private void morphClient$onGlowingCall(boolean glowing, CallbackInfo ci)
    {
        var thisInstance = ((Entity)(Object)this);
        if (thisInstance.entityTags().contains(EntityCache.tag))
            this.setSharedFlag(6, glowing);
    }

    /**
     * 26.2 下客户端可以切换「显示玩家原本模型」（渲染不再重定向到伪装实体）。
     * 视线高度必须和实际显示出来的模型一致，否则会出现“看着自己的模型、视角却停在伪装高度”的情况。
     */
    @Unique
    private boolean featherMorph$usesDisguiseEyeHeight()
    {
        if (featherMorph$entityInstance != Minecraft.getInstance().player || !ServerHandler.modifyBoundingBox)
            return false;

        if (((IMorphClientEntity) (Object) this).featherMorph$bypassesDispatcherRedirect())
            return false;

        return FeatherMorphClientBootstrap.getInstance().getModConfigData().clientViewVisible();
    }

    @Inject(method = "getEyeY", at = @At("HEAD"), cancellable = true)
    private void featherMorph$onGetEyeY(CallbackInfoReturnable<Double> cir)
    {
        if (featherMorph$usesDisguiseEyeHeight())
        {
            runIfSyncerEntityNotNull(syncerEntity ->
                    cir.setReturnValue(Minecraft.getInstance().player.getY() + syncerEntity.getEyeHeight()));
        }
    }

    @Inject(method = "getEyeHeight(Lnet/minecraft/world/entity/Pose;)F", at = @At("HEAD"), cancellable = true)
    private void featherMorph$onGetEyeHeight(Pose pose, CallbackInfoReturnable<Float> cir)
    {
        if (featherMorph$usesDisguiseEyeHeight())
        {
            runIfSyncerEntityNotNull(syncerEntity ->
                    cir.setReturnValue(syncerEntity.getEyeHeight(pose)));
        }
    }

    @Inject(method = "getEyeHeight()F", at = @At("HEAD"), cancellable = true)
    private void featherMorph$onGetStandingEyeHeight(CallbackInfoReturnable<Float> cir)
    {
        if (featherMorph$usesDisguiseEyeHeight())
        {
            runIfSyncerEntityNotNull(syncerEntity ->
                    cir.setReturnValue(syncerEntity.getEyeHeight()));
        }
    }

    // 26.2 把碰撞箱计算拆成了 makeBoundingBox() 与 makeBoundingBox(Vec3) 两个重载：
    // setPos() 走无参版本，但碰撞求解（collidedWithShapeMovingFrom）与 checkInsideBlocks 直接调用
    // Vec3 版本。只拦截无参版本会让同一 tick 内出现两个不同的碰撞箱（自己和自己打架 → 卡墙/来回抖动），
    // 因此改为拦截所有调用都会经过的 Vec3 版本，并使用调用方传入的位置。
    //
    // 尺寸口径必须和服务端插件一致：VanillaDisguiseProvider#tryModifyPlayerDimensions 用的是
    // “原始”箱子（BoundingBoxLookup 读新生成实体的 AABB，再 EntityDimensions.fixed），不含玩家 SCALE；
    // 而 LivingEntity#getDimensions 会把 SCALE 乘进去（本模组又会把玩家的 SCALE 同步给伪装实体），
    // 直接用它会得到比服务端小一圈的箱子 → 能挤进服务端不允许的位置 → 被拉回 → 撞墙抖动。
    @Inject(method = "makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;", at = @At("HEAD"), cancellable = true)
    private void featherMorph$onCalcCall(Vec3 pos, CallbackInfoReturnable<AABB> cir)
    {
        if (featherMorph$entityInstance == Minecraft.getInstance().player && ServerHandler.modifyBoundingBox)
        {
            runIfSyncerEntityNotNull(e ->
            {
                EntityDimensions dimensions = e instanceof LivingEntity living
                        ? ((LivingEntityAccessor) living).callGetDefaultDimensions(Pose.STANDING)
                        : e.getDimensions(Pose.STANDING);

                cir.setReturnValue(dimensions.makeBoundingBox(pos));
            });
        }
    }

    @Inject(method = "setRemoved", at = @At("RETURN"))
    private void morphClient$onRemoved(CallbackInfo ci)
    {
        EntityCacheUtils.postEntityRemove(featherMorph$entityInstance);
    }

    @Unique
    private void runIfSyncerEntityNotNull(Consumer<Entity> consumerifNotNull)
    {
        ClientSyncerUtils.runIfSyncerEntityValid(consumerifNotNull::accept);
    }

    @Unique
    private Pose morphClient$overridePose;

    @Override
    public void featherMorph$overridePose(@Nullable Pose newPose)
    {
        this.morphClient$overridePose = newPose;

        if (newPose != null)
            this.setPose(newPose);
    }

    @Inject(method = "getPose", at = @At("HEAD"), cancellable = true)
    private void morphClient$onPoseCall(CallbackInfoReturnable<Pose> cir)
    {
        if (morphClient$overridePose != null)
            cir.setReturnValue(morphClient$overridePose);
    }

    @Unique
    @Nullable
    private Boolean morphClient$isInvisible;

    @Override
    public void featherMorph$overrideInvisibility(boolean invisible)
    {
        if (invisible)
            this.morphClient$isInvisible = invisible;
        else
            this.morphClient$isInvisible = null;
    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void morphClient$onInvisibleCall(CallbackInfoReturnable<Boolean> cir)
    {
        if (this.morphClient$isInvisible != null)
            cir.setReturnValue(this.morphClient$isInvisible);
    }

    @Unique
    private boolean morphClient$noAcceptSetPose;

    @Override
    public void featherMorph$setNoAcceptSetPose(boolean noAccept)
    {
        this.morphClient$noAcceptSetPose = noAccept;
    }

    @Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
    private void morphClient$onSetPose(Pose pose, CallbackInfo ci)
    {
        if (this.morphClient$noAcceptSetPose)
            ci.cancel();
    }

    @WrapMethod(method = "tick")
    public void morphClient$onTick(Operation<Void> original)
    {
        var syncer = morphclient$masterSyncer;

        if (syncer == null)
        {
            original.call();
            return;
        }

        syncer.preEntityTick();
        original.call();
        syncer.postEntityTick();
    }

    @Unique
    private final List<Object> morphClient$bypassRequests = new ObjectArrayList<>();

    @Override
    public void featherMorph$requestBypassDispatcherRedirect(Object source, boolean bypass)
    {
        if (!bypass)
        {
            morphClient$bypassRequests.remove(source);
            return;
        }

        if (morphClient$bypassRequests.contains(source)) return;

        morphClient$bypassRequests.add(source);
    }

    @Override
    public boolean featherMorph$bypassesDispatcherRedirect()
    {
        return !morphClient$bypassRequests.isEmpty();
    }
}