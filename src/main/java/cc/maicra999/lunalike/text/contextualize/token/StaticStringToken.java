package cc.maicra999.lunalike.text.contextualize.token;

import cc.maicra999.lunalike.text.contextualize.TokenizedString;
import cc.maicra999.lunalike.util.ChatUtil;
import net.kyori.adventure.text.Component;

public class StaticStringToken implements StringToken {

    private final Component value;

    public StaticStringToken(Component value) {
        this.value = value;
    }

    public StaticStringToken(String value) {
        this(Component.text(value));
    }

    @Override
    public String contextualize(TokenizedString.Contextualizer ctx) {
        int argId = ctx.addArgument(value);
        StringBuilder builder = new StringBuilder(ChatUtil.COLOR_CHAR + "<");
        char[] chars = Integer.toString(argId).toCharArray();
        for (char c : chars) {
            builder.append(ChatUtil.COLOR_CHAR).append(c);
        }
        builder.append(ChatUtil.COLOR_CHAR).append(">");
        return builder.toString();
    }
}
