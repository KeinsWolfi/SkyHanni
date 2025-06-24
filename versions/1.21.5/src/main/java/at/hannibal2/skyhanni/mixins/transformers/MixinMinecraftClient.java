package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.render.gui.GuiScreenOpenEvent;
import at.hannibal2.skyhanni.utils.IMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IMinecraftClient {
    @Unique
    private boolean rightClick;

    @Shadow
    protected abstract void doItemUse();

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @Shadow
    @Final
    @Mutable
    private Framebuffer framebuffer;

    @Inject(method = "setScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        new GuiScreenOpenEvent(screen).post();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        if (rightClick  && interactionManager != null) doItemUse();
        rightClick = false;
    }

    // Interface

    @Override
    public void skyhanniRightClick() {
        rightClick = true;
    }

    @Override
    public void skyhanniSetFramebuffer(Framebuffer framebuffer) {
        this.framebuffer = framebuffer;
    }
}
