package app.moreo.chatpimper

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object ChatPimperMod : ModInitializer {
    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger("chat-pimper")

    override fun onInitialize() {
        LOGGER.info("Initializing ChatPimper")
    }
}
