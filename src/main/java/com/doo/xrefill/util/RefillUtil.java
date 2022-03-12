package com.doo.xrefill.util;

import com.doo.xrefill.Refill;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.EquipmentSlot;
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

    private static final int DIFF = 20;

    private static final int ARMOR_IDX = 5;
    private static final int MAIN_HAND_IDX = 36;
    private static final int OFF_HAND_IDX = 45;

    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor();

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
                EXEC.schedule(() -> manager.clickSlot(0, next, 0, SlotActionType.PICKUP, player), Refill.option.delay + 20, TimeUnit.MILLISECONDS);
            }, Refill.option.delay, TimeUnit.MILLISECONDS);
        }, player, stack, slot);
    }

    private static void ifRefill(BiConsumer<Integer, Integer> refillSetter, PlayerEntity player, ItemStack stack, EquipmentSlot slot) {
        int current = getEquipmentSlotInScreen(slot, player.getInventory().selectedSlot);
//        int current = getEquipmentSlotInScreen(slot, player.inventory.selectedSlot);
        if (current == -1) {
            return;
        }

        DefaultedList<ItemStack> main = player.getInventory().main;
//        DefaultedList<ItemStack> main = player.inventory.main;
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
            sortNum = ((BlockItem) item2).getBlock() == ((BlockItem) item2).getBlock() ? 2 : 3;
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

    private static EquipmentSlot getEquipment(Byte status) {
        // see LivingEntity#getEquipmentBreakStatus(net.minecraft.entity.EquipmentSlot)
        switch (status) {
            case 47:
                return EquipmentSlot.MAINHAND;
            case 48:
                return EquipmentSlot.OFFHAND;
            case 49:
                return EquipmentSlot.HEAD;
            case 50:
                return EquipmentSlot.CHEST;
            case 51:
                return EquipmentSlot.LEGS;
            case 52:
                return EquipmentSlot.FEET;
            default:
                return null;
        }
    }
}
