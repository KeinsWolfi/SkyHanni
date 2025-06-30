package at.hannibal2.skyhanni.mixins.transformers.render;

import at.hannibal2.skyhanni.features.foraging.LeafXray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public abstract class MixinRenderLayers {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void onGetBlockLayer(BlockState state, CallbackInfoReturnable<RenderLayer> cir) {
        int alpha = LeafXray.getAlpha(state, null);
        if (alpha > 0 && alpha < 255) cir.setReturnValue(RenderLayer.getTranslucent());
    }
}
