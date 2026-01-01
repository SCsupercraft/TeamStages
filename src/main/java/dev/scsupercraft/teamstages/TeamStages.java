package dev.scsupercraft.teamstages;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import dev.scsupercraft.teamstages.command.TeamStageCommands;
import dev.scsupercraft.teamstages.config.CommonConfig;
import dev.scsupercraft.teamstages.data.TeamStageSaveHandler;
import dev.scsupercraft.teamstages.ftbquests.FtbQuestsIntegration;
import dev.scsupercraft.teamstages.listener.ServerEventListener;
import dev.scsupercraft.teamstages.packet.TeamStagesPacketHandler;
import org.slf4j.Logger;

@Mod(TeamStages.MOD_ID)
@Mod.EventBusSubscriber
public class TeamStages {
    public static final String MOD_ID = "teamstages";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer server;

    public TeamStages(FMLJavaModLoadingContext ctx) throws ClassNotFoundException {
        ctx.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(new ServerEventListener());

        TeamStageSaveHandler.init();
        FtbQuestsIntegration.init();
        TeamStagesPacketHandler.init();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TeamStageCommands.register(event.getDispatcher());
    }
}
