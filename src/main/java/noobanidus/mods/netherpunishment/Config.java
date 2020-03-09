package noobanidus.mods.netherpunishment;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
  public static final ServerConfig SERVER_CONFIG;
  public static final ForgeConfigSpec SERVER_CONFIG_SPEC;
  public static List<Effect> EFFECTS = null;

  public static int netherHeight = -1;
  public static int potionDuration = -1;
  public static int checkFrequency = -1;

  static {
    final Pair<ServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
    SERVER_CONFIG = pair.getLeft();
    SERVER_CONFIG_SPEC = pair.getRight();
  }

  public static class ServerConfig {
    public static ForgeConfigSpec.IntValue netherHeight;
    public static ForgeConfigSpec.IntValue potionDuration;
    public static ForgeConfigSpec.IntValue checkFrequency;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> potionEffects;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
      builder.push("general");
      netherHeight = builder.comment("The height that is considered \"above bedrock\" for the purposes of this mod.")
          .defineInRange("netherHeight", 127, 0, 256);
      potionDuration = builder.comment("The duration of the applied potion effects in ticks. This should be slightly longer than the checkFrequency value in order to handle short lag bursts.")
          .defineInRange("potionDuration", 3 * 20 + 10, 0, 10000);
      checkFrequency = builder.comment("How often the player list should be checked for players in the Nether who may potentially be above the defined `netherHeight`, and have the specified potion effects applied.")
          .defineInRange("checkFrequency", 3 * 20, 0, 10000);
      potionEffects = builder.comment("A list of potion effects that should be applied for a short duratiopn whenever a player is above `netherHeight`. In the format of modid:potionname.")
          .defineList("potionEffects", new ArrayList<>(Arrays.asList("minecraft:weakness", "minecraft:nausea", "minecraft:hunger", "minecraft:slowness")), (s) -> ((String) s).contains(":"));
      builder.pop();
    }
  }

  public static List<Effect> getEffects () {
    if (EFFECTS == null) {
      EFFECTS = new ArrayList<>();
      for (String s : ServerConfig.potionEffects.get()) {
        ResourceLocation rl = new ResourceLocation(s);
        Effect instance = ForgeRegistries.POTIONS.getValue(rl);
        if (instance == null) {
          NetherPunishment.LOG.error("Invalid specified potion name '" + s + "', does not exist in potion effect registry.");
        } else {
          EFFECTS.add(instance);
        }
      }
    }
    return EFFECTS;
  }

  public static void onReload(ModConfig.Reloading event) {
    reset(event.getConfig());
  }

  public static void onLoading (ModConfig.Loading event) {
    reset(event.getConfig());
  }

  private static void reset (ModConfig config) {
    if (config.getType() == ModConfig.Type.SERVER) {
      SERVER_CONFIG_SPEC.correct(config.getConfigData());
      EFFECTS = null;
      netherHeight = ServerConfig.netherHeight.get();
      checkFrequency = ServerConfig.checkFrequency.get();
      potionDuration = ServerConfig.potionDuration.get();
    }
  }
}
