package xyz.nifeather.morph.client.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nifeather.morph.client.graphics.transforms.Transformer;
import xyz.nifeather.morph.client.utilties.MinecraftClientMixinUtils;
import xyz.nifeather.morph.client.utilties.Screens;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin
{
    //@Shadow @Final private YggdrasilAuthenticationService authenticationService;

    @Shadow @Final public File gameDirectory;

    @Inject(method = "runTick", at = @At("RETURN"))
    private void featherMorph$onClientRender(boolean tick, CallbackInfo ci)
    {
        Transformer.onClientRenderEnd(Minecraft.getInstance());
    }

    /*
    @Inject(method = "setLevel", at = @At("HEAD"))
    private void featherMorph$onJoinServer(ClientLevel world, ReceivingLevelScreen.Reason worldEntryReason, CallbackInfo ci)
    {
        MinecraftClientMixinUtils.setApiService(this.authenticationService, this.gameDirectory);
    }*/

    // 26.2 起 Minecraft#setScreen 已被移除，界面切换的钩子移到了 GuiMixin
}
