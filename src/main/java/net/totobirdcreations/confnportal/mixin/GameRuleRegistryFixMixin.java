package net.totobirdcreations.confnportal.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.GameRuleRegistryFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.throwables.MixinError;

@Mixin(GameRuleRegistryFix.class)
public abstract class GameRuleRegistryFixMixin {

	@Shadow
	private static Dynamic<?> convertBoolean(Dynamic<?> dynamic) {
		throw new MixinError("Not applied");
	}

	@Shadow
	private static Dynamic<?> convertInteger(Dynamic<?> dynamic) {
		throw new MixinError("Not applied");
	}

	@Shadow
	private static Dynamic<?> convertInteger(Dynamic<?> dynamic, int min) {
		throw new MixinError("Not applied");
	}

	@ModifyReturnValue(
			method = "lambda$makeRule$9", // Lambda inside addRules,
			at = @At("RETURN")
	)
	private static Dynamic<?> addRules(Dynamic<?> original) {
		return original.renameAndFixField(
				"portalsAllowCryingObsidian", "confnportal:allow_crying_obsidian",
				GameRuleRegistryFixMixin::convertBoolean
		).renameAndFixField(
				"portalsAllowCustomShapes", "confnportal:allow_custom_shapes",
				GameRuleRegistryFixMixin::convertBoolean
		).renameAndFixField(
				"portalsCustomSearchMaxDepth", "confnportal:custom_search_max_depth",
				GameRuleRegistryFixMixin::convertInteger
		).renameAndFixField(
				"portalsCustomShapeMinBlocks", "confnportal:custom_shape_min_blocks",
				v -> convertInteger(v, 1)
		).renameAndFixField(
				"portalsCustomShapeMaxBlocks", "confnportal:custom_shape_max_blocks",
				v -> convertInteger(v, 1)
		);
	}

}
