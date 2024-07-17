package me.drpot.discordWebhook;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuppressWarnings("ALL")
public final class DiscordWebhook extends JavaPlugin implements Listener {

    private String webhookUrl;
    private Boolean chatalerts;
    private Boolean joinalerts;
    private Boolean leavealerts;
    private Boolean deathalerts;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        webhookUrl = config.getString("webhook-url", "");
        chatalerts = config.getBoolean("chat-alerts");
        joinalerts = config.getBoolean("join-alerts");
        leavealerts = config.getBoolean("disconnect-alert");
        deathalerts = config.getBoolean("death-alerts");

        if (webhookUrl.isEmpty()) {
            getLogger().warning("Webhook URL is not set in the config.yml file!");
        }

        if (joinalerts) {
            getLogger().info(ChatColor.GREEN + " Join Alerts have been enabled.");
        } else {
            getLogger().info(ChatColor.RED + " Join Alerts were not enabled");
        }
        if (leavealerts) {
            getLogger().info(ChatColor.GREEN + "Leave Alerts have been enabled.");
        } else {
            getLogger().info(ChatColor.RED + "Leave Alerts were not enabled.");
        }

        if (chatalerts) {
            getLogger().info(ChatColor.GREEN + "Chat Alerts have been enabled.");
        } else {
            getLogger().info(ChatColor.RED + "Chat Alerts were not enabled.");
        }
        if (deathalerts) {
            getLogger().info(ChatColor.GREEN + "Death Alerts have been enabled.");
        } else {
            getLogger().info(ChatColor.RED + "Death Alerts were not enabled.");
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        sendDiscordEmbed("Server Status", "Server is online!", 0x00FF00);
    }

    @Override
    public @NotNull ComponentLogger getComponentLogger() {
        return super.getComponentLogger();
    }

    @Override
    public void onDisable() {
        sendDiscordEmbed("Server Status", "Server is offline!", 0xFF0000);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (joinalerts){
            sendDiscordEmbed("Player Join", event.getPlayer().getName() + " has joined the server.", 0x00FF00);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (leavealerts) {
            sendDiscordEmbed("Player Quit", event.getPlayer().getName() + " has left the server.", 0xFF0000);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (deathalerts) {
            sendDiscordEmbed("Player Death",  event.getDeathMessage(), 0x00990000);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (chatalerts) {
            sendDiscordEmbed("Chat Alert", event.getPlayer().getName() + event.getMessage(), 0x00FFFF);
        }
    }

    private void sendDiscordEmbed(String title, String description, int color) {
        if (webhookUrl.isEmpty()) return;

        String json = String.format("{\"embeds\":[{\"title\":\"%s\",\"description\":\"%s\",\"color\":%d}]}", title, description, color);

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
