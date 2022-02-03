package net.okocraft.ttt.module.spawner;

import java.util.List;
import java.util.Locale;

import com.github.siroshun09.configapi.api.Configuration;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.ttt.TTT;
import net.okocraft.ttt.language.Messages;

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
        /* okocraft ancient - disable max-spawnable-mobs-limit
        if (!dataContainer.has(maxSpawnableMobsKey, PersistentDataType.INTEGER)) {
            return false;
        }
        if (!dataContainer.has(spawnableMobsKey, PersistentDataType.INTEGER)) {
            return false;
        }
        */

        return true;
    }

    public static void fixData(ItemStack spawner) {
        if (isValid(spawner)) {
            return;
        }
        if (spawner == null) {
            return;
        }
        ItemMeta meta = spawner.getItemMeta();
        if (spawner.getType() != Material.SPAWNER || meta == null) {
            return;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        if (dataContainer.has(entityKey, PersistentDataType.STRING)) {
            String typeName = dataContainer.get(entityKey, PersistentDataType.STRING);
            EntityType type;
            if (typeName != null) {
                try {
                    type = EntityType.valueOf(typeName);
                } catch(IllegalArgumentException e) {
                    type = EntityType.PIG;
                }
            } else {
                type = EntityType.PIG;
            }
            dataContainer.set(entityKey, PersistentDataType.STRING, type.name());
            /* okocraft ancient - disable max-spawnable-mobs-limit
            int max = TTT.getPlugin(TTT.class).getConfiguration()
                    .getInteger("world-setting.default.spawner.max-spawnable-mobs.DEFAULT", 100000);
            dataContainer.set(maxSpawnableMobsKey, PersistentDataType.INTEGER, max);
            dataContainer.set(spawnableMobsKey, PersistentDataType.INTEGER, max);
            */
            spawner.setItemMeta(meta);
        }
    }

    public static SpawnerItem from(ItemStack item) {
        if (item == null) {
            item = new ItemStack(Material.SPAWNER);
        }
        SpawnerItem spawner = new SpawnerItem(item);
        if (!isValid(item)) {
            spawner.setSpawnedType(spawner.getSpawnedType());
            /* okocraft ancient - disable max-spawnable-mobs-limit
            Configuration config = TTT.getPlugin(TTT.class).getConfiguration();
            int maxSpawnableMobs = config
                    .getInteger("world-setting.default.spawner.max-spawnable-mobs." + spawner.getSpawnedType().name(), 
                            config.getInteger("world-setting.default.spawner.max-spawnable-mobs.DEFAULT", 100000));
            spawner.setMaxSpawnableMobs(maxSpawnableMobs);
            spawner.setSpawnableMobs(spawner.getSpawnableMobs());
            */
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
        /* okocraft ancient - disable max-spawnable-mobs-limit
        spawner.setMaxSpawnableMobs(maxSpawnableMobs);
        spawner.setSpawnableMobs(spawnableMobs);
         */
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
        // okocraft ancient - disable max-spawnable-mobs-limit
        //meta.lore(List.of(GlobalTranslator.render(Messages.SPAWNER_LORE.apply(this), locale)));
        
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
        // okocraft ancient - disable max-spawnable-mobs-limit
        if (true) return Integer.MAX_VALUE;
        return spawner.getItemMeta().getPersistentDataContainer().getOrDefault(maxSpawnableMobsKey, PersistentDataType.INTEGER, 100000);
    }
    
    @Override
    public void setMaxSpawnableMobs(int maxSpawnableMobs) {
        // okocraft ancient - disable max-spawnable-mobs-limit
        if (true) return;
        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(maxSpawnableMobsKey, PersistentDataType.INTEGER, maxSpawnableMobs);
        spawner.setItemMeta(meta);

        if (getSpawnableMobs() > maxSpawnableMobs) {
            setSpawnableMobs(maxSpawnableMobs);
        }
    }

    @Override
    public int getSpawnableMobs() {
        // okocraft ancient - disable max-spawnable-mobs-limit
        if (true) return Integer.MAX_VALUE;
        return spawner.getItemMeta().getPersistentDataContainer().getOrDefault(spawnableMobsKey, PersistentDataType.INTEGER, getMaxSpawnableMobs());
    }

    @Override
    public void setSpawnableMobs(int spawnableMobs) {
        // okocraft ancient - disable max-spawnable-mobs-limit
        if (true) return;
        int maxSpawnableMobs = getMaxSpawnableMobs();
        if (spawnableMobs > maxSpawnableMobs) {
            spawnableMobs = maxSpawnableMobs;
        }
        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(spawnableMobsKey, PersistentDataType.INTEGER, spawnableMobs);
        spawner.setItemMeta(meta);
    }
}
