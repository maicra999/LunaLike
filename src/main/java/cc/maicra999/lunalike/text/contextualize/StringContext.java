package cc.maicra999.lunalike.text.contextualize;

import cc.maicra999.lunalike.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class StringContext {

    private static final Component ERROR_COMPONENT = Component.text("⚠", NamedTextColor.RED);

    private final Component[] arguments;

    public StringContext(Component[] arguments) {
        this.arguments = arguments;
    }

    public Component toComponent(String ctxString) {
        TextComponent.Builder builder = Component.text();

        char[] chars = ctxString.toCharArray();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ChatUtil.COLOR_CHAR && i + 2 < chars.length && chars[i + 1] == '<') {
                if (!current.isEmpty()) {
                    builder.append(Component.text(current.toString()));
                    current.setLength(0);
                }

                int j = i + 2;
                StringBuilder idBuilder = new StringBuilder();
                while (j < chars.length && chars[j] != '>') {
                    if (Character.isDigit(chars[j])) {
                        idBuilder.append(chars[j]);
                    }
                    j++;
                }

                try {
                    int argId = Integer.parseInt(idBuilder.toString());
                    if (argId >= 0 && argId < arguments.length) {
                        builder.append(arguments[argId]);
                    }
                } catch (NumberFormatException e) {
                    builder.append(ERROR_COMPONENT);
                }

                i = j; // Skip to the end of the argument
            } else {
                current.append(chars[i]);
            }
        }

        if (!current.isEmpty()) {
            builder.append(Component.text(current.toString()));
        }

        return builder.build();
    }
}
