package app.moreo.chatpimper.mixin.client;

import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.time.Instant;

@Mixin(ChatMessageC2SPacket.class)
public interface ChatMessageC2SPacketAccessors {

    @Accessor
    String getChatMessage();

    @Accessor
    Instant getTimestamp();

    @Accessor
    long getSalt();

    @Accessor
    MessageSignatureData getSignature();

    @Accessor
    LastSeenMessageList.Acknowledgment getAcknowledgment();

}
