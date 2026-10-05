package com.cntrident.fakevoidtrading;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryStoreTest {
    @Test void stackedBoxesExpandWithComponentsAndLeaveOneOutputSlot() {
        var slots = inventory();
        var stacked = box(new ItemStack(Items.BOOK, 2)); stacked.setCount(3);
        stacked.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("boxes"));
        slots.set(0, stacked);
        var plan = new InventoryStore(slots);
        assertEquals(1, plan.expandStackedBoxes()); assertTrue(plan.hasStackedBoxes());
        assertTrue(plan.take(new ItemStack(Items.BOOK, 2)));
        assertEquals(3, slots.get(0).getCount());
        assertEquals("boxes", slots.get(0).get(DataComponents.CUSTOM_NAME).getString());
        var crowded = inventory();
        for (int i = 0; i < 34; i++) crowded.set(i, new ItemStack(Items.STONE, 64));
        crowded.set(0, stacked.copy());
        var partial = new InventoryStore(crowded);
        assertEquals(1, partial.expandStackedBoxes()); assertTrue(partial.hasStackedBoxes());
        assertTrue(partial.canReceive(new ItemStack(Items.DIAMOND)));
    }
    @Test void preparesOneEmptyBoxPerRequestAndPrefersItOverFilledBoxes() {
        var slots = inventory(); var stack = box(); stack.setCount(64); slots.set(0, stack);
        slots.set(1, new ItemStack(Items.DIAMOND, 64));
        var plan = new InventoryStore(slots);
        assertEquals(1, plan.prepareOutput(new ItemStack(Items.DIAMOND)));
        assertEquals(0, plan.prepareOutput(new ItemStack(Items.DIAMOND)));
        assertEquals(64, plan.storeAvailableInBoxes(new ItemStack(Items.DIAMOND), 64));
        assertEquals(1, plan.prepareOutput(new ItemStack(Items.DIAMOND)));
        assertEquals(0, plan.prepareOutput(new ItemStack(Items.DIAMOND)));
        assertEquals(64, stack.getCount());
    }
    @Test void emptyBoxIsFilledBeforeAnEarlierMatchingBox() {
        var slots = inventory(); slots.set(0, box(new ItemStack(Items.DIAMOND, 60)));
        slots.set(1, box()); slots.set(2, new ItemStack(Items.DIAMOND, 4));
        var plan = new InventoryStore(slots);
        assertEquals(4, plan.storeAvailableInBoxes(new ItemStack(Items.DIAMOND), 4));
        var inventory = new net.minecraft.world.entity.player.Inventory(null, new net.minecraft.world.entity.EntityEquipment());
        plan.commit(inventory);
        assertEquals(60, inventory.getItem(0).get(DataComponents.CONTAINER).copyOne().getCount());
        assertEquals(4, inventory.getItem(1).get(DataComponents.CONTAINER).copyOne().getCount());
    }
    @Test void paymentExpansionOnlyOpensNeededMaterialAndReusesIt() {
        var slots = inventory(); var empty = box(); empty.setCount(64); slots.set(0, empty);
        var books = box(new ItemStack(Items.BOOK, 8)); books.setCount(64); slots.set(1, books);
        slots.set(2, new ItemStack(Items.EMERALD, 64));
        var plan = new InventoryStore(slots);
        assertEquals(1, plan.preparePayment(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertEquals(0, plan.preparePayment(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        for (int i = 0; i < 8; i++) assertTrue(TradeSession.canPay(plan, new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertEquals(1, plan.preparePayment(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertTrue(TradeSession.canPay(plan, new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BOOK)));
        assertEquals(64, empty.getCount()); assertEquals(64, books.getCount());
    }
    @Test void storesWhatFitsAndLeavesOverflowInInventory() {
        var slots = inventory(); slots.set(0, new ItemStack(Items.DIAMOND, 8));
        var contents = NonNullList.withSize(27, new ItemStack(Items.STONE, 64));
        contents.set(0, new ItemStack(Items.DIAMOND, 62));
        var almostFull = box(); almostFull.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        slots.set(1, almostFull);
        var plan = new InventoryStore(slots);
        assertEquals(2, plan.storeAvailableInBoxes(new ItemStack(Items.DIAMOND), 8));
        assertTrue(plan.take(new ItemStack(Items.DIAMOND, 6)));
        assertEquals(8, slots.get(0).getCount());
        assertEquals(62, almostFull.get(DataComponents.CONTAINER).copyOne().getCount());
        var noBoxes = inventory(); noBoxes.set(0, new ItemStack(Items.DIAMOND, 8));
        assertEquals(0, new InventoryStore(noBoxes).storeAvailableInBoxes(new ItemStack(Items.DIAMOND), 8));
        assertEquals(8, noBoxes.get(0).getCount());
    }
    @Test void extraEmeraldsCannotPayAfterTheOtherIngredientIsExhausted() {
        var slots = inventory(); slots.set(0, new ItemStack(Items.EMERALD, 64)); slots.set(1, new ItemStack(Items.BOOK, 2));
        var plan = new InventoryStore(slots);
        var first = new ItemStack(Items.EMERALD, 2); var second = new ItemStack(Items.BOOK);
        assertEquals(2, plan.affordableTrades(first, second));
        assertTrue(TradeSession.canPay(plan, first, second)); assertTrue(TradeSession.canPay(plan, first, second));
        assertEquals(0, plan.affordableTrades(first, second));
        assertTrue(plan.take(new ItemStack(Items.EMERALD, 60)));
        assertEquals(64, slots.get(0).getCount());
    }
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        // Tests need only stack limits, while a running server binds full datapack defaults.
        for (var item : java.util.List.of(Items.EMERALD, Items.BOOK, Items.DIAMOND, Items.STONE, Items.SHULKER_BOX))
            item.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, item == Items.SHULKER_BOX ? 1 : 64).build());
    }
    private static NonNullList<ItemStack> inventory() { return NonNullList.withSize(36, ItemStack.EMPTY); }
    private static ItemStack box(ItemStack... contents) {
        var box = new ItemStack(Items.SHULKER_BOX);
        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(java.util.List.of(contents)));
        return box;
    }
    @Test void pullsIngredientsFromDirectAndBoxWithoutMutatingSource() {
        var slots = inventory(); slots.set(0, new ItemStack(Items.EMERALD, 2));
        slots.set(1, box(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.BOOK, 2)));
        var plan = new InventoryStore(slots);
        assertTrue(plan.take(new ItemStack(Items.EMERALD, 10)));
        assertTrue(plan.take(new ItemStack(Items.BOOK, 2)));
        assertFalse(plan.take(new ItemStack(Items.EMERALD)));
        assertEquals(2, slots.get(0).getCount());
        assertEquals(8, slots.get(1).get(DataComponents.CONTAINER).copyOne().getCount());
    }
    @Test void insufficientSpaceLeavesSourceUntouched() {
        var slots = inventory(); slots.set(0, new ItemStack(Items.DIAMOND, 8));
        var full = NonNullList.withSize(27, new ItemStack(Items.STONE, 64));
        var fullBox = box(); fullBox.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(full)); slots.set(1, fullBox);
        assertFalse(new InventoryStore(slots).storeInBoxes(new ItemStack(Items.DIAMOND), 8));
        assertEquals(8, slots.get(0).getCount());
        assertEquals(27, fullBox.get(DataComponents.CONTAINER).nonEmptyItemCopyStream().count());
    }
    @Test void outputStackingUsesCapacityAndPreservesOriginals() {
        var slots = inventory(); slots.set(0, new ItemStack(Items.DIAMOND, 8));
        slots.set(1, box(new ItemStack(Items.DIAMOND, 60)));
        var plan = new InventoryStore(slots);
        assertTrue(plan.storeInBoxes(new ItemStack(Items.DIAMOND), 8));
        assertFalse(plan.take(new ItemStack(Items.DIAMOND, 69)));
        assertEquals(8, slots.get(0).getCount());
    }
    @Test void completeOutputMustFitInDirectInventory() {
        var slots = inventory(); for (int i = 0; i < 36; i++) slots.set(i, new ItemStack(Items.STONE, 64));
        slots.set(0, new ItemStack(Items.DIAMOND, 63));
        var plan = new InventoryStore(slots);
        assertTrue(plan.canReceive(new ItemStack(Items.DIAMOND)));
        assertFalse(plan.canReceive(new ItemStack(Items.DIAMOND, 2)));
    }
    @Test void selectsUnitPriceBeforeTotalPrice() {
        var a = new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.DIAMOND, 4), 12, 1, 0);
        var b = new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(Items.DIAMOND, 1), 12, 1, 0);
        assertTrue(TradeSession.compare(a, b) < 0);
    }
    @Test void oversizedContainerIsSkippedRatherThanTruncated() {
        var contents = NonNullList.withSize(28, ItemStack.EMPTY);
        contents.set(27, new ItemStack(Items.EMERALD, 8));
        var oversized = box(); oversized.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        assertFalse(InventoryStore.usableBox(oversized));
        var slots = inventory(); slots.set(0, oversized);
        assertFalse(new InventoryStore(slots).take(new ItemStack(Items.EMERALD)));
        assertEquals(8, oversized.get(DataComponents.CONTAINER).nonEmptyItemCopyStream().findFirst().orElseThrow().getCount());
    }
    @Test void differentComponentsCannotPayForExactTemplate() {
        var named = new ItemStack(Items.BOOK);
        named.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("named"));
        var slots = inventory(); slots.set(0, named);
        assertFalse(new InventoryStore(slots).take(new ItemStack(Items.BOOK)));
        assertEquals(1, named.getCount());
    }
}
