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

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.ConfigurationNode;

import java.util.*;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddItemGroup implements Iterable<ItemStack> {
    final String name;
    final List<ItemStack> items;
    final ConfigurationNode data;

    public OddItemGroup(String name, Collection<ItemStack> items, ConfigurationNode data) {
        this.name = name;
        this.items = Collections.unmodifiableList(new ArrayList<ItemStack>(items));
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public ConfigurationNode getData() {
        return data;
    }

    public ItemStack get(int index) throws IndexOutOfBoundsException {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return items.iterator();
    }

    /**
     * Returns whether group contains ItemStack matching material and durability, ignoring quantity
     * @param is ItemStack to look for
     * @return boolean group contains ItemStack
     */
    public boolean contains(ItemStack is) {
        return contains(is, false);
    }

    /**
     * Returns whether group contains ItemStack matching material and durability
     * @param is ItemStack to look for
     * @param quantity boolean whether to check quantity of ItemStack
     * @return boolean group contains ItemStack
     */
    public boolean contains(ItemStack is, boolean quantity) {
        return contains(is, true, quantity);
    }

    /**
     * Returns whether group contains ItemStack matching material
     * @param is ItemStack to look for
     * @param durability boolean whether to check durability of ItemStack
     * @param quantity boolean whether to check quantity of ItemStack
     * @return boolean group contains ItemStack
     */
    public boolean contains(ItemStack is, boolean durability, boolean quantity) {
        for (ItemStack i : items)
            if (OddItem.compare(is, i, durability, quantity)) return true;
        return false;
    }

    public String toString() {
        List<String> itemNames = new ArrayList<String>();
        for (ItemStack itemStack : items) {
            itemNames.add(OddItem.getAliases(itemStack).first());
        }
        return itemNames.toString();
    }
}
