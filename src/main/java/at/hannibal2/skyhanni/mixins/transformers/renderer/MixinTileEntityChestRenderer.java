package at.hannibal2.skyhanni.mixins.transformers.renderer;

import at.hannibal2.skyhanni.events.render.RenderChestEvent;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityChestRenderer.class)
public class MixinTileEntityChestRenderer {
    @Inject(method = { "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityChest;DDDFI)V" }, at = { @At("HEAD") })
    public void onDrawChest(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        new RenderChestEvent.Pre(te, x, y, z, partialTicks).post();
    }

    @Inject(method = { "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityChest;DDDFI)V" }, at = { @At("RETURN") })
    public void onDrawChestPost(final TileEntityChest te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final CallbackInfo ci) {
        new RenderChestEvent.Post(te, x, y, z, partialTicks).post();
    }
}
