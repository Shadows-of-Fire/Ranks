package shadows.ranks;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandNextRank extends CommandBase {

	@Override
	public String getName() {
		return "untilnextrank";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/untilnextrank - shows time needed until the next rank milestone is hit.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			long time = Ranks.TIMES.getLong(((EntityPlayer) sender).getUniqueID());
			for (long reward : RewardManager.getTimeValues()) {
				if (reward > time) {
					sender.sendMessage(new TextComponentString("Your next reward is in " + CommandActive.ticksToTime(reward - time)));
					return;
				}
			}
			sender.sendMessage(new TextComponentString("You have gained all available rewards."));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

}
