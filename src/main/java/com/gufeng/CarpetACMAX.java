package com.gufeng;

import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetACMAX implements ModInitializer {
	public static final String MOD_ID = "carpet-acmax";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Carpet-ACMAX loaded!");

		// Register Carpet extension
		CarpetServer.manageExtension(new CarpetACMAXExtension());
		new com.cntrident.fakevoidtrading.FakeVoidTrading().onInitialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
