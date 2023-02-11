package dev.siro256.forgemod.expandmaxcollision.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = World.class)
public abstract class MixinWorld {
    @Redirect(
            method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;)Z")
    )
    private boolean getCollisionBoxes(
            World instance,
            Entity entityIn,
            AxisAlignedBB aabb,
            boolean boolIn,
            List<AxisAlignedBB> list
    ) {
        Vec3i min = new Vec3i(
                MathHelper.floor(aabb.minX) - 3,
                MathHelper.floor(aabb.minY) - 1,
                MathHelper.floor(aabb.minZ) - 3
        );
        Vec3i max = new Vec3i(
                MathHelper.ceil(aabb.maxX) + 3,
                MathHelper.ceil(aabb.maxY) + 1,
                MathHelper.ceil(aabb.maxZ) + 3
        );

        WorldBorder border = instance.getWorldBorder();
        boolean isOutsideBorder = entityIn != null && entityIn.isOutsideBorder();
        boolean isInsideWorldBorder = entityIn != null && instance.isInsideWorldBorder(entityIn);
        IBlockState defaultState = Blocks.STONE.getDefaultState();
        BlockPos.PooledMutableBlockPos mutablePos = BlockPos.PooledMutableBlockPos.retain();

        try {
            int x, y, z;
            boolean xIsEdge; boolean zIsEdge;
            IBlockState state;

            for (x = min.getX(); x < max.getX(); ++x) {
                for (z = min.getZ(); z < max.getZ(); ++z) {
                    xIsEdge = x == min.getX() || x == max.getX() -1;
                    zIsEdge = z == min.getZ() || z == max.getZ() - 1;

                    if (!((!xIsEdge || !zIsEdge) && instance.isBlockLoaded(mutablePos.setPos(x, 64, z)))) continue;

                    for (y = min.getY(); y < max.getY(); ++y) {
                        if (!(!xIsEdge && !zIsEdge || y != max.getY() - 1)) continue;
                        if (boolIn) {
                            if (isOutsideBorder(x, z)) return true;
                        } else if (entityIn != null && isOutsideBorder == isInsideWorldBorder) {
                            entityIn.setOutsideBorder(!isInsideWorldBorder);
                        }

                        mutablePos.setPos(x, y, z);

                        if (!boolIn && !border.contains(mutablePos) && isInsideWorldBorder) {
                            state = defaultState;
                        } else {
                            state = instance.getBlockState(mutablePos);
                        }

                        state.addCollisionBoxToList(instance, mutablePos, aabb, list, entityIn, false);

                        if (boolIn && !list.isEmpty()) return true;
                    }
                }
            }
        } finally {
            mutablePos.release();
        }

        return !list.isEmpty();
    }

    private static boolean isOutsideBorder(int x, int z) {
        return x < -30_000_000 || x >= 30_000_000 || z < -30_000_000 || z >= 30_000_000;
    }
}
