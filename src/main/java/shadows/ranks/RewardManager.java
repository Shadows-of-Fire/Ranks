package shadows.ranks;

import java.util.Collections;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;

public class RewardManager {

	private static final Long2ObjectMap<String[]> commands = new Long2ObjectOpenHashMap<>();
	private static final LongList sorted = new LongArrayList();

	public static void executeRewardsFor(long ticks, EntityPlayer player) {
		String[] cmd = commands.get(ticks);
		if (cmd != null) {
			MinecraftServer svr = player.world.getMinecraftServer();
			for (String s : cmd)
				svr.commandManager.executeCommand(svr, s.replace("<player>", player.getName()));
		}
	}

	public static void load(Configuration cfg) {
		String[] unparsed = cfg.getStringList("Rewards", "rewards", new String[] { "4000|/give <player> diamond|/kill <player>" }, "A list of reward entries.  Each entry uses the following format: Time|Command1|Command2...etc.  Each new | will be treated as a new separate command.  The string <player> will be replaced with the rewarded player's name.");
		for (String s : unparsed) {
			try {
				String[] split = s.split("\\|");
				long ticks = Long.parseLong(split[0]);
				String[] cmds = new String[split.length - 1];
				System.arraycopy(split, 1, cmds, 0, cmds.length);
				commands.put(ticks, cmds);
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				Ranks.LOGGER.error("Invalid reward entry {} will be ignored.", s);
				e.printStackTrace();
			}
		}
		if (cfg.hasChanged()) cfg.save();
		sorted.addAll(commands.keySet());
		Collections.sort(sorted);
	}

	public static LongList getTimeValues() {
		return sorted;
	}

}
