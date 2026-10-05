package com.cntrident.fakevoidtrading;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

/** Snapshot of the exact displayed quote; discounts and remaining uses may change. */
record OfferSelection(int index, ItemStack baseCost, ItemStack secondCost, ItemStack result) {
    static OfferSelection capture(int index, MerchantOffer offer) {
        return new OfferSelection(index, offer.getBaseCostA().copy(), offer.getCostB().copy(), offer.getResult().copy());
    }
    boolean matches(MerchantOffer offer) {
        return ItemStack.matches(baseCost, offer.getBaseCostA())
                && ItemStack.matches(secondCost, offer.getCostB()) && ItemStack.matches(result, offer.getResult());
    }
}
