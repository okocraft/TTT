package net.okocraft.ttt;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class SpawnerUtil {

    private final TTT plugin;
    private final NamespacedKey entityKey;
    
    SpawnerUtil(TTT plugin) {
        this.plugin = plugin;
        this.entityKey = NamespacedKey.fromString("entity", plugin);
    }

    public ItemStack createSpawner(EntityType type) {
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(entityKey, PersistentDataType.STRING, type.name());
        // spawner.getItemMeta().displayName(null); // TODO: message config value like "{0} spawner".repalceAll({0}, mobname);
        spawner.setItemMeta(meta);
        return spawner;
    }

    public Optional<EntityType> getEntityTypeFrom(ItemStack spawner) {
        ItemMeta meta = spawner.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }

        String type = meta.getPersistentDataContainer().get(entityKey, PersistentDataType.STRING);
        if (type == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(EntityType.valueOf(type));
        } catch(IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
