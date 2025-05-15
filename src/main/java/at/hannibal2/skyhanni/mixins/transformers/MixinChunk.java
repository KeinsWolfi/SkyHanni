package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.ChunkLoadEvent;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class MixinChunk {
    @Inject(method = "fillChunk", at = @At("RETURN"))
    public void onFillChunk(byte[] p_177439_1_, int p_177439_2_, boolean p_177439_3_, CallbackInfo ci) {
        new ChunkLoadEvent((Chunk) (Object) this).post();
    }
}
