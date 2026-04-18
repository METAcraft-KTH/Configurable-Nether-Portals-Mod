package net.totobirdcreations.confnportal.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.totobirdcreations.confnportal.Mod;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;


@Mixin(PortalShape.class)
public class NetherPortalMixin {

	@Unique
	private final ArrayList<BlockPos>   blocks    = new ArrayList<>();
	@Unique
	private       Direction.Axis        direction = Direction.Axis.X;
	@Unique
	private       @Nullable ServerLevel world     = null;

	@ModifyReturnValue(method = "findAnyShape", at = @At("RETURN"))
	private static PortalShape init(PortalShape original, BlockGetter view) {
		NetherPortalMixin self = (NetherPortalMixin)(Object) original;
		var pos = original.bottomLeft;
		self.world = view instanceof ServerLevel world$ ? world$ : null;
		if (self.portalsAllowCustomShapes()) {
			original.bottomLeft = null;
			original.width       = 0;
			original.height      = 0;
			self.blocks.clear();
			self.direction = Direction.Axis.X;
			if (! self.checkAxis(pos, 0)) {
				self.blocks.clear();
				self.direction = Direction.Axis.Z;
				if (! self.checkAxis(pos, 0)) {
					self.blocks.clear();
				}
			}
		}
		return original;
	}

	@Unique
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean checkAxis(BlockPos pos, int depth) {
		assert this.world != null;
		if (this.blocks.contains(pos)) {
			return true;
		}
		if (this.isValidFrameBlock(pos)) {
			return true;
		}
		if (depth >= this.portalsCustomSearchMaxDepth()) {
			return false;
		}
		BlockState state = this.world.getBlockState(pos);
		if (state.isAir() || state.is(BlockTags.FIRE)) {
			this.blocks.add(pos);
			for (Direction offset : Direction.values()) {
				if (offset.getAxis() == Direction.Axis.Y || offset.getClockWise().getAxis() != this.direction) {
					BlockPos next = pos.relative(offset, 1);
					if (! this.checkAxis(next, depth + 1)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
	private void isValid(CallbackInfoReturnable<Boolean> cir) {
		if (this.portalsAllowCustomShapes()) {
			int size = this.blocks.size();
			cir.setReturnValue(size > 0 && size >= this.portalsCustomShapeMinBlocks() && size <= this.portalsCustomShapeMaxBlocks());
		}
	}

	@Inject(method = "createPortalBlocks", at = @At("HEAD"), cancellable = true)
	private void createPortal(CallbackInfo ci) {
		if (this.portalsAllowCustomShapes()) {
			assert this.world != null;
			BlockState state = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.direction);
			for (BlockPos pos : this.blocks) {
				this.world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
			ci.cancel();
		}
	}

	@Inject(method = "lambda$static$0", at = @At("HEAD"), cancellable = true, remap = false)
	private static void isValidFrameBlock(BlockState state, BlockGetter view, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (view instanceof ServerLevel world && world.getGameRules().get(Mod.PORTALS_ALLOW_CUSTOM_SHAPES)) {
			cir.setReturnValue(isValidFrameBlock(state, view, pos));
		}
	}

	@Unique
	private static boolean isValidFrameBlock(BlockState state, BlockGetter view, BlockPos ignored) {
		return view instanceof ServerLevel world && (
				(world.getGameRules().get(Mod.PORTALS_ALLOW_CRYING_OBSIDIAN) && state.is(Blocks.CRYING_OBSIDIAN))
						|| (state.is(Blocks.OBSIDIAN))
		);
	}

	@Unique
	private boolean isValidFrameBlock(BlockPos pos) {
		return this.world != null && isValidFrameBlock(this.world.getBlockState(pos), this.world, pos);
	}

	@Unique
	private boolean portalsAllowCustomShapes() {
		return this.world != null && this.world.getGameRules().get(Mod.PORTALS_ALLOW_CUSTOM_SHAPES);
	}

	@Unique
	private int portalsCustomSearchMaxDepth() {
		return this.world != null ? this.world.getGameRules().get(Mod.PORTALS_CUSTOM_SEARCH_MAX_DEPTH) : -1;
	}

	@Unique
	private int portalsCustomShapeMinBlocks() {
		return this.world != null ? this.world.getGameRules().get(Mod.PORTALS_CUSTOM_SHAPE_MIN_BLOCKS) : -1;
	}

	@Unique
	private int portalsCustomShapeMaxBlocks() {
		return this.world != null ? this.world.getGameRules().get(Mod.PORTALS_CUSTOM_SHAPE_MAX_BLOCKS) : -1;
	}

}