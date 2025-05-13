package net.runelite.client.plugins.discordforwarder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.*;

import java.io.IOException;

@Slf4j
@PluginDescriptor(
        name = "Discord Forwarder",
        description = "Forward chat messages to Discord",
        tags = {"discord", "chat"}
)
public class DiscordForwarderPlugin extends Plugin
{
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Inject
    private DiscordForwarderConfig config;

    private OkHttpClient httpClient;

    @Override
    protected void startUp() throws Exception
    {
        httpClient = new OkHttpClient();
        log.info("Discord Forwarder started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        if (httpClient != null)
        {
            httpClient.dispatcher().executorService().shutdown();
        }
        log.info("Discord Forwarder stopped.");
    }

    // ✅ Forward only clan chat messages containing "coffer"
    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.CLAN_CHAT)
        {
            return;
        }

        String message = event.getMessage();
        if (!message.toLowerCase().contains("coffer"))
        {
            return;
        }

        String user = event.getName();
        String content = String.format("**%s:** %s", user, message);
        sendToDiscord(content);
    }

    private void sendToDiscord(String content)
    {
        if (config == null || config.webhookUrl().isEmpty())
        {
            return;
        }

        String safeContent = content.replace("\"", "\\\"");
        String json = String.format("{\"content\": \"%s\"}", safeContent);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(config.webhookUrl())
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.error("Failed to send Discord message", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                response.close();
            }
        });
    }

    @Provides
    DiscordForwarderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(DiscordForwarderConfig.class);
    }
}