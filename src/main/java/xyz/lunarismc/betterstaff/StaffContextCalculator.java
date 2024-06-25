package xyz.lunarismc.betterstaff;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class StaffContextCalculator implements ContextCalculator<Player> {

    private final BetterStaff plugin;

    public StaffContextCalculator(BetterStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public void calculate(@NonNull Player player, @NonNull ContextConsumer contextConsumer) {
        if (Objects.requireNonNull(this.plugin.getScoreboard().getTeam("onDuty")).hasPlayer(player)) {
            contextConsumer.accept("betterstaff:is_duty", "true");
        }
        else {
            contextConsumer.accept("betterstaff:is_duty", "false");
        }
    }

    @Override
    public @NonNull ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        builder.add("betterstaff:is_duty", "true");
        builder.add("betterstaff:is_duty", "false");

        return builder.build();
    }
}
