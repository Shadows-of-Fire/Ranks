package shadows.ranks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@Mod(modid = Ranks.MODID, name = Ranks.MODNAME, version = Ranks.VERSION, acceptableRemoteVersions = "*")
public class Ranks {

	public static final String MODID = "ranks";
	public static final String MODNAME = "Ranks";
	public static final String VERSION = "1.0.2";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	static Object fileLock = new Object();
	static Object2LongMap<UUID> TIMES = new Object2LongOpenHashMap<>();
	static File propFile;
	static long ticks = 0;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) throws Exception {
		MinecraftForge.EVENT_BUS.register(this);
		propFile = new File(e.getSuggestedConfigurationFile().getParentFile(), "ranks.properties");
		if (!propFile.exists()) {
			propFile.createNewFile();
		}
		RewardManager.load(new Configuration(e.getSuggestedConfigurationFile()));
	}

	@EventHandler
	public void starting(FMLServerStartingEvent e) {
		deserializeTimes();
		e.registerServerCommand(new CommandActive());
		e.registerServerCommand(new CommandSetTime());
		e.registerServerCommand(new CommandNextRank());
	}

	@EventHandler
	public void stopping(FMLServerStoppedEvent e) {
		serializeTimes();
	}

	@SubscribeEvent
	public void tick(PlayerTickEvent e) {
		if (e.phase == Phase.END) {
			long ticks;
			TIMES.put(e.player.getUniqueID(), ticks = (TIMES.getLong(e.player.getUniqueID()) + 1));
			RewardManager.executeRewardsFor(ticks, e.player);
		}
	}

	@SubscribeEvent
	public void tick(WorldTickEvent e) {
		if (e.phase == Phase.END) {
			if (++ticks % 24000 == 0) {
				new Thread(Ranks::serializeAndBackup, "Ranks serialization thread").start();
			}
		}
	}

	private static void serializeTimes() {
		synchronized (fileLock) {
			Properties props = new Properties();
			for (Object2LongMap.Entry<UUID> e : TIMES.object2LongEntrySet()) {
				props.setProperty(e.getKey().toString(), String.valueOf(e.getLongValue()));
			}
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(propFile))) {
				props.store(writer, "Ranks user logged in time.");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static void backup() {
		synchronized (fileLock) {
			File backupDir = new File(propFile.getParentFile(), "rank_backups");
			if (!backupDir.exists()) backupDir.mkdir();
			File out = new File(backupDir, String.format("%s-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS.properties", "ranks", System.currentTimeMillis()));
			try {
				Files.copy(propFile, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void serializeAndBackup() {
		serializeTimes();
		backup();
	}

	private static void deserializeTimes() {
		synchronized (fileLock) {
			Properties props = new Properties();
			try (BufferedReader reader = new BufferedReader(new FileReader(propFile))) {
				props.load(reader);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			TIMES.clear();
			for (Map.Entry<Object, Object> e : props.entrySet()) {
				UUID id = UUID.fromString((String) e.getKey());
				long ticks = Long.parseLong((String) e.getValue());
				TIMES.put(id, ticks);
			}
		}
	}

}
