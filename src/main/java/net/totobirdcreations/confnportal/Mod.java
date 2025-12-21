package net.totobirdcreations.confnportal;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;


public class Mod implements ModInitializer {

    public static final GameRule<Boolean> PORTALS_ALLOW_CRYING_OBSIDIAN   = registerBoolean("allow_crying_obsidian", GameRuleCategory.MISC, true);
    public static final GameRule<Boolean> PORTALS_ALLOW_CUSTOM_SHAPES     = registerBoolean("allow_custom_shapes", GameRuleCategory.MISC, true);
    public static final GameRule<Integer> PORTALS_CUSTOM_SEARCH_MAX_DEPTH = registerInt("custom_search_max_depth", GameRuleCategory.MISC, 512, 0);
    public static final GameRule<Integer> PORTALS_CUSTOM_SHAPE_MIN_BLOCKS = registerInt("custom_shape_min_blocks", GameRuleCategory.MISC, 1, 1);
    public static final GameRule<Integer> PORTALS_CUSTOM_SHAPE_MAX_BLOCKS = registerInt("custom_shape_max_blocks", GameRuleCategory.MISC, 512, 1);

    @Override
    public void onInitialize() {
    }

	private static GameRule<Integer> registerInt(String name, GameRuleCategory category, int defaultValue, int minValue) {
		return registerInt(name, category, defaultValue, minValue, Integer.MAX_VALUE, FeatureFlagSet.of());
	}

	private static GameRule<Integer> registerInt(String name, GameRuleCategory category, int defaultValue, int minValue, int maxValue, FeatureFlagSet requiredFeatures) {
		return register(
				name, category, GameRuleType.INT, IntegerArgumentType.integer(minValue, maxValue),
				Codec.intRange(minValue, maxValue), defaultValue, requiredFeatures,
				GameRuleTypeVisitor::visitInteger, (value) -> value
		);
	}

	private static GameRule<@NotNull Boolean> registerBoolean(String id, GameRuleCategory gameRuleCategory, boolean defaultValue) {
		return register(
				id, gameRuleCategory, GameRuleType.BOOL, BoolArgumentType.bool(),
				Codec.BOOL, defaultValue, FeatureFlagSet.of(), GameRuleTypeVisitor::visitBoolean,
				(b) -> b ? 1 : 0
		);
	}

	private static <T> GameRule<@NotNull T> register(
			String id, GameRuleCategory gameRuleCategory,
			GameRuleType gameRuleType, ArgumentType<T> argumentType,
			Codec<T> codec, T object, FeatureFlagSet featureFlagSet,
			GameRules.VisitorCaller<@NotNull T> visitorCaller, ToIntFunction<T> toIntFunction
	) {
		return Registry.register(
				BuiltInRegistries.GAME_RULE, Identifier.fromNamespaceAndPath("confnportal", id),
				new GameRule<>(
						gameRuleCategory, gameRuleType, argumentType, visitorCaller,
						codec, toIntFunction, object, featureFlagSet
				)
		);
	}

}
