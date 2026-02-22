package moe.pxe.headCommand.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import moe.pxe.headCommand.HeadCommand;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class NoteBlockCommand {
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return Commands.literal("noteblock")
                .requires(sender -> sender.getSender().hasPermission("head.noteblock") || sender.getSender().isOp())
                .then(Commands.argument("key", ArgumentTypes.key())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getExecutor() instanceof final Player player)) {
                                ctx.getSource().getSender().sendRichMessage("<red><tr:permissions.requires.player>");
                                return 0;
                            }

                            final Key key = ctx.getArgument("key", Key.class);

                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (!(item.getType().equals(Material.PLAYER_HEAD))) {
                                ctx.getSource().getSender().sendRichMessage("<red>You must be holding a Player Head to use this command.");
                                return 0;
                            }

                            item.editMeta(SkullMeta.class, meta -> meta.setNoteBlockSound((NamespacedKey) key));
                            ctx.getSource().getSender().sendRichMessage("Set Note Block sound value to <aqua><key>", Placeholder.unparsed("key", key.asString()));
                            ctx.getSource().getSender().playSound(HeadCommand.MODIFY_SOUND);

                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(ctx -> {
                    if (!(ctx.getSource().getExecutor() instanceof final Player player)) {
                        ctx.getSource().getSender().sendRichMessage("<red><tr:permissions.requires.player>");
                        return 0;
                    }

                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (!(item.getType().equals(Material.PLAYER_HEAD))) {
                        ctx.getSource().getSender().sendRichMessage("<red>You must be holding a Player Head to use this command.");
                        return 0;
                    }

                    item.editMeta(SkullMeta.class, meta ->
                        meta.setNoteBlockSound(null)
                    );

                    ctx.getSource().getSender().sendRichMessage("Removed Note Block sound value from head");
                    ctx.getSource().getSender().playSound(HeadCommand.REMOVE_SOUND);

                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
