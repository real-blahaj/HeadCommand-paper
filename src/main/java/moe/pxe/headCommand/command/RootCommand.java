package moe.pxe.headCommand.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import moe.pxe.headCommand.HeadCommand;
import org.bukkit.entity.Player;

public class RootCommand {

    public static LiteralCommandNode<CommandSourceStack> getCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(sender -> (sender.getSender().hasPermission("head.use") || sender.getSender().isOp()))
                .then(GetCommand.getCommand())
                .then(NoteBlockCommand.getCommand())
                .then(TextureCommand.getCommand())
                .executes(ctx -> {
                    if (!(ctx.getSource().getExecutor() instanceof final Player player)) {
                        ctx.getSource().getSender().sendRichMessage("<red><tr:permissions.requires.player>");
                        return 0;
                    }
                    HeadCommand.giveHead(ctx.getSource().getSender(), player, player.getPlayerProfile(), 1);
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
