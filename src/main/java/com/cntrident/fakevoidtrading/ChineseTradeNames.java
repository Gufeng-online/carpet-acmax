package com.cntrident.fakevoidtrading;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ArrayList;

final class ChineseTradeNames {
    private static final Map<String, String> NAMES = load();
    private ChineseTradeNames() {}
    private static Map<String, String> load() {
        try (var stream = ChineseTradeNames.class.getResourceAsStream("/assets/carpet-acmax/lang/trade_zh_cn.json")) {
            if (stream == null) throw new IllegalStateException("Chinese trade names missing");
            return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<Map<String, String>>() {}.getType());
        } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
    }
    static String name(ItemStack stack) {
        if (stack.isEmpty()) return "无";
        String name = NAMES.getOrDefault(stack.getItem().getDescriptionId(),
                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        var enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        var labels = new ArrayList<String>();
        for (var holder : enchantments.keySet()) {
            var id = holder.unwrapKey().map(k -> k.identifier()).orElse(null);
            if (id != null) labels.add(NAMES.getOrDefault("enchantment." + id.getNamespace() + "." + id.getPath(), id.toString())
                    + " " + enchantments.getLevel(holder));
        }
        labels.sort(String::compareTo);
        return name + (labels.isEmpty() ? "" : "（" + String.join("、", labels) + "）");
    }
    static String stack(ItemStack stack) { return name(stack) + " ×" + stack.getCount(); }
}
