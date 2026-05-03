package cc.maicra999.lunalike.text.contextualize.token;

import cc.maicra999.lunalike.text.contextualize.TokenizedString;

public class RawStringToken implements StringToken {

    private final String value;

    public RawStringToken(String value) {
        this.value = value;
    }

    @Override
    public String contextualize(TokenizedString.Contextualizer ctx) {
        return value;
    }
}
