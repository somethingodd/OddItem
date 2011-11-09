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

package info.somethingodd.bukkit.util;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bundles both type ID and durability value in a single object.
 */
public final class ItemSpecifier implements Comparable<ItemSpecifier>, ConfigurationSerializable {
    final int typeId;
    final short durability;

    public ItemSpecifier(int typeId) {
        this(typeId, (short) 0);
    }

    public ItemSpecifier(Material type) {
        this(type.getId());
    }

    public ItemSpecifier(int typeId, short durability) {
        this.typeId = typeId;
        this.durability = durability;
    }

    public ItemSpecifier(Material type, short durability) {
        this(type.getId(), durability);
    }

    /**
     * Create an ItemSpecifier that represents the type of item stored in the ItemStack.
     */
    public static ItemSpecifier of(ItemStack itemStack) {
        return new ItemSpecifier(itemStack.getTypeId(), itemStack.getDurability());
    }

    /**
     * Create an ItemStack, given an amount.
     */
    public ItemStack toItemStack(int amount) {
        return new ItemStack(typeId, amount, durability);
    }

    /**
     * Parse an ItemSpecifier from a string in the format "id;damage".
     * @throws IllegalArgumentException if the string cannot be parsed.
     */
    public static ItemSpecifier fromString(String s) {
        String[] bits = s.split(";", 2);
        switch (bits.length) {
            case 1:
                return new ItemSpecifier(Integer.parseInt(bits[0]));
            case 2:
                return new ItemSpecifier(Integer.parseInt(bits[0]), Short.parseShort(bits[1]));
            default:
                throw new IllegalArgumentException("empty string");
        }
    }

    public Material getType() {
        return Material.getMaterial(typeId);
    }

    public int getTypeId() {
        return typeId;
    }

    public short getDurability() {
        return durability;
    }

    /**
     * Returns a string that can be passed back to {@link #fromString(String)}.
     */
    @Override
    public String toString() {
        if (durability == 0) {
            return Integer.toString(typeId);
        } else {
            return Integer.toString(typeId) + ";" + Short.toString(durability);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemSpecifier that = (ItemSpecifier) o;

        return this.typeId == that.typeId && this.durability == that.durability;
    }

    @Override
    public int hashCode() {
        int result = typeId;
        result = 41 * result + (int) durability;
        return result;
    }

    @Override
    public int compareTo(ItemSpecifier that) {
        if (this == that) return 0;

        if (this.typeId < that.typeId) return -1;
        if (this.typeId > that.typeId) return 1;

        if (this.durability < that.durability) return -1;
        if (this.durability > that.durability) return 1;

        return 0;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("type", getType());

        if (durability != 0) {
            result.put("damage", durability);
        }

        return result;
    }

    public static ItemSpecifier deserialize(Map<String, Object> args) {
        Material type = Material.getMaterial((String) args.get("type"));
        short damage = 0;

        if (args.containsKey("damage")) {
            damage = (Short) args.get("damage");
        }

        return new ItemSpecifier(type, damage);
    }
}
