package com.gufeng;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.gufeng.settings.ACMAXSettings;

import java.util.Map;

public class CarpetACMAXExtension implements CarpetExtension {

    @Override
    public String version() {
        return CarpetACMAX.MOD_ID;
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(ACMAXSettings.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        String path = "assets/" + CarpetACMAX.MOD_ID + "/lang/" + lang + ".json";
        return carpet.utils.Translations.getTranslationFromResourcePath(path);
    }
}
