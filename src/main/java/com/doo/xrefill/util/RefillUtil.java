package com.doo.xrefill.util;

import com.doo.xrefill.Refill;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.BiConsumer;

/**
 * 补充工具
 */
public class RefillUtil {

    /**
     * 不是同一种物品
     */
    private static final int DIFF = 20;

    /**
     * screen slot index
     */
    private static final int ARMOR_IDX = 5;
    private static final int MAIN_HAND_IDX = 36;
    private static final int OFF_HAND_IDX = 45;

    /**
     * 补充
     *
     * @param player 本地玩家
     * @param stack  this
     * @param item   item
     */
    public static void refill(PlayerEntity player, ItemStack stack, Item item) {
        if (!Refill.option.enable) {
            return;
        }
        ClientPlayerInteractionManager manager = MinecraftClient.getInstance().interactionManager;
        if (manager == null) {
            return;
        }
        ifRefill((current, next) -> MinecraftClient.getInstance().execute(() -> {
            // button = 0 mean left click in inventory slot
            manager.clickSlot(0, next, 0, SlotActionType.PICKUP, player);
            manager.clickSlot(0, current, 0, SlotActionType.PICKUP, player);
        }), player, stack, item);
    }

    private static void ifRefill(BiConsumer<Integer, Integer> refillSetter, PlayerEntity player, ItemStack stack, Item item) {
        // 找到当前操作的栈
        int current = -1;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (player.getEquippedStack(slot) == stack) {
                current = getEquipmentSlotInScreen(slot, player.getInventory().selectedSlot);
                break;
            }
        }
        if (current == -1) {
            return;
        }

        DefaultedList<ItemStack> main = player.getInventory().main;
        // sort number
        double min = DIFF, prev = DIFF;
        // temp stack
        ItemStack tmp;
        int next = -1;
        for (int i = 0; i < main.size(); i++) {
            tmp = main.get(i);
            // if min < prev
            if (tmp.getItem() == item && (min = Math.min(min, getSortNum(tmp, item))) < prev) {
                next = i;
                prev = min;
            }
        }
        if (next == -1) {
            return;
        }
        if (next < 9) {
            next += MAIN_HAND_IDX;
        }
        refillSetter.accept(current, next);
    }

    private static int getEquipmentSlotInScreen(EquipmentSlot slot, int selectedSlot) {
        int armorIdx = 0;
        switch (slot) {
            case MAINHAND:
                return selectedSlot + MAIN_HAND_IDX;
            case OFFHAND:
                return OFF_HAND_IDX;
            case FEET:
                armorIdx++;
            case LEGS:
                armorIdx++;
            case CHEST:
                armorIdx++;
            case HEAD:
                return ARMOR_IDX + armorIdx;
            default:
                return -1;
        }
    }

    /**
     * 是相同的物品
     *
     * @param itemStack 物品1
     * @param item2     物品2
     * @return true or false
     */
    private static double getSortNum(ItemStack itemStack, Item item2) {
        int sortNum = DIFF;
        Item item = itemStack.getItem();
        if (itemStack.isOf(item2)) {
            sortNum = 1;
        } else if (item.getClass() == item2.getClass()) {
            sortNum = 2;
        } else if ((item.isFood() && item2.isFood())) {
            sortNum = 3;
        } else if ((item.getGroup() == ItemGroup.BUILDING_BLOCKS && item2.getGroup() == ItemGroup.BUILDING_BLOCKS)) {
            sortNum = 4;
        }
        return sortNum + (itemStack.getMaxDamage() - itemStack.getDamage()) / 100000D;
    }
}
