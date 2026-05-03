package cc.maicra999.lunalike.text.contextualize;

import cc.maicra999.lunalike.text.contextualize.token.RawStringToken;
import cc.maicra999.lunalike.text.contextualize.token.StaticStringToken;
import cc.maicra999.lunalike.text.contextualize.token.StringToken;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;

public class TokenizedString {

    private final StringToken[] tokens;

    private TokenizedString(StringToken[] tokens) {
        this.tokens = tokens;
    }

    public Contextualized toContextString() {
        Contextualizer ctx = new Contextualizer();
        StringBuilder builder = new StringBuilder();

        for (StringToken token : tokens) {
            String contextualized = token.contextualize(ctx);
            if (contextualized != null) {
                builder.append(contextualized);
            }
        }

        return new Contextualized(builder.toString(), new StringContext(ctx.arguments.toArray(new Component[0])));
    }

    public static class Contextualizer {

        private final List<Component> arguments = new ArrayList<>();

        public int addArgument(Component argument) {
            arguments.add(argument);
            return arguments.size() - 1;
        }
    }

    public record Contextualized(String string, StringContext context) {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<StringToken> tokens = new ArrayList<>();

        private Builder() {}

        public Builder append(StringToken token) {
            tokens.add(token);
            return this;
        }

        public Builder append(StringToken... tokens) {
            for (StringToken token : tokens) {
                append(token);
            }
            return this;
        }

        public Builder append(String rawString) {
            return append(new RawStringToken(rawString));
        }

        public TokenizedString build() {
            return new TokenizedString(tokens.toArray(new StringToken[0]));
        }
    }

    // Util
    public static TokenizedString of(String rawString) {
        return builder().append(rawString).build();
    }

    public static TokenizedString of(String input, String... staticWords) {
        StringBuilder current = new StringBuilder();
        TokenizedString.Builder builder = TokenizedString.builder();

        char[] chars = input.toCharArray();
        outer:
        for (int i = 0; i < chars.length; i++) {
            for (String word : staticWords) {
                int advance = word.length();
                if (i + advance <= chars.length
                        && input.substring(i, i + advance).equals(word)) {
                    if (!current.isEmpty()) {
                        builder.append(current.toString());
                        current.setLength(0);
                    }
                    builder.append(new StaticStringToken(word));
                    i += advance - 1;
                    continue outer;
                }
            }
            current.append(chars[i]);
        }
        if (!current.isEmpty()) {
            builder.append(current.toString());
        }

        return builder.build();
    }
}
