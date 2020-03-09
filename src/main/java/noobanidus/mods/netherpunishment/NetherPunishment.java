package noobanidus.mods.netherpunishment;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("netherpunishment")
@SuppressWarnings("WeakerAccess")
public class NetherPunishment {
  public static final String MODID = "netherpunishment";

  public final static Logger LOG = LogManager.getLogger(MODID);

  public NetherPunishment() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG_SPEC);
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(Config::onReload);
    bus.addListener(Config::onLoading);
    MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
  }

  public void onServerTick (TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server.getTickCounter() % Config.checkFrequency == 0) {
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
          if (player.isCreative() || player.isSpectator()) {
            continue;
          }
          if (player.dimension == DimensionType.THE_NETHER) {
            if (player.getPosition().getY() > Config.netherHeight) {
              for (Effect effect : Config.getEffects()) {
                player.addPotionEffect(new EffectInstance(effect, Config.potionDuration, 0, false, false, true));
              }
            }
          }
        }
      }
    }
  }
}
