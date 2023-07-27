package app.moreo.chatpimper.mixin.client;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatMessageC2SPacket.class)
public class MaxLengthOverrider {

    @Inject(at = @At("HEAD"), method = "write", cancellable = true)
    public void write(PacketByteBuf buf, CallbackInfo info) {
        info.cancel();

        buf.writeString(((ChatMessageC2SPacketAccessors) this).getChatMessage(), ((ChatMessageC2SPacketAccessors) this).getChatMessage().length());
        buf.writeInstant(((ChatMessageC2SPacketAccessors) this).getTimestamp());
        buf.writeLong(((ChatMessageC2SPacketAccessors) this).getSalt());
        buf.writeNullable(((ChatMessageC2SPacketAccessors) this).getSignature(), MessageSignatureData::write);
        ((ChatMessageC2SPacketAccessors) this).getAcknowledgment().write(buf);
    }
}
