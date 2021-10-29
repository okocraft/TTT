package net.okocraft.ttt.config.worldsetting.spawner;

public record IsolatingSetting(
        boolean enabled,
        int radius,
        int amount,
        int plotsquaredRadius,
        int plotsquaredAmount
) {
}
