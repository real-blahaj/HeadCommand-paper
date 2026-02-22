package moe.pxe.headCommand.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import moe.pxe.headCommand.HeadCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

public class TextureCommand {

    private static boolean setTextureToProfile(CommandSender sender, ItemStack item, @Nullable PlayerProfile newProfile) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        PlayerProfile profile = newProfile;
        if (newProfile == null) {
            profile = meta.getPlayerProfile();
            if (profile == null) {
                sender.sendRichMessage("<red>No profile given.");
                return false;
            }
            profile.clearProperties();
        }

        PlayerProfile itemProfile = meta.getPlayerProfile();
        if (itemProfile == null) itemProfile = Bukkit.createProfile(UUID.randomUUID());

        if (!profile.hasTextures()) sender.sendRichMessage("<gray><i>Attempting to resolve skin, this may take some time...");
        if (!profile.complete()) {
            sender.sendRichMessage("<red>Resolving skin failed. :(");
            return false;
        }

        itemProfile.setProperties(profile.getProperties());
        meta.setPlayerProfile(itemProfile);
        item.setItemMeta(meta);
        sender.playSound(HeadCommand.MODIFY_SOUND);
        return true;
    }

    private static int setTextureToData(CommandContext<CommandSourceStack> ctx, String data) {
        if (!(ctx.getSource().getExecutor() instanceof final Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red><tr:permissions.requires.player>");
            return 0;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!(item.getType().equals(Material.PLAYER_HEAD))) {
            ctx.getSource().getSender().sendRichMessage("<red>You must be holding a Player Head to use this command.");
            return 0;
        }

        item.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = meta.getPlayerProfile();
            if (profile == null) profile = Bukkit.createProfile(UUID.randomUUID());

            if (data != null) profile.setProperty(new ProfileProperty("textures", data));
            else profile.clearProperties();
            profile.complete(false);
            meta.setPlayerProfile(profile);
        });

        return Command.SINGLE_SUCCESS;
    }

    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return Commands.literal("texture")
                .requires(sender -> sender.getSender().hasPermission("head.texture") || sender.getSender().isOp())
                .then(Commands.literal("profile")
                        .then(Commands.argument("profile", ArgumentTypes.playerProfiles())
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

                                    final PlayerProfileListResolver profilesResolver = ctx.getArgument("profile", PlayerProfileListResolver.class);

                                    new Thread(() -> {
                                        Collection<PlayerProfile> profiles;
                                        try {
                                            profiles = profilesResolver.resolve(ctx.getSource());
                                        } catch (CommandSyntaxException e) {
                                            ctx.getSource().getSender().sendRichMessage("<red>Failed to resolve profile. Did you type the username correctly?");
                                            return;
                                        }
                                        PlayerProfile profile = (PlayerProfile) profiles.toArray()[0];

                                        if (setTextureToProfile(ctx.getSource().getSender(), item, profile)) {
                                            ctx.getSource().getSender().sendRichMessage("Set texture to <profile>'s skin", Placeholder.unparsed("profile", profile.getName() != null ? profile.getName() : "player"));
                                            ctx.getSource().getSender().playSound(HeadCommand.MODIFY_SOUND);
                                        }
                                    }).start();

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
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

                            new Thread(() -> {
                                if (setTextureToProfile(ctx.getSource().getSender(), item, null)) {
                                    ctx.getSource().getSender().sendRichMessage("Set head texture to default");
                                    ctx.getSource().getSender().playSound(HeadCommand.MODIFY_SOUND);
                                }
                            }).start();
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("encode")
                        .then(Commands.argument("data", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String toEncode = ctx.getArgument("data", String.class);
                                    String data = Base64.getEncoder().encodeToString(toEncode.getBytes());
                                    int returnValue = setTextureToData(ctx, data);
                                    if (returnValue >= 1) {
                                        ctx.getSource().getSender().sendRichMessage("Set head texture to <data>", Placeholder.component("data",
                                                Component.text(data.length() > 50 ? data.substring(0, 50) + "..." : data).hoverEvent(Component.text(data)
                                                        .append(Component.text("\n\nEncoded: " + toEncode).color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
                                                )
                                        ));
                                        ctx.getSource().getSender().playSound(HeadCommand.MODIFY_SOUND);
                                    }
                                    return returnValue;
                                })
                        ))
                .then(Commands.argument("data", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String data = ctx.getArgument("data", String.class);
                            int returnValue = setTextureToData(ctx, data);
                            if (returnValue >= 1) {
                                ctx.getSource().getSender().sendRichMessage("Set head texture to <data>", Placeholder.component("data",
                                        Component.text(data.length() > 50 ? data.substring(0, 50) + "..." : data).hoverEvent(Component.text(data))
                                ));
                                ctx.getSource().getSender().playSound(HeadCommand.MODIFY_SOUND);
                            }
                            return returnValue;
                        })
                )
                .executes(ctx -> {
                    int returnValue = setTextureToData(ctx, null);
                    if (returnValue >= 1) {
                        ctx.getSource().getSender().sendRichMessage("Removed texture from Player Head<newline><gray><i>Wanted to reset the texture to default? Run <white><u><click:run_command:/head texture profile>/head texture profile</click></u></white> to do so.");
                        ctx.getSource().getSender().playSound(HeadCommand.REMOVE_SOUND);
                    }
                    return returnValue;
                })
                .build();
    }
}
