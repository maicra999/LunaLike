package cc.maicra999.lunalike.text.transcribe;

import cc.maicra999.lunalike.LunaLike;
import cc.maicra999.lunalike.text.contextualize.StringContext;
import cc.maicra999.lunalike.text.contextualize.TokenizedString;
import cc.maicra999.lunalike.util.ChatUtil;
import com.google.common.collect.ImmutableMap;
import com.velocitypowered.api.proxy.Player;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class Transcription {

    private static final String TRANSCRIBE_APPLICABLE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.,!?{}[]()<>+-*/\\\"'`^#$%&~|:;@ "
                    + ChatUtil.COLOR_CHAR;
    private static final String TRANSCRIBE_TRIGGER = "abcdefghijklmnopqrstuvwxyz";

    private static final Map<Character, Character> ARGUMENT_REPLACE_MAP = ImmutableMap.<Character, Character>builder()
            .put('＜', '<')
            .put('＞', '>')
            .put('０', '0')
            .put('１', '1')
            .put('２', '2')
            .put('３', '3')
            .put('４', '4')
            .put('５', '5')
            .put('６', '6')
            .put('７', '7')
            .put('８', '8')
            .put('９', '9')
            .build();

    private LunaLike lunaLike;

    public Transcription(LunaLike lunaLike) {
        this.lunaLike = lunaLike;
    }

    public @Nullable Component transcribe(String message) {
        return transcribe(message, false);
    }

    public @Nullable Component transcribe(String message, boolean force) {
        if (message.length() > lunaLike.getConfig().maxSourceMessageLength) {
            return null;
        }

        String[] names = lunaLike.getServer().getAllPlayers().stream()
                .map(Player::getUsername)
                .toArray(String[]::new);

        TokenizedString.Contextualized result =
                TokenizedString.of(message, names).toContextString();
        String ctxString = result.string();
        StringContext ctx = result.context();

        if (!force && (!shouldTranscribe(ctxString) || !isTranscribable(ctxString))) {
            return null;
        }

        ctxString = fixArgumentTags(lunaLike.getConfig().transcriptionMode.transcribe(ctxString));
        if (ctxString.equals(result.string())) {
            return null;
        }

        return ctx.toComponent(ctxString);
    }

    private static String fixArgumentTags(String input) {
        char[] chars = input.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ChatUtil.COLOR_CHAR && i + 2 < chars.length) {
                builder.append(ChatUtil.COLOR_CHAR).append(ARGUMENT_REPLACE_MAP.get(chars[i + 1]));
                i++;
                continue;
            } else if (Character.isDigit(chars[i])) {
                builder.append(ARGUMENT_REPLACE_MAP.get(chars[i]));
                continue;
            }
            builder.append(chars[i]);
        }
        return builder.toString();
    }

    private static boolean isTranscribable(String input) {
        for (char c : input.toCharArray()) {
            if (TRANSCRIBE_APPLICABLE.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldTranscribe(String input) {
        for (char c : input.toCharArray()) {
            if (TRANSCRIBE_TRIGGER.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }
}
