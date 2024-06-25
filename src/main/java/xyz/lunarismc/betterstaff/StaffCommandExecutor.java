package xyz.lunarismc.betterstaff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kyori.adventure.text.Component.*;

public class StaffCommandExecutor implements CommandExecutor, TabExecutor, Listener {

    private final BetterStaff plugin;

    private final List<String> staffTabCompletions = List.of("on", "off");

    private final List<String> betterStaffTabCompletions = List.of("reload", "version");

    public StaffCommandExecutor(BetterStaff plugin) {
        this.plugin = plugin;
    }

    public Component getParsedMessage(String messageConfig) {
        return this.plugin.miniMessage.deserialize(Objects.requireNonNull(this.plugin.getMessageFile().getString(messageConfig)));
    }

    public GameMode getGameMode(String gameModeConfig) {
        return GameMode.valueOf(Objects.requireNonNull(this.plugin.getConfig().getString("staff." + gameModeConfig + ".gamemode")).toUpperCase());
    }

    public boolean getGodMode(String godConfig) {
        return this.plugin.getConfig().getBoolean("staff." + godConfig + ".godmode");
    }

    public void setStaffMode(Player player, boolean isOnDuty) {
        if (isOnDuty) {
            if (!this.plugin.playerinTeam("onDuty", player)) {
                Objects.requireNonNull(this.plugin.getScoreboard().getTeam("onDuty")).addPlayer(player);
            }

            this.plugin.onDutyPlayers.add(player);

            player.setGameMode(getGameMode("staff-on"));
            player.setInvulnerable(getGodMode("staff-on"));

            @NotNull Component playerDisplayName = this.plugin.miniMessage.deserialize(Objects.requireNonNull(this.plugin.getMessageFile().getString("staff-name")), Placeholder.component("playername", Component.text(player.getName())));

            player.displayName(playerDisplayName);
            player.playerListName(playerDisplayName);

            this.plugin.luckPerms.getContextManager().signalContextUpdate(player);
        }
        else {
            Objects.requireNonNull(this.plugin.getScoreboard().getTeam("onDuty")).removePlayer(player);

            this.plugin.onDutyPlayers.remove(player);

            player.setGameMode(getGameMode("staff-off"));
            player.setInvulnerable(getGodMode("staff-off"));

            player.displayName(text(player.getName()));

            this.plugin.luckPerms.getContextManager().signalContextUpdate(player);
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (this.plugin.playerinTeam("onDuty", player)) {
            this.setStaffMode(player, true);

            player.sendMessage(getParsedMessage("staff-on"));
        }
        else {
            this.setStaffMode(player, false);
            this.plugin.getLogger().info("0");
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        if (player.isInvulnerable()) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("staff")) {
            if (!(commandSender instanceof Player sender)) {
                commandSender.sendMessage(getParsedMessage("no-console"));
            }
            else {
                if (!sender.hasPermission("betterstaff.staff")) {
                    sender.sendMessage(getParsedMessage("no-perms"));
                }
                else {
                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("on")) {
                            this.setStaffMode(sender, true);

                            sender.sendMessage(getParsedMessage("staff-on"));
                        }
                        else if (args[0].equalsIgnoreCase("off")) {
                            this.setStaffMode(sender, false);

                            sender.sendMessage(getParsedMessage("staff-off"));
                        }
                    }
                    else if (args.length == 0) {
                        setStaffMode(sender, !this.plugin.playerinTeam("onDuty", sender));
                        if (!this.plugin.playerinTeam("onDuty", sender)) {
                            sender.sendMessage(getParsedMessage("staff-off"));
                        }
                        else {
                            sender.sendMessage(getParsedMessage("staff-on"));
                        }
                    }
                    else {
                        sender.sendMessage(getParsedMessage("invalid-args"));
                    }
                }
            }

            return true;
        }
        else if(command.getName().equalsIgnoreCase("stafflist")) {
            if (!commandSender.hasPermission("betterstaff.stafflist")) {
                commandSender.sendMessage(getParsedMessage("no-perms"));
            }
            else {
                List<String> groupsConfig = this.plugin.getConfig().getStringList("staff-list.groups");
                if (groupsConfig.isEmpty()) {
                    commandSender.sendMessage(getParsedMessage("no-groups"));
                }
                else {
                    StringBuilder staffListMessage = new StringBuilder("<gold>Online Staff members:\n</gold>");

                    HashSet<Player> addedPlayers = new HashSet<>();

                    for (String groupName : groupsConfig) {
                        Group group = this.plugin.luckPerms.getGroupManager().getGroup(groupName);
                        if (group != null) {
                            String groupDisplayName;
                            if (group.getDisplayName() == null) {
                                groupDisplayName = group.getName();
                            }
                            else {
                                groupDisplayName = group.getDisplayName();
                            }

                            StringJoiner playerinGroup = new StringJoiner(", ");

                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (!addedPlayers.contains(player)) {
                                    if (player.hasPermission("group." + group)) {
                                        playerinGroup.add(this.plugin.miniMessage.serialize(player.displayName()));
                                        addedPlayers.add(player);
                                    }
                                }
                            }
                            if (playerinGroup.length() != 0) {
                                staffListMessage.append(groupDisplayName).append(": ").append(playerinGroup).append("\n");
                            }
                        }
                    }

                    commandSender.sendMessage(this.plugin.miniMessage.deserialize(staffListMessage.toString()));
                }
            }

            return true;
        }
        else if (command.getName().equalsIgnoreCase("betterstaff")) {
            if (!commandSender.hasPermission("betterstaff.betterstaff")) {
                commandSender.sendMessage(getParsedMessage("no-perms"));
            }
            else {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        if (!commandSender.hasPermission("betterstaff.betterstaff.reload")) {
                            commandSender.sendMessage(getParsedMessage("no-perms"));
                        } else {
                            this.plugin.reloadConfig();
                            this.plugin.reloadMessageFile();

                            commandSender.sendMessage(getParsedMessage("reload"));
                        }
                    } else if (args[0].equalsIgnoreCase("version")) {
                        if (!commandSender.hasPermission("betterstaff.betterstaff.version")) {
                            commandSender.sendMessage(getParsedMessage("no-perms"));
                        } else {
                            commandSender.sendMessage(this.plugin.miniMessage.deserialize("<gradient:blue:red>BetterStaff Plugin</gradient>"));
                            for (Component component : Arrays.asList(this.plugin.miniMessage.deserialize("<yellow>Version: </yellow><white><version></white>", Placeholder.component("version", Component.text(this.plugin.getPluginMeta().getVersion()))),
                                    this.plugin.miniMessage.deserialize("<yellow>Made by: </yellow><white><click:open_url:https://modrinth.com/user/Milesf17>Milesf17</click></white>"),
                                    this.plugin.miniMessage.deserialize("<aqua><click:open_url:https://discord.gg/FKbGjfJVps>Click to go to Discord</click></aqua>"))) {
                                commandSender.sendMessage(component);
                            }
                        }
                    }
                } else {
                    commandSender.sendMessage(getParsedMessage("invalid-args"));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("staff")) {
            if (args.length == 1) {
                return staffTabCompletions;
            }
        }
        else if (command.getName().equalsIgnoreCase("betterstaff")) {
            if (args.length == 1) {
                return betterStaffTabCompletions;
            }
        }

        return null;
    }
}
