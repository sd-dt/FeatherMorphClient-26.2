package xyz.nifeather.morph.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nifeather.morph.client.utilties.Screens;

/**
 * 26.2 把“当前界面”的处理从 {@code Minecraft} 移到了 {@code Gui}（{@code Minecraft#setScreen} 已删除），
 * 因此界面切换的钩子挂在这里；{@code screen()} 在 HEAD 处仍返回旧界面。
 */
@Mixin(Gui.class)
public abstract class GuiMixin
{
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void featherMorph$onSetScreen(Screen screenNext, CallbackInfo ci)
    {
        Screens.getInstance().onChange(Minecraft.getInstance().gui.screen(), screenNext);
    }
}
