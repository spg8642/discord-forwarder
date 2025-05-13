package net.runelite.client.plugins.discordforwarder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("discordforwarder")
public interface DiscordForwarderConfig extends Config
{
    @ConfigItem(
            keyName = "webhookUrl",
            name = "Discord Webhook URL",
            description = "Your Discord channel webhook URL"
    )
    default String webhookUrl()
    {
        return "";
    }
}