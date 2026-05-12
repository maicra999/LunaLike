package cc.maicra999.lunalike.event;

import cc.maicra999.lunalike.LunaLike;
import cc.maicra999.lunalike.util.ChatUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatEventListener {

    private static final String ORIGINAL_MESSAGE_START_TOKEN =
            ChatUtil.COLOR_CHAR + "u" + ChatUtil.COLOR_CHAR + "u" + ChatUtil.COLOR_CHAR + "s";
    private static final String ORIGINAL_MESSAGE_END_TOKEN =
            ChatUtil.COLOR_CHAR + "u" + ChatUtil.COLOR_CHAR + "u" + ChatUtil.COLOR_CHAR + "x";

    private final LunaLike lunaLike;
    private final Cache<PlayerChatEvent, TranscriptionCacheEntry> transcriptionCache =
            CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    public ChatEventListener(LunaLike lunaLike) {
        this.lunaLike = lunaLike;
    }

    @Subscribe(priority = 100)
    public void onPlayerChatPre(PlayerChatEvent event) {
        String message = getCurrentMessage(event);
        Component transcribed = lunaLike.getTranscription().transcribe(message);
        if (transcribed == null) {
            return;
        }
        transcriptionCache.put(event, new TranscriptionCacheEntry(transcribed, message));

        if (!lunaLike.getConfig().pluginCompatibilityMode) {
            return;
        }

        // Make other plugins recognize the chat message with transcribed content included
        // This can be useful when there is a plugin that sends the message to a Discord channel for example
        String content = PlainTextComponentSerializer.plainText()
                .serialize(MiniMessage.miniMessage()
                        .deserialize(
                                lunaLike.getConfig().contentFormat,
                                Placeholder.component("transcribed", transcribed),
                                Placeholder.unparsed("original", message)));
        event.setResult(PlayerChatEvent.ChatResult.message(content));
    }

    @Subscribe(priority = -100)
    public void onPlayerChatPost(PlayerChatEvent event) {
        String message = getCurrentMessage(event);
        if (lunaLike.getConfig().pluginCompatibilityMode) {
            // Use the original message from cache
            TranscriptionCacheEntry entry = transcriptionCache.getIfPresent(event);
            if (entry != null) {
                message = entry.original();
            }
        }

        Component transcribed = null;
        TranscriptionCacheEntry entry = transcriptionCache.getIfPresent(event);
        if (entry != null) {
            transcribed = entry.transcribed();
        }

        Component content;
        if (transcribed == null) {
            content = Component.text(message);
        } else {
            content = MiniMessage.miniMessage()
                    .deserialize(
                            lunaLike.getConfig().contentFormat,
                            Placeholder.component("transcribed", transcribed),
                            Placeholder.unparsed("original", message));
        }

        Component currentServerSender = MiniMessage.miniMessage()
                .deserialize(
                        lunaLike.getConfig().senderFormat,
                        Placeholder.unparsed("player", event.getPlayer().getUsername()));
        Component externalServerSender = MiniMessage.miniMessage()
                .deserialize(
                        lunaLike.getConfig().externalSenderFormat,
                        Placeholder.unparsed("player", event.getPlayer().getUsername()),
                        Placeholder.unparsed(
                                "server",
                                event.getPlayer()
                                        .getCurrentServer()
                                        .map(connection ->
                                                connection.getServerInfo().getName())
                                        .orElse("unknown")));

        Component currentServerMessage = MiniMessage.miniMessage()
                .deserialize(
                        lunaLike.getConfig().messageFormat,
                        Placeholder.component("sender", currentServerSender),
                        Placeholder.component("content", content));
        Component externalServerMessage = MiniMessage.miniMessage()
                .deserialize(
                        lunaLike.getConfig().messageFormat,
                        Placeholder.component("sender", externalServerSender),
                        Placeholder.component("content", content));

        String plain = PlainTextComponentSerializer.plainText().serialize(externalServerMessage);
        lunaLike.getServer().getConsoleCommandSource().sendMessage(Component.text(plain));

        String currentServer = event.getPlayer()
                .getCurrentServer()
                .map(connection -> connection.getServerInfo().getName())
                .orElse(null);

        if (!lunaLike.getConfig().forwardChatEventServers.contains(currentServer)) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
        } else if (lunaLike.getConfig().forwardOriginalMessage) {
            if (entry != null) {
                event.setResult(PlayerChatEvent.ChatResult.message(entry.original()));
            }
        }

        if (lunaLike.getConfig().sendMessageToCurrentServer) {
            event.getPlayer()
                    .getCurrentServer()
                    .ifPresent(connection -> connection.getServer().sendMessage(currentServerMessage));
        }

        if (lunaLike.getConfig().sendMessageToOtherServers) {
            for (RegisteredServer server : lunaLike.getServer().getAllServers()) {
                if (server.getServerInfo().getName().equals(currentServer)) {
                    continue;
                }
                server.sendMessage(externalServerMessage);
            }
        }
    }

    private static String getCurrentMessage(PlayerChatEvent event) {
        if (event.getResult().getMessage().isPresent()) {
            return event.getResult().getMessage().get();
        } else {
            return event.getMessage();
        }
    }

    record TranscriptionCacheEntry(Component transcribed, String original) {}
}
