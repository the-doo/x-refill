package com.doo.xrefill.util;

import com.doo.xrefill.Refill;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 补充工具
 */
@Environment(EnvType.CLIENT)
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

    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor();

    /**
     * 补充
     *
     * @param player 本地玩家
     * @param stack  stack
     * @param slot   slot
     */
    public static void tryRefill(PlayerEntity player, ItemStack stack, EquipmentSlot slot) {
        if (!Refill.option.enable || player.isCreative()) {
            return;
        }
        if (stack.isEmpty() || stack.isStackable() && stack.getCount() > 1 || stack.isDamageable() && stack.getMaxDamage() - stack.getDamage() > 1) {
            return;
        }
        // if open screen don't do anything
        if (MinecraftClient.getInstance().currentScreen != null) {
            return;
        }
        ClientPlayerInteractionManager manager = MinecraftClient.getInstance().interactionManager;
        if (manager == null) {
            return;
        }

        ifRefill((current, next) -> {
            // button = 0 mean left click in inventory slot
            manager.clickSlot(0, next, 0, SlotActionType.PICKUP, player);
            EXEC.schedule(() -> {
                manager.clickSlot(0, current, 0, SlotActionType.PICKUP, player);
                // rollback if set it wrong or can set empty back
                EXEC.schedule(() -> manager.clickSlot(0, next, 0, SlotActionType.PICKUP, player), Refill.option.delay, TimeUnit.MILLISECONDS);
            }, Refill.option.delay, TimeUnit.MILLISECONDS);
        }, player, stack, slot);
    }

    private static void ifRefill(BiConsumer<Integer, Integer> refillSetter, PlayerEntity player, ItemStack stack, EquipmentSlot slot) {
        // 找到当前操作的栈
        int current = getEquipmentSlotInScreen(slot, player.getInventory().selectedSlot);
        if (current == -1) {
            return;
        }

        DefaultedList<ItemStack> main = player.getInventory().main;
        // sort number
        double min = DIFF, prev = DIFF;
        // temp stack
        ItemStack tmp;
        Item item = stack.getItem();
        int next = -1;
        for (int i = 0; i < main.size(); i++) {
            tmp = main.get(i);
            // if min < prev
            if (tmp != stack && (min = Math.min(min, getSortNum(tmp, item))) < prev) {
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

    /**
     * in screen slot
     * <p>
     * mainHand = 36 + 0 ~ 36 + 9
     * offHand = 45
     *
     * @param slot         slot
     * @param selectedSlot selectedSlot
     * @return inScreenSlot
     */
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
        if (item == item2 || item.isFood() && item2.isFood()) {
            sortNum = 1;
        } else if (item2 instanceof ArmorItem && item instanceof ArmorItem && ((ArmorItem) item2).getSlotType() == ((ArmorItem) item).getSlotType()) {
            sortNum = 1;
        } else if (item2 instanceof ShieldItem && item instanceof ShieldItem) {
            sortNum = 1;
        } else if (item2 instanceof ToolItem) {
            if (item2.getClass() == item.getClass()) {
                sortNum = 2;
            } else if (item2.getClass().isInstance(item) || item.getClass() != Item.class && item.getClass().isInstance(item2)) {
                sortNum = 3;
            }
        } else if (item2 instanceof BlockItem && (item2.getGroup() == item.getGroup())) {
            if (((BlockItem) item2).getBlock() == ((BlockItem) item2).getBlock()) {
                sortNum = 2;
            } else {
                sortNum = 3;
            }
        }

        return sortNum + (itemStack.getMaxDamage() - itemStack.getDamage()) / 100000D;
    }

    public static void parserPacket(ClientPlayerEntity player, Byte status) {
        // consumeItem refill
        if (status == 9) {
            tryRefill(player, player.getStackInHand(player.getActiveHand()), player.getActiveHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            return;
        }

        // equipment refill
        EquipmentSlot slot = getEquipment(status);
        if (slot == null) {
            return;
        }

        tryRefill(player, player.getEquippedStack(slot), slot);
    }

    /**
     * 根据接收的状态码获取当且编码所代表的装备栏
     *
     * @param status 状态
     * @return 装备栏
     * @see LivingEntity#getEquipmentBreakStatus(net.minecraft.entity.EquipmentSlot)
     */
    private static EquipmentSlot getEquipment(Byte status) {
        return switch (status) {
            case 47 -> EquipmentSlot.MAINHAND;
            case 48 -> EquipmentSlot.OFFHAND;
            case 49 -> EquipmentSlot.HEAD;
            case 50 -> EquipmentSlot.CHEST;
            case 51 -> EquipmentSlot.LEGS;
            case 52 -> EquipmentSlot.FEET;
            default -> null;
        };
    }
}
