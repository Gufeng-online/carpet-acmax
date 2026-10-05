package com.cntrident.fakevoidtrading;

import com.gufeng.settings.ACMAXSettings;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TradeChoicesTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        for (var item : java.util.List.of(Items.EMERALD, Items.BOOK, Items.DIAMOND, Items.COAL))
            item.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 64).build());
    }
    private static MerchantOffer mixed() {
        return new MerchantOffer(new ItemCost(Items.EMERALD, 2), Optional.of(new ItemCost(Items.BOOK, 1)),
                new ItemStack(Items.DIAMOND), 12, 1, 0);
    }
    @Test void rulesDefaultOff() {
        assertFalse(ACMAXSettings.fakePlayerVoidTrading);
        assertFalse(ACMAXSettings.fakePlayerVoidTradingAllowNonOp);
    }
    @Test void commandPermissionCanBeOpenedToEveryoneAndRestrictedAgain() {
        boolean original = ACMAXSettings.fakePlayerVoidTradingAllowNonOp;
        try {
            ACMAXSettings.fakePlayerVoidTradingAllowNonOp = false;
            assertFalse(FakeVoidTrading.commandPermissionAllowed(false));
            assertTrue(FakeVoidTrading.commandPermissionAllowed(true));
            ACMAXSettings.fakePlayerVoidTradingAllowNonOp = true;
            assertTrue(FakeVoidTrading.commandPermissionAllowed(false));
            assertTrue(FakeVoidTrading.commandPermissionAllowed(true));
            ACMAXSettings.fakePlayerVoidTradingAllowNonOp = false;
            assertFalse(FakeVoidTrading.commandPermissionAllowed(false));
        } finally {
            ACMAXSettings.fakePlayerVoidTradingAllowNonOp = original;
        }
    }
    @Test void autoBindingRuleIsRemoved() {
        assertThrows(NoSuchFieldException.class, () -> ACMAXSettings.class.getDeclaredField("fakePlayerVoidTradingAutoBind"));
    }
    @Test void pricePreviewUsesDiscountAndRestoresLiveQuote() {
        var offer = mixed(); offer.setSpecialPriceDiff(4);
        var preview = TradePrices.preview(List.of(offer), () -> {
            offer.resetSpecialPriceDiff(); offer.addToSpecialPriceDiff(-1);
        });
        assertEquals(1, preview.getFirst().getCostA().getCount());
        assertEquals(4, offer.getSpecialPriceDiff());
        var hover = assertInstanceOf(HoverEvent.ShowText.class,
                VillagerBindings.option("Bot", 0, preview.getFirst()).getStyle().getHoverEvent()).value().getString();
        assertTrue(hover.contains("绿宝石 ×1 + 书 ×1"), hover);
        assertThrows(IllegalStateException.class, () -> TradePrices.preview(List.of(offer), () -> {
            offer.addToSpecialPriceDiff(-2); throw new IllegalStateException("test");
        }));
        assertEquals(4, offer.getSpecialPriceDiff());
    }
    @Test void completionContainsOnlyAvailableOutputsAndRemovesDuplicates() {
        var soldOut = new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.BOOK), 12, 1, 0);
        soldOut.setToOutOfStock();
        var sell = new MerchantOffer(new ItemCost(Items.COAL, 15), new ItemStack(Items.EMERALD), 16, 1, 0);
        assertEquals(List.of("minecraft:diamond", "minecraft:emerald"), VillagerBindings.itemIds(List.of(mixed(), mixed(), sell, soldOut)));
    }
    @Test void partialBudgetOnlyWarnsAndLargeRoundCountCannotOverflow() {
        String warning = TradeSession.resourceWarning(100, 12, 12, 2);
        assertTrue(warning.contains("可支付 2 次"));
        assertTrue(warning.contains("仍继续交易"));
        assertNull(TradeSession.resourceWarning(2, 3, 12, 15));
        assertTrue(TradeSession.resourceWarning(1000000, 10000, 10000, 2).contains("10000000000"));
    }
    @Test void affordabilityHandlesBothInputsAndSharedMaterialWithoutMutating() {
        var slots = NonNullList.withSize(36, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.EMERALD, 5)); slots.set(1, new ItemStack(Items.BOOK, 2));
        var inventory = new InventoryStore(slots);
        assertEquals(2, inventory.affordableTrades(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertEquals(1, inventory.affordableTrades(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.EMERALD, 2)));
        assertEquals(0, inventory.affordableTrades(new ItemStack(Items.BOOK, 3), ItemStack.EMPTY));
        assertEquals(5, slots.get(0).getCount());
    }
    @Test void clickSuggestsExactQuoteWithRoundLastWithoutExecuting() {
        var line = VillagerBindings.option("TradeBot", 2, mixed());
        assertNull(line.getStyle().getClickEvent());
        assertEquals(2, line.getSiblings().size());
        assertEquals(" [按轮次]", line.getSiblings().getFirst().getString());
        assertEquals(" [按数量]", line.getSiblings().get(1).getString());
        var click = assertInstanceOf(ClickEvent.SuggestCommand.class, line.getSiblings().getFirst().getStyle().getClickEvent());
        assertEquals("/voidtrade TradeBot offer 3 cycle ", click.command());
        assertEquals("/voidtrade TradeBot offer 3 cycle 100", click.command() + "100");
        var countClick = assertInstanceOf(ClickEvent.SuggestCommand.class, line.getSiblings().get(1).getStyle().getClickEvent());
        assertEquals("/voidtrade TradeBot offer 3 count ", countClick.command());
        var hover = assertInstanceOf(HoverEvent.ShowText.class, line.getStyle().getHoverEvent()).value().getString();
        assertTrue(hover.contains("绿宝石 ×2 + 书 ×1 → 钻石 ×1"), hover);
    }
    @Test void countGoalUsesNewOutputsAndPartialBudgetDoesNotRejectTarget() {
        assertEquals(3, TradeSession.tradesForCount(10, 4));
        assertFalse(TradeSession.goalReached(true, 100, 8, 10));
        assertTrue(TradeSession.goalReached(true, 1, 12, 10));
        assertFalse(TradeSession.goalReached(false, 1, 1000, 2));
        assertTrue(TradeSession.countWarning(10, 4, 2).contains("仍尽力交易"));
        assertNull(TradeSession.countWarning(10, 4, 3));
    }
    @Test void newOfferCommandSupportsCycleCountAndOldNumericForm() {
        for (boolean hasItem : new boolean[]{false, true}) {
            var index = FakeVoidTrading.offerArguments(hasItem).build().getChild("offer");
            assertNotNull(index.getChild("cycle").getChild("amount").getCommand());
            assertNotNull(index.getChild("count").getChild("amount").getCommand());
            assertNotNull(index.getChild("cycles").getCommand());
        }
    }
    @Test void sellingQuoteAlsoShowsChineseCosts() {
        var offer = new MerchantOffer(new ItemCost(Items.COAL, 15), new ItemStack(Items.EMERALD), 16, 1, 0);
        var hover = assertInstanceOf(HoverEvent.ShowText.class,
                VillagerBindings.option("Bot", 0, offer).getStyle().getHoverEvent()).value().getString();
        assertTrue(hover.contains("煤炭 ×15 → 绿宝石 ×1"), hover);
    }
    @Test void soldOutQuoteShowsPriceButCannotBeClicked() {
        var offer = mixed();
        for (int i = 0; i < 12; i++) offer.increaseUses();
        var line = VillagerBindings.option("Bot", 0, offer);
        assertNull(line.getStyle().getClickEvent());
        assertTrue(line.getString().contains("已售罄"));
        assertNotNull(line.getStyle().getHoverEvent());
        assertEquals(2, line.getSiblings().size());
        for (var button : line.getSiblings()) {
            assertNull(button.getStyle().getClickEvent());
            assertNotNull(button.getStyle().getHoverEvent());
        }
    }
    @Test void snapshotsAllowDiscountsButRejectChangedComponents() {
        var offer = mixed(); var saved = OfferSelection.capture(0, offer);
        offer.setSpecialPriceDiff(-1);
        assertTrue(saved.matches(offer));
        offer.getResult().set(DataComponents.CUSTOM_NAME, Component.literal("changed"));
        assertFalse(saved.matches(offer));
        assertFalse(saved.result().has(DataComponents.CUSTOM_NAME));
    }
    @Test void materialShortfallNeverCommitsPartialPayment() {
        var slots = NonNullList.withSize(36, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.EMERALD, 5)); slots.set(1, new ItemStack(Items.BOOK, 2));
        var plan = new InventoryStore(slots);
        assertTrue(TradeSession.canPay(plan, new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertTrue(TradeSession.canPay(plan, new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertFalse(TradeSession.canPay(plan, new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertFalse(TradeSession.canPay(new InventoryStore(slots), new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK, 3)));
        assertEquals(5, slots.get(0).getCount()); assertEquals(2, slots.get(1).getCount());
        assertTrue(TradeSession.canPay(new InventoryStore(slots), new ItemStack(Items.BOOK, 2), ItemStack.EMPTY));
    }
}
