package at.hannibal2.skyhanni.mixins.transformers.renderer;

import at.hannibal2.skyhanni.features.misc.ContributorManager;
import at.hannibal2.skyhanni.utils.CapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCape.class)
public class MixinLayerCape {
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    public void renderCustomCape(
        EntityLivingBase entity,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale,
        CallbackInfo ci
    ) {
        if (!(entity instanceof AbstractClientPlayer)) return;

        AbstractClientPlayer player = (AbstractClientPlayer) entity;

        if (ContributorManager.INSTANCE.shouldRenderCustomCape(player)) {
            ResourceLocation capeLocation = CapeManager.INSTANCE.getCapeResource(CapeManager.CapeType.SKYHANNI);
            Minecraft.getMinecraft().getTextureManager().bindTexture(capeLocation);
            GlStateManager.pushMatrix();

            Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player);
            if (render instanceof RenderPlayer) {
                RenderPlayer renderPlayer = (RenderPlayer) render;
                renderPlayer.getMainModel().renderCape(0.0625F);
            }

            GlStateManager.popMatrix();
            ci.cancel();
        }
    }
}
