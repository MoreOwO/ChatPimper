package app.moreo.chatpimper.mixin.client;

import app.moreo.chatpimper.config.Config;
import app.moreo.chatpimper.utils.SplitedMessagePart;
import app.moreo.ucl.enums.ColorType;
import app.moreo.ucl.enums.InterpolationPath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.moreo.ucl.minecraft.ChatColorMinecraftConverterKt.toChatColor;


@Mixin(ClientPlayNetworkHandler.class)
public class ChatSendMixin {

	@Shadow @Final private static Logger LOGGER;
	@Unique
	private final Config config = Config.get("default").getConfig();


	@Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
	private void init(String content, CallbackInfo info) {
		info.cancel();

		final MinecraftClient client = ((ClientPlayNetworkHandlerAccessors) this).getClient();

		final boolean isLocal;
		if (client.getCurrentServerEntry() != null) {
			isLocal = client.getCurrentServerEntry().isLocal();
		} else {
			isLocal = true;
		}

		boolean makeGradient = config.getColorGradient();
		final String cleanedContent;

		if (!config.getUseCommandToEnable()) {
			cleanedContent = content;
		} else {
			Pattern messageMatchesCommand = Pattern.compile(config.getColorGradientEnableCommand().trim() + " ?(.*)");

			final Matcher result = messageMatchesCommand.matcher(content);
			if (result.matches()) {
				makeGradient = true;
				cleanedContent = result.group(1);
			} else {
				makeGradient = false;
				cleanedContent = content;
			}
		}

		if (isLocal) {
			if (client.player != null) {
				client.player.sendMessage(createClientMessage(cleanedContent, makeGradient));
			}
		} else {
			String message = createServerMessage(cleanedContent, makeGradient);

			if (message.length() > 256 && !config.getSplitMessages()) {
				if (client.player != null) {
					final Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000));
					client.player.sendMessage(Text.translatable("error.message.tooLong").setStyle(style));
				}
				return;
			}

			if (message.length() > 256) {
				int chunkSize = 255;
				SplitedMessagePart[] smallChunks = Arrays.stream(message.split("&#(?:[0-9a-f]{6} )")).map(s -> {
					if (s.length() > chunkSize)
						return Arrays.stream(s.split("&")).map(s2 -> new SplitedMessagePart(s2, false));
					else return new SplitedMessagePart(s, true);
				}).toArray(SplitedMessagePart[]::new);
				LOGGER.info("smallChunks" + Arrays.toString(smallChunks) + "size:" + smallChunks.length);

				ArrayList<String> sentChunks = getSplitedString(smallChunks, chunkSize);

				LOGGER.info(Arrays.toString(sentChunks.toArray()) + "size:" + ((long) sentChunks.size()));

				for (String splitMessage : sentChunks) {
					if (splitMessage.isEmpty()) continue;

					final Instant instant = Instant.now();
					final long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();

					final LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = ((ClientPlayNetworkHandlerAccessors) this).getLastSeenMessagesCollector().collect();
					final MessageSignatureData messageSignatureData = ((ClientPlayNetworkHandlerAccessors) this).getMessagePacker().pack(new MessageBody(splitMessage, instant, l, lastSeenMessages.lastSeen()));
					((ClientPlayNetworkHandler)(Object)this).sendPacket(new ChatMessageC2SPacket(splitMessage, instant, l, messageSignatureData, lastSeenMessages.update()));
				}
			} else {
				Instant instant = Instant.now();
				long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();

				LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = ((ClientPlayNetworkHandlerAccessors) this).getLastSeenMessagesCollector().collect();
				MessageSignatureData messageSignatureData = ((ClientPlayNetworkHandlerAccessors) this).getMessagePacker().pack(new MessageBody(message, instant, l, lastSeenMessages.lastSeen()));
				((ClientPlayNetworkHandler)(Object)this).sendPacket(new ChatMessageC2SPacket(message, instant, l, messageSignatureData, lastSeenMessages.update()));

			}
		}
	}

	@Unique
	@NotNull
	private static ArrayList<String> getSplitedString(SplitedMessagePart[] smallChunks, int chunkSize) {
		ArrayList<String> sentChunks = new ArrayList<>();

		StringBuilder currentChunk = new StringBuilder();
		for (SplitedMessagePart smallChunk : smallChunks) {
			if (currentChunk.length() + smallChunk.content().length() + 1 > chunkSize) {
				sentChunks.add(currentChunk.toString());
				currentChunk = new StringBuilder();
			}
			if (smallChunk.isSpaceSeparated()) currentChunk.append(" ");
			currentChunk.append(smallChunk.content());
		}
		sentChunks.add(currentChunk.toString());
		return sentChunks;
	}

	@Unique
	private Text createClientMessage(String content, boolean isGradient) {
		final MutableText message = Text.empty();
		if (isGradient) {
			AtomicInteger index = new AtomicInteger();
			config.getColorGradientStart().toSpace(ColorType.HSB).rangeTo(config.getColorGradientEnd()).steps(content.length()).path(InterpolationPath.SHORTEST).forEach((color) -> {
				message.append(Text.literal(String.valueOf(content.charAt(index.get()))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color.toSpace(ColorType.SRGB).toInt()))));
				index.getAndIncrement();
			});
		} else {
			message.append(Text.literal(config.getMessagePrefix())).append(Text.literal(content)).append(Text.literal(config.getMessageSuffix()));
		}

		return message;
	}

	@Unique
	private String createServerMessage(String content, boolean isGradient) {
		StringBuilder coloredContent = new StringBuilder();

		coloredContent.append(config.getMessagePrefix());

		if (isGradient) {
			AtomicInteger index = new AtomicInteger();
			config.getColorGradientStart().toSpace(ColorType.HSB).rangeTo(config.getColorGradientEnd()).steps(content.length()).path(InterpolationPath.SHORTEST).forEach((color) -> {
				final String chatColor = toChatColor(color, config.getColorGradientFormat());
				coloredContent.append(chatColor).append(content.charAt(index.get()));
				index.getAndIncrement();
			});
		} else {
			coloredContent.append(content);
		}

		coloredContent.append(config.getMessageSuffix());

		return coloredContent.toString();
	}
}