package cc.maicra999.lunalike.text.contextualize.token;

import cc.maicra999.lunalike.text.contextualize.TokenizedString;

public interface StringToken {

    String contextualize(TokenizedString.Contextualizer ctx);
}
