package shadows.ranks;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandActive extends CommandBase {

	public static final int DAY_TO_TICK = 20 * 60 * 60 * 24;
	public static final int HOUR_TO_TICK = DAY_TO_TICK / 24;
	public static final int MINUTE_TO_TICK = HOUR_TO_TICK / 60;
	public static final int SECOND_TO_TICK = MINUTE_TO_TICK / 60;

	@Override
	public String getName() {
		return "ranktime";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/ranktime - shows total login time.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			long time = Ranks.TIMES.getLong(((EntityPlayer) sender).getUniqueID());
			sender.sendMessage(new TextComponentString(ticksToTime(time)));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	public static String ticksToTime(long time) {
		long days = time / DAY_TO_TICK;
		time -= days * DAY_TO_TICK;
		long hours = time / HOUR_TO_TICK;
		time -= hours * HOUR_TO_TICK;
		long minutes = time / MINUTE_TO_TICK;
		time -= minutes * MINUTE_TO_TICK;
		long seconds = time / SECOND_TO_TICK;
		return String.format("Time Played: %s days, %s hours, %s minutes, and %s seconds.", days, hours, minutes, seconds);
	}

}
