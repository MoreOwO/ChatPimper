package app.moreo.chatpimper.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessors {

    @Accessor
    LastSeenMessagesCollector getLastSeenMessagesCollector();

    @Accessor
    MessageChain.Packer getMessagePacker();

    @Accessor
    MinecraftClient getClient();
}
