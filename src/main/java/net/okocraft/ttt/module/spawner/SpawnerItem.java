package net.okocraft.ttt.module.spawner;

import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.ttt.config.Messages;
import net.okocraft.ttt.config.Settings;

public class SpawnerItem extends Spawner<ItemStack> {

    private SpawnerItem(@NotNull ItemStack spawner) {
        super(spawner.clone());
    }

    public static boolean isValid(ItemStack spawner) {
        if (spawner == null) {
            return false;
        }
        ItemMeta meta = spawner.getItemMeta();
        if (spawner.getType() != Material.SPAWNER || meta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        if (!dataContainer.has(entityKey, PersistentDataType.STRING)) {
            return false;
        }
        if (!dataContainer.has(maxSpawnableMobsKey, PersistentDataType.INTEGER)) {
            return false;
        }
        if (!dataContainer.has(spawnableMobsKey, PersistentDataType.INTEGER)) {
            return false;
        }

        return true;
    }

    public static SpawnerItem from(ItemStack item) {
        if (item == null) {
            item = new ItemStack(Material.SPAWNER);
        }
        SpawnerItem spawner = new SpawnerItem(item);
        if (!isValid(item)) {
            spawner.setSpawnedType(spawner.getSpawnedType());
            spawner.setMaxSpawnableMobs(Settings.getMaxSpawnableMobs(
                    plugin.getConfiguration(),
                    null,
                    spawner.getSpawnedType()
            ));
            spawner.setSpawnableMobs(spawner.getSpawnableMobs());
        }
        return spawner;
    }

    public static SpawnerItem from(CreatureSpawner creatureSpawner) {
        return from(SpawnerState.from(creatureSpawner));
    }

    public static SpawnerItem from(SpawnerState spawnerState) {
        return create(spawnerState.getSpawnedType(), spawnerState.getMaxSpawnableMobs(), spawnerState.getSpawnableMobs());
    }

    public static SpawnerItem create(EntityType spawnedType, int maxSpawnableMobs, int spawnableMobs) {
        SpawnerItem spawner = new SpawnerItem(new ItemStack(Material.SPAWNER));
        spawner.setSpawnedType(spawnedType);
        spawner.setMaxSpawnableMobs(maxSpawnableMobs);
        spawner.setSpawnableMobs(spawnableMobs);
        return spawner;
    }
    
    public ItemStack getWithLocale(Locale locale) {
        changeLocale(locale);
        return spawner;
    }

    private void changeLocale(Locale locale) {
        ItemMeta meta = spawner.getItemMeta();
        EntityType type = getSpawnedType();
        meta.displayName(GlobalTranslator.render(Messages.SPAWNER_DISPLAY_NAME.apply(type), locale));
        meta.lore(List.of(GlobalTranslator.render(Messages.SPAWNER_LORE.apply(this), locale)));
        
        spawner.setItemMeta(meta);
    }

    @Override
    public void setSpawnedType(EntityType type) {
        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(entityKey, PersistentDataType.STRING, type.name());
        spawner.setItemMeta(meta);
    }

    @Override
    public EntityType getSpawnedType() {
        ItemMeta meta = spawner.getItemMeta();
        String type = meta.getPersistentDataContainer().get(entityKey, PersistentDataType.STRING);
        if (type == null) {
            return EntityType.PIG;
        }

        try {
            return EntityType.valueOf(type);
        } catch(IllegalArgumentException e) {
            return EntityType.PIG;
        }
    }

    @Override
    public int getMaxSpawnableMobs() {
        return spawner.getItemMeta().getPersistentDataContainer().getOrDefault(maxSpawnableMobsKey, PersistentDataType.INTEGER, 100000);
    }
    
    @Override
    public void setMaxSpawnableMobs(int maxSpawnableMobs) {
        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(maxSpawnableMobsKey, PersistentDataType.INTEGER, maxSpawnableMobs);
        spawner.setItemMeta(meta);

        if (getSpawnableMobs() > maxSpawnableMobs) {
            setSpawnableMobs(maxSpawnableMobs);
        }
    }

    @Override
    public int getSpawnableMobs() {
        return spawner.getItemMeta().getPersistentDataContainer().getOrDefault(spawnableMobsKey, PersistentDataType.INTEGER, getMaxSpawnableMobs());
    }

    @Override
    public void setSpawnableMobs(int spawnableMobs) {
        int maxSpawnableMobs = getMaxSpawnableMobs();
        if (spawnableMobs > maxSpawnableMobs) {
            spawnableMobs = maxSpawnableMobs;
        }
        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(spawnableMobsKey, PersistentDataType.INTEGER, spawnableMobs);
        spawner.setItemMeta(meta);
    }
}
