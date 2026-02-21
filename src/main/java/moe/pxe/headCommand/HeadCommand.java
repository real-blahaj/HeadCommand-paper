package moe.pxe.headCommand;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import moe.pxe.headCommand.command.RootCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Set;

public final class HeadCommand extends JavaPlugin {

    public static void giveHead(CommandSender sender, Player player, PlayerProfile profile, int amount) {
        new Thread(() -> {
            if (!profile.hasTextures()) sender.sendRichMessage("<gray><i>Attempting to resolve skin, this may take some time...");
            if (!profile.complete()) sender.sendRichMessage("<red>Resolving skin failed. :(");
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            item.setAmount(amount);
            item.editMeta(SkullMeta.class, meta -> {
                meta.setPlayerProfile(profile);
                meta.setCanPlaceOn(Collections.singleton(Material.RED_CONCRETE));
            });
            player.give(item);
            sender.sendMessage(Component.translatable("commands.give.success.single").arguments(
                    Component.text(amount),
                    item.displayName(),
                    player.displayName()
            ));
        }).start();
    }

    @Override
    public void onEnable() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(RootCommand.getCommand("head"), "Spawns a player head", Set.of("skull"));
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
