/* This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.somethingodd.bukkit.OddItem;

import info.somethingodd.bukkit.OddItem.bktree.BKTree;
import info.somethingodd.bukkit.OddItem.bktree.DistanceStrategies;
import info.somethingodd.bukkit.OddItem.bktree.DistanceStrategy;
import info.somethingodd.bukkit.util.ItemSpecifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public final class OddItemConfiguration {
    private OddItemBase oddItemBase = null;
    private int version;

    public OddItemConfiguration(OddItemBase OddItemBase) {
        this.oddItemBase = OddItemBase;
    }

    public void configure(String fileName) throws Exception {
        File file = new File(fileName);
        if (!file.exists())
            if (!writeConfig(file)) throw new Exception("Could not create configuration file!");

        OddItem.itemMap = new HashMap<String, ItemSpecifier>();
        OddItem.items = new TreeMap<ItemSpecifier, NavigableSet<String>>();
        OddItem.groups = new TreeMap<String, OddItemGroup>();

        Configuration configuration = new Configuration(file);
        configuration.load();

        version = configuration.getInt("listversion", 0);

        DistanceStrategy<String> comparator = DistanceStrategies.get(configuration.getString("comparator"));
        if (comparator == null) {
            comparator = DistanceStrategies.get("levenshtein");
        }
        OddItem.bktree = new BKTree<String>(comparator);
        oddItemBase.log.info(oddItemBase.logPrefix + "Using " + comparator + " for suggestions.");

        ConfigurationNode itemsNode = configuration.getNode("items");
        for (Map.Entry<String, Object> entry : itemsNode.getAll().entrySet()) {
            // Parse the item ID
            String keyString = entry.getKey();
            ItemSpecifier key = ItemSpecifier.fromString(keyString);

            // Warn if the item does not exist
            if (key.getType() == null) {
                oddItemBase.log.warning(oddItemBase.logPrefix + "Unknown item: " + keyString);
            }

            // Get the set of aliases for this item
            NavigableSet<String> itemAliases = OddItem.items.get(key);

            // Create the set of aliases, if it does not exist already
            if (itemAliases == null) {
                itemAliases = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                OddItem.items.put(key, itemAliases);
            }

            // Add all the aliases
            if (entry.getValue() instanceof List) {
                for (String alias : (List<String>) entry.getValue()) {
                    alias = alias.toLowerCase();
                    itemAliases.add(alias);
                    OddItem.itemMap.put(alias, key);
                    OddItem.bktree.add(alias);
                }
            }
        }

        ConfigurationNode groupsNode = configuration.getNode("groups");
        if (OddItem.groups != null) {
            for (String g : groupsNode.getKeys()) {
                List<String> i = new ArrayList<String>();
                if (groupsNode.getKeys(g) == null) {
                    i.addAll(groupsNode.getStringList(g, new ArrayList<String>()));
                    groupsNode.removeProperty(g);
                    groupsNode.setProperty(g + ".items", i);
                    groupsNode.setProperty(g + ".data", "null");
                } else {
                    i.addAll(groupsNode.getStringList(g + ".items", new ArrayList<String>()));
                }
                List<ItemStack> itemStackList = new ArrayList<ItemStack>();
                for (String is : i) {
                    ItemStack itemStack;
                    Integer q = null;
                    try {
                        if (is.contains(",")) {
                            q = Integer.valueOf(is.substring(is.indexOf(",") + 1));
                            is = is.substring(0, is.indexOf(","));
                            itemStack = OddItem.getItemStack(is, q);
                        } else {
                            itemStack = OddItem.getItemStack(is);
                        }
                        oddItemBase.log.info(oddItemBase.logPrefix + "Adding " + is + (q != null ? " x" + q : "") + " to group \"" + g + "\"");
                        if (itemStack != null) itemStackList.add(itemStack);
                    } catch (IllegalArgumentException e) {
                        oddItemBase.log.warning(oddItemBase.logPrefix + "Invalid item \"" + is + "\" in group \"" + g + "\"");
                        OddItem.groups.remove(g);
                    } catch (NullPointerException e) {
                        oddItemBase.log.warning(oddItemBase.logPrefix + "NPE adding ItemStack \"" + is + "\" to group " + g);
                    }
                    OddItem.groups.put(g, new OddItemGroup(g, itemStackList));
                }
                if (OddItem.groups.get(g) != null) oddItemBase.log.info(oddItemBase.logPrefix + "Group " + g + " added.");
            }
        }
        configuration.save();
    }

    private boolean writeConfig(File file) {
        FileWriter fw;
        if (!file.getParentFile().exists()) file.getParentFile().mkdir();
        try {
            fw = new FileWriter(file);
        } catch (IOException e) {
            oddItemBase.log.severe(oddItemBase.logPrefix + "Couldn't write config file: " + e.getMessage());
            Bukkit.getServer().getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("OddItem"));
            return false;
        }
        BufferedReader i = new BufferedReader(new InputStreamReader(new BufferedInputStream(oddItemBase.getClass().getResourceAsStream("/OddItem.yml"))));
        BufferedWriter o = new BufferedWriter(fw);
        try {
            String line = i.readLine();
            while (line != null) {
                o.write(line + System.getProperty("line.separator"));
                line = i.readLine();
            }
            oddItemBase.log.info(oddItemBase.logPrefix + "Wrote default config");
        } catch (IOException e) {
            oddItemBase.log.severe(oddItemBase.logPrefix + "Error writing config: " + e.getMessage());
        } finally {
            try {
                o.close();
                i.close();
            } catch (IOException e) {
                oddItemBase.log.severe(oddItemBase.logPrefix + "Error saving config: " + e.getMessage());
                Bukkit.getServer().getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("OddItem"));
            }
        }
        return true;
    }

    private int getVersion() {
        return version;
    }
}
