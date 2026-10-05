package com.cntrident.fakevoidtrading;

import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;

final class ServerConfig {
    static ServerConfig get = new ServerConfig();
    int phaseTimeoutTicks = 2400;
    int gatewayArmTicks = 20;
    int postTeleportTradeDelayTicks = 6;
    double returnCooldownSeconds = 2.0;
    int trappedChestOpenTicks = 1;
    int trappedChestSearchTicks = 600;
    static void load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("fake_player_void_trading.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.isRegularFile(path)) try (var reader = Files.newBufferedReader(path)) {
                var value = gson.fromJson(reader, ServerConfig.class);
                if (value != null) get = value;
            }
            get.phaseTimeoutTicks = clamp(get.phaseTimeoutTicks, 200, 72000);
            get.gatewayArmTicks = clamp(get.gatewayArmTicks, 1, 200);
            get.postTeleportTradeDelayTicks = clamp(get.postTeleportTradeDelayTicks, 1, 200);
            get.returnCooldownSeconds = ReturnGate.sanitizeSeconds(get.returnCooldownSeconds);
            get.trappedChestOpenTicks = clamp(get.trappedChestOpenTicks, 1, 200);
            get.trappedChestSearchTicks = clamp(get.trappedChestSearchTicks, 20, 72000);
            // Persist defaults for new fields so an existing installation can edit them too.
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(get));
        } catch (java.io.IOException | RuntimeException exception) {
            get = new ServerConfig();
            FakeVoidTrading.LOGGER.error("Cannot load config; using defaults", exception);
        }
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
