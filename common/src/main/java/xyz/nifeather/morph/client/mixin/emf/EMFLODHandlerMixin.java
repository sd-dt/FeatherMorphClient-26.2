package xyz.nifeather.morph.client.mixin.emf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.utils.EMFLODHandler;
import xyz.nifeather.morph.client.entities.IMorphClientEntity;

/**
 * EMF LOD frame skipping tests the entity that is currently being animated through the render state,
 * so disguise entities are forced to LOD 0 to keep them fully animated.
 */
@Mixin(EMFLODHandler.class)
public abstract class EMFLODHandlerMixin
{
    @Inject(method = "getLODFactorOfEntity", remap = false, at = @At("HEAD"), cancellable = true)
    private static void morphclient$modifyLODFactor(EMFEntityRenderState renderState, CallbackInfoReturnable<Integer> cir)
    {
        if (renderState.emfEntity() instanceof IMorphClientEntity morphClientEntity
                && morphClientEntity.featherMorph$isDisguiseEntity())
            cir.setReturnValue(0);
    }
}
