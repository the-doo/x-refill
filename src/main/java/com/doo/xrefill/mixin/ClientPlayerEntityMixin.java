package com.doo.xrefill.mixin;

import com.doo.xrefill.util.RefillUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
@Environment(EnvType.CLIENT)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void onEntityStatusT(byte status, CallbackInfo ci) {
        RefillUtil.parserPacket((ClientPlayerEntity) (Object) this, status);
    }
}
