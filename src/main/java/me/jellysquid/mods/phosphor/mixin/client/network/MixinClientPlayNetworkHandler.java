package me.jellysquid.mods.phosphor.mixin.client.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler implements ClientPlayPacketListener {
    @Shadow
    private MinecraftClient client;

    @Shadow
    private ClientWorld world;

    /**
     * @reason Don't schedule light updates for unloaded chunks in the client world, since they're expensive and won't be seen.
     * @author Lucy-t
     */
    @Overwrite
    public void onUnloadChunk(UnloadChunkS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor) this.client);
        int i = packet.getX();
        int j = packet.getZ();
        ClientChunkManager clientChunkManager = this.world.getChunkManager();
        clientChunkManager.unload(i, j);
        LightingProvider lightingProvider = clientChunkManager.getLightingProvider();

        for (int k = 0; k < 16; ++k) {
            this.world.scheduleBlockRenders(i, k, j);
            //lightingProvider.updateSectionStatus(ChunkSectionPos.from(i, k, j), true);
        }

        lightingProvider.setLightEnabled(new ChunkPos(i, j), false);
    }
}
