package xyz.nifeather.morph.client.mixin.accessors;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor
{
    @Invoker
    void callSetLivingEntityFlag(int flag, boolean bl);

    /**
     * 取“不含 SCALE 属性”的原始碰撞箱尺寸。
     * 服务端（插件 VanillaDisguiseProvider#tryModifyPlayerDimensions）用的就是原始箱子，
     * 客户端镜像碰撞箱时必须用同一口径，否则两边会差一个 SCALE 倍率。
     */
    @Invoker
    EntityDimensions callGetDefaultDimensions(Pose pose);
}
