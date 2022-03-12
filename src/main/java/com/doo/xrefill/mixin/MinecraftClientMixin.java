package com.doo.xrefill.mixin;

import com.doo.xrefill.util.RefillUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public abstract class MinecraftClientMixin {

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    private ItemStack usedItem = null;
    private EquipmentSlot usedHand = null;

    @ModifyVariable(method = "doItemUse", at = @At("STORE"))
    private ItemStack onEntityStatusT(ItemStack stack) {
        if (player == null) {
            return stack;
        }

        usedItem = stack.copy();
        usedHand = player.getMainHandStack() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        return stack;
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult;shouldSwingHand()Z", ordinal = 1))
    private void onEntityStatusT(CallbackInfo ci) {
        ItemStack stack = player.getEquippedStack(usedHand);
        if (stack.isEmpty()) {
            RefillUtil.tryRefill(player, usedItem, usedHand);
            usedItem = null;
            usedHand = null;
        }
    }
}
