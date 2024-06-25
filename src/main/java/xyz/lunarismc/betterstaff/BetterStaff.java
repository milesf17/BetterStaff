package xyz.lunarismc.betterstaff;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;

public final class BetterStaff extends JavaPlugin {

    public LuckPerms luckPerms;

    public MiniMessage miniMessage;

    public File messageFile;

    public HashSet<Player> onDutyPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().severe("LuckPerms not installed! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        luckPerms = LuckPermsProvider.get();

        luckPerms.getContextManager().registerCalculator(new StaffContextCalculator(this));

        miniMessage = MiniMessage.miniMessage();

        saveDefaultConfig();
        saveMessageFile();

        getServer().getPluginManager().registerEvents(new StaffCommandExecutor(this), this);

        Objects.requireNonNull(this.getCommand("staff")).setExecutor(new StaffCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("betterstaff")).setExecutor(new StaffCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("stafflist")).setExecutor(new StaffCommandExecutor(this));

        createTeam("onDuty");

        getLogger().info("BetterStaff has been enabled!");
        getLogger().info("Made by Milesf17.");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterStaff has been disabled!");
        getLogger().info("Goodbye");
    }

    public void saveMessageFile() {
        if (messageFile == null) {
            messageFile = new File(getDataFolder(), "messages.yml");
        }
        if (!messageFile.exists()) {
            this.saveResource("messages.yml", true);
        }
    }

    public void reloadMessageFile() {
        saveMessageFile();
        YamlConfiguration.loadConfiguration(messageFile);
    }

    public @NotNull YamlConfiguration getMessageFile() {
        saveMessageFile();
        return YamlConfiguration.loadConfiguration(messageFile);
    }

    public Scoreboard getScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        return manager.getMainScoreboard();
    }

    public boolean checkTeam(String teamName) {
        return getScoreboard().getTeam(teamName) != null;
    }

    public void createTeam(String teamName) {
        if (!checkTeam(teamName)) {
            Team team = getScoreboard().registerNewTeam(teamName);
        }
    }

    public boolean playerinTeam(String teamName, Player player) {
        createTeam(teamName);
        return Objects.requireNonNull(getScoreboard().getTeam(teamName)).hasPlayer(player);
    }
}
