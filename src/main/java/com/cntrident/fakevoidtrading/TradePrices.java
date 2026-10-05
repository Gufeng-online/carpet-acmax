package com.cntrident.fakevoidtrading;

import com.cntrident.fakevoidtrading.mixin.VillagerPriceInvoker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import java.util.List;

final class TradePrices {
    private TradePrices() {}
    static List<MerchantOffer> forBot(Villager villager, ServerPlayer bot) {
        if (villager.getTradingPlayer() == bot)
            return villager.getOffers().stream().map(MerchantOffer::copy).toList();
        return preview(villager.getOffers(), () -> {
            // Another player's menu carries their personal discount. A future bot interaction
            // happens after that menu closes and vanilla resets the personal price.
            if (villager.isTrading()) villager.getOffers().forEach(MerchantOffer::resetSpecialPriceDiff);
            ((VillagerPriceInvoker) villager).fakeVoidTrading$updateSpecialPrices(bot);
        });
    }
    // Vanilla computes per-player prices on live offers. Restore them even when preview fails.
    static List<MerchantOffer> preview(List<MerchantOffer> offers, Runnable applyDiscounts) {
        int[] previous = offers.stream().mapToInt(MerchantOffer::getSpecialPriceDiff).toArray();
        try {
            applyDiscounts.run();
            return offers.stream().map(MerchantOffer::copy).toList();
        } finally {
            for (int i = 0; i < offers.size(); i++) offers.get(i).setSpecialPriceDiff(previous[i]);
        }
    }
}
