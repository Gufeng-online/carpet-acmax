package com.cntrident.fakevoidtrading;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import java.util.ArrayList;
import java.util.List;

/** All operations run on copies. Failed plans never modify the player's inventory. */
public final class InventoryStore {
    private final List<ItemStack> items;
    private int preferredOutputSlot = -1;
    public InventoryStore(Inventory inventory) {
        items = new ArrayList<>();
        for (int i = 0; i < 36; i++) items.add(inventory.getItem(i).copy());
    }
    InventoryStore(Inventory inventory, int preferredOutputSlot) {
        this(inventory);
        this.preferredOutputSlot = preferredOutputSlot;
    }
    int preferredOutputSlot() { return preferredOutputSlot; }
    int directCount(ItemStack template) { return count(items, template); }
    InventoryStore(List<ItemStack> source) {
        items = source.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
    public void commit(Inventory inventory) {
        for (int i = 0; i < 36; i++) inventory.setItem(i, items.get(i));
        inventory.setChanged();
    }
    static boolean usableBox(ItemStack stack) {
        return expandableBox(stack) && stack.getCount() == 1;
    }
    static boolean expandableBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock
                && !stack.has(DataComponents.CONTAINER_LOOT)
                && stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).allItemsCopyStream().count() <= 27;
    }
    boolean hasStackedBoxes() { return items.stream().anyMatch(s -> expandableBox(s) && s.getCount() > 1); }
    int expandStackedBoxes() {
        return expandOneBox(box -> true);
    }
    private int expandOneBox(java.util.function.Predicate<ItemStack> needed) {
        if (items.stream().filter(ItemStack::isEmpty).count() <= 1) return 0;
        for (int source = 0; source < items.size(); source++) {
            var stack = items.get(source);
            if (!expandableBox(stack) || stack.getCount() <= 1 || !needed.test(stack)) continue;
            for (int i = 0; i < items.size(); i++) if (items.get(i).isEmpty()) {
                items.set(i, stack.copyWithCount(1)); stack.shrink(1); return 1;
            }
        }
        return 0;
    }
    int preparePayment(ItemStack first, ItemStack second) {
        int moved = 0;
        var needs = new ArrayList<ItemStack>(); needs.add(first.copy());
        if (!second.isEmpty()) {
            if (ItemStack.isSameItemSameComponents(first, second)) needs.getFirst().grow(second.getCount());
            else needs.add(second.copy());
        }
        for (var need : needs) while (allMatching(need) < need.getCount()) {
            if (expandOneBox(box -> count(contents(box), need) > 0) == 0) break;
            moved++;
        }
        return moved;
    }
    int prepareOutput(ItemStack output) {
        if (output.isEmpty() || !output.getItem().canFitInsideContainerItems()) return 0;
        if (preferredOutputSlot >= 0 && preferredOutputSlot < items.size()) {
            var box = items.get(preferredOutputSlot);
            if (usableBox(box) && capacity(contents(box), output) > 0) return 0;
        }
        preferredOutputSlot = findOutputBox(output, true);
        if (preferredOutputSlot >= 0) return 0;
        if (expandOneBox(InventoryStore::emptyBox) > 0) {
            preferredOutputSlot = findOutputBox(output, true);
            return 1;
        }
        preferredOutputSlot = findOutputBox(output, false);
        if (preferredOutputSlot >= 0) return 0;
        if (expandOneBox(box -> capacity(contents(box), output) > 0) > 0) {
            preferredOutputSlot = findOutputBox(output, false);
            return 1;
        }
        return 0;
    }
    private int findOutputBox(ItemStack output, boolean emptyOnly) {
        int best = -1, priority = Integer.MAX_VALUE;
        for (int i = 0; i < items.size(); i++) {
            var box = items.get(i);
            if (!usableBox(box)) continue;
            var inside = contents(box);
            boolean empty = inside.stream().allMatch(ItemStack::isEmpty);
            if (emptyOnly && !empty || capacity(inside, output) <= 0) continue;
            int score = empty ? 0 : inside.stream().anyMatch(item -> same(item, output)
                    && item.getCount() < item.getMaxStackSize()) ? 1 : 2;
            if (score < priority) { best = i; priority = score; }
        }
        return best;
    }
    private static boolean emptyBox(ItemStack box) {
        return contents(box).stream().allMatch(ItemStack::isEmpty);
    }
    private static NonNullList<ItemStack> contents(ItemStack box) {
        var result = NonNullList.withSize(27, ItemStack.EMPTY);
        box.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(result);
        return result;
    }
    public boolean take(ItemStack template) {
        if (template.isEmpty()) return true;
        int left = remove(items, template, template.getCount());
        for (ItemStack box : items) {
            if (left == 0) break;
            if (!usableBox(box)) continue;
            var inside = contents(box);
            left = remove(inside, template, left);
            box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inside));
        }
        return left == 0;
    }
    public boolean canReceive(ItemStack stack) { return capacity(items, stack) >= stack.getCount(); }
    int affordableTrades(ItemStack first, ItemStack second) {
        if (first.isEmpty()) return 0;
        int firstCost = first.getCount();
        if (!second.isEmpty() && ItemStack.isSameItemSameComponents(first, second))
            return potentialMatching(first) / (firstCost + second.getCount());
        int available = potentialMatching(first) / firstCost;
        return second.isEmpty() ? available : Math.min(available, potentialMatching(second) / second.getCount());
    }
    private int potentialMatching(ItemStack template) {
        int total = count(items, template);
        for (var box : items) if (expandableBox(box)) total += count(contents(box), template) * box.getCount();
        return total;
    }
    private int allMatching(ItemStack template) {
        int total = count(items, template);
        for (var box : items) if (usableBox(box)) total += count(contents(box), template);
        return total;
    }
    public boolean storeInBoxes(ItemStack template, int requested) {
        if (requested < 0 || count(items, template) < requested) return false;
        var plan = new InventoryStore(items);
        plan.preferredOutputSlot = preferredOutputSlot;
        if (plan.storeAvailableInBoxes(template, requested) != requested) return false;
        for (int i = 0; i < items.size(); i++) items.set(i, plan.items.get(i));
        preferredOutputSlot = plan.preferredOutputSlot;
        return true;
    }
    int storeAvailableInBoxes(ItemStack template, int requested) {
        if (requested <= 0 || template.isEmpty() || !template.getItem().canFitInsideContainerItems()) return 0;
        int left = Math.min(requested, count(items, template));
        int stored = 0;
        while (left > 0) {
            // Select or split only while there are actual items left to store.
            prepareOutput(template);
            if (preferredOutputSlot < 0) break;
            var box = items.get(preferredOutputSlot);
            var inside = contents(box);
            int moved = Math.min(left, capacity(inside, template));
            if (moved <= 0) break;
            remove(items, template, moved);
            insert(inside, template, moved);
            box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inside));
            stored += moved;
            left -= moved;
        }
        return stored;
    }
    static int count(List<ItemStack> slots, ItemStack template) {
        return slots.stream().filter(s -> same(s, template)).mapToInt(ItemStack::getCount).sum();
    }
    static int capacity(List<ItemStack> slots, ItemStack template) {
        return slots.stream().mapToInt(s -> s.isEmpty() ? template.getMaxStackSize()
                : same(s, template) ? Math.max(0, s.getMaxStackSize() - s.getCount()) : 0).sum();
    }
    private static boolean same(ItemStack a, ItemStack b) { return ItemStack.isSameItemSameComponents(a, b); }
    private static int remove(List<ItemStack> slots, ItemStack template, int left) {
        for (ItemStack slot : slots) if (same(slot, template)) {
            int n = Math.min(slot.getCount(), left); slot.shrink(n); left -= n;
            if (left == 0) break;
        }
        return left;
    }
    private static int insert(List<ItemStack> slots, ItemStack template, int left) {
        for (int pass = 0; pass < 2; pass++) for (int i = 0; i < slots.size(); i++) {
            ItemStack slot = slots.get(i);
            if (pass == 0 && !slot.isEmpty() && same(slot, template)) {
                int n = Math.min(left, Math.max(0, slot.getMaxStackSize() - slot.getCount()));
                slot.grow(n); left -= n;
            } else if (pass == 1 && slot.isEmpty()) {
                int n = Math.min(left, template.getMaxStackSize()); slots.set(i, template.copyWithCount(n)); left -= n;
            }
            if (left == 0) return 0;
        }
        return left;
    }
}
