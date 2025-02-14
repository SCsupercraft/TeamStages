package net.scsupercraft.teamstages;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.scsupercraft.teamstages.command.TeamStageCommands;
import net.scsupercraft.teamstages.config.CommonConfig;
import net.scsupercraft.teamstages.ftbquests.FtbQuestsIntegration;
import net.scsupercraft.teamstages.listeners.FtbTeamsEventListener;
import net.scsupercraft.teamstages.listeners.ServerEventListener;
import net.scsupercraft.teamstages.packet.TeamStagesPacketHandler;
import org.slf4j.Logger;

@Mod(TeamStages.MOD_ID)
@Mod.EventBusSubscriber
public class TeamStages {
    public static final String MOD_ID = "teamstages";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer server;

    public TeamStages() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new ServerEventListener());

        FtbQuestsIntegration.init();

        TeamStagesPacketHandler.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        FtbTeamsEventListener.listen();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TeamStageCommands.register(event.getDispatcher());
    }
}
