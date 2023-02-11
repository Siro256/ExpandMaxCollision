package dev.siro256.forgemod.expandmaxcollision.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = World.class)
public abstract class MixinWorld {
    @Shadow public abstract List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb);

    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;)Z", at = @At("HEAD"))
    private void getCollisionBoxes(
            Entity entityIn,
            AxisAlignedBB aabb,
            boolean p_191504_3_,
            List<AxisAlignedBB> outList,
            CallbackInfoReturnable<Boolean> cir) {
        aabb = aabb.grow(5.0, 0.0, 5.0);
    }
}
