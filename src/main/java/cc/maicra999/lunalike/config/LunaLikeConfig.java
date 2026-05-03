package cc.maicra999.lunalike.config;

import cc.maicra999.lunalike.text.transcribe.GoogleConverter;
import cc.maicra999.lunalike.text.transcribe.YukiKanaConverter;
import java.util.function.Function;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class LunaLikeConfig {

    public TranscriptionMode transcriptionMode = TranscriptionMode.GOOGLE;
    public int maxSourceMessageLength = 150;

    public String senderFormat = "\\<<player>>";
    public String externalSenderFormat = "<gray>\\<<player>@<server>></gray>";
    public String contentFormat = "<transcribed> <dark_gray>(<original>)</dark_gray>";
    public String messageFormat = "<sender> <content>";

    public boolean pluginCompatibilityMode = true;
    public boolean sendMessageToCurrentServer = true;
    public boolean sendMessageToOtherServers = true;
    public boolean forwardChatEvents = false;

    public enum TranscriptionMode {
        NONE(Function.identity()),
        HIRAGANA(YukiKanaConverter::conv),
        GOOGLE(message -> GoogleConverter.convert(YukiKanaConverter.conv(message))),
        ;

        private final Function<String, String> transcriber;

        TranscriptionMode(Function<String, String> transcriber) {
            this.transcriber = transcriber;
        }

        public String transcribe(String message) {
            return transcriber.apply(message);
        }
    }
}
