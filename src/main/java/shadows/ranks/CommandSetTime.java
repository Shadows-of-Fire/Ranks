package shadows.ranks;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandSetTime extends CommandBase {

	@Override
	public String getName() {
		return "setranktime";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/setranktime <player> <time> - sets the time for that player to the specified value.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer player = getPlayer(server, sender, args[0]);
		long time = Long.parseLong(args[1]);
		Ranks.TIMES.put(player.getUniqueID(), time);
		sender.sendMessage(new TextComponentString("Set the rank time for player " + player.getName() + " to " + time));
	}

}
