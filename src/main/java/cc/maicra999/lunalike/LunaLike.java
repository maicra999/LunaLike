package cc.maicra999.lunalike;

import cc.maicra999.lunalike.config.LunaLikeConfig;
import cc.maicra999.lunalike.event.ChatEventListener;
import cc.maicra999.lunalike.text.transcribe.Transcription;
import cc.maicra999.lunalike.util.PathUtil;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@Plugin(
        id = "lunalike",
        name = "LunaLike",
        version = Constants.VERSION,
        url = "https://github.com/maicra999/LunaLike",
        description = "Lightweight Japanese chat translation for Velocity.",
        authors = {"KabanFriends"})
public class LunaLike {

    private final ProxyServer server;
    private final Logger logger;
    private final YamlConfigurationLoader configLoader;
    private final Transcription transcription;

    private LunaLikeConfig config;

    @Inject
    public LunaLike(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.configLoader = YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .path(PathUtil.fromPluginHome("config.yml"))
                .build();
        this.transcription = new Transcription(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Load plugin config
        try {
            CommentedConfigurationNode root = loadConfig();
            configLoader.save(root);
        } catch (IOException e) {
            logger.atError().log("Failed to load configuration!");
            throw new RuntimeException(e);
        }

        // Register events
        server.getEventManager().register(this, new ChatEventListener(this));
    }

    public CommentedConfigurationNode loadConfig() throws IOException {
        CommentedConfigurationNode root = configLoader.load();
        this.config = root.get(LunaLikeConfig.class);
        return root;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Transcription getTranscription() {
        return transcription;
    }

    public LunaLikeConfig getConfig() {
        return config;
    }
}
