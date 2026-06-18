package dev.armand.monarch_wings;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = MonarchWings.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue COOLDOWN_SECONDS = BUILDER
            .comment("How many seconds of cooldown to have between wing flaps. -1 for only one boost per jump")
            .defineInRange("cooldown_seconds", -1, -1, Double.MAX_VALUE);

    // a list of strings that are treated as resource locations for items
    private static final ModConfigSpec.ConfigValue<List<? extends String>> FLYING_DIMENSIONS = BUILDER
            .comment("List of dimensions to disable Double Jump behaviour in, to have default flying")
            .defineList("flying_dimensions", List.of("minecraft:end"), () -> "", Config::validateItemName);

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName; // && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    public static Set<String> flyingDimensions;
    public static int cooldownTicks;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        double cooldownSeconds = COOLDOWN_SECONDS.get();
        if (cooldownSeconds == -1) {
            cooldownTicks = Integer.MAX_VALUE;
        } else {
            cooldownTicks = (int) Math.max(cooldownSeconds / 20, DoubleJump.LAUNCH_DELAY_TICKS);
        }

        flyingDimensions = new HashSet<>(FLYING_DIMENSIONS.get());
    }
}
