package app.moreo.chatpimper

import app.moreo.chatpimper.config.Config
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

object ChatPimperClientMod : ClientModInitializer {

	private val consumedKeys = mutableSetOf<Int>()

	override fun onInitializeClient() {
		val config = Config.get("default")
		ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { client: MinecraftClient ->
			config.config.boundedCommands.forEach { command ->
				if (InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, command.key) && !consumedKeys.contains(command.key)) {
					client.networkHandler?.sendChatCommand(command.command)
					consumedKeys.add(command.key)
				}

				consumedKeys.toList().forEach {
					if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, it)) {
						consumedKeys.remove(it)
					}
				}
			}
		})
	}
}