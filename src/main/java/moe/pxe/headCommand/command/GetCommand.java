package moe.pxe.headCommand.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import moe.pxe.headCommand.HeadCommand;
import org.bukkit.entity.Player;

import java.util.Collection;

public class GetCommand {

    private static int logic(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof final Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red><tr:permissions.requires.player>");
            return 0;
        }

        int amount = 1;
        try {
            amount = IntegerArgumentType.getInteger(ctx, "amount");
        } catch (IllegalArgumentException ignored) {}
        int finalAmount = amount;

        final PlayerProfileListResolver profilesResolver = ctx.getArgument("profile", PlayerProfileListResolver.class);

        new Thread(() -> {
            Collection<PlayerProfile> profiles;
            try {
                profiles = profilesResolver.resolve(ctx.getSource());
            } catch (CommandSyntaxException e) {
                ctx.getSource().getSender().sendRichMessage("<red>Failed to resolve profile. Did you type the username correctly?");
                return;
            }
            for (PlayerProfile profile : profiles) {
                HeadCommand.giveHead(ctx.getSource().getSender(), player, profile, finalAmount);
            }
        }).start();

        return Command.SINGLE_SUCCESS;
    }

    public static RequiredArgumentBuilder<CommandSourceStack, PlayerProfileListResolver> getArgument() {
        return Commands.argument("profile", ArgumentTypes.playerProfiles())
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(GetCommand::logic))
            .executes(GetCommand::logic);
    }

    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return Commands.literal("get")
                .then(getArgument())
                .executes(ctx -> {
                    if (!(ctx.getSource().getExecutor() instanceof final Player player)) {
                        ctx.getSource().getSender().sendRichMessage("<red><tr:permissions.requires.player>");
                        return 0;
                    }
                    HeadCommand.giveHead(ctx.getSource().getSender(), player, player.getPlayerProfile(), 1);
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
