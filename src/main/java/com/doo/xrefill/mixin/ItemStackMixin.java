package com.doo.xrefill.mixin;

import com.doo.xrefill.util.RefillUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    @Final
    @Deprecated
    private Item item;

    private final Object o = this;

    private ClientPlayerEntity player = null;


    @Inject(method = "use", at = @At("HEAD"))
    private void useH(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> re) {
        if (user instanceof ClientPlayerEntity) {
            player = (ClientPlayerEntity) user;
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"))
    private void useOnBlockH(ItemUsageContext context, CallbackInfoReturnable<ActionResult> re) {
        if (context.getPlayer() instanceof ClientPlayerEntity) {
            player = (ClientPlayerEntity) context.getPlayer();
        }
    }

    @Inject(method = "useOnEntity", at = @At("HEAD"))
    private void useOnEntityH(PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> re) {
        if (user instanceof ClientPlayerEntity) {
            player = (ClientPlayerEntity) user;
        }
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void damageH(int amount, LivingEntity entity, Consumer<LivingEntity> breakCallback, CallbackInfo info) {
        if (entity instanceof ClientPlayerEntity) {
            player = (ClientPlayerEntity) entity;
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void finishUsingH(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> returnable) {
        if (user instanceof ClientPlayerEntity) {
            player = (ClientPlayerEntity) user;
        }
    }

    @Inject(method = "setCount", at = @At("TAIL"))
    private void setCountT(int count, CallbackInfo info) {
        if (count == 0 && player != null) {
            RefillUtil.refill(player, (ItemStack) o, item);
        }
        player = null;
    }
}
