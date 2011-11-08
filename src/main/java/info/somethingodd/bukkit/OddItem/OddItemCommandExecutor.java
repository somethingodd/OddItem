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

import info.somethingodd.bukkit.util.ItemSpecifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddItemCommandExecutor implements CommandExecutor {
    private OddItemBase oddItemBase = null;

    public OddItemCommandExecutor(OddItemBase oddItemBase) {
        this.oddItemBase = oddItemBase;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (sender instanceof Player && !sender.hasPermission("odditem." + args[0])) {
                sender.sendMessage("Not allowed.");
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            sender.sendMessage("Invalid.");
            return true;
        }
        switch (args.length) {
            case 1:
                if (args[0].equals("info")) {
                    sender.sendMessage(oddItemBase.logPrefix + OddItem.itemMap.size() + " aliases loaded");
                    return true;
                } else if (args[0].equals("reload")) {
                    try {
                        OddItem.clear();
                        new OddItemConfiguration(oddItemBase).configure(oddItemBase.configFile);
                        return true;
                    } catch (Exception e) {
                        sender.sendMessage(oddItemBase.logPrefix + "Error!");
                        oddItemBase.log.severe(oddItemBase.logPrefix + "Error on /reload! - " + e.getMessage());
                        e.printStackTrace();
                        oddItemBase.getServer().getPluginManager().disablePlugin(oddItemBase);
                    }
                }
                break;
            case 2:
                if (args[0].equals("alias") || args[0].equals("search")) {
                    ItemSpecifier item;
                    try {
                        item = ItemSpecifier.fromString(args[1]);
                    } catch (IllegalArgumentException e) {
                        item = null;
                    }

                    Collection<String> aliases;
                    String suggestion = null;
                    try {
                        if (item != null) {
                            aliases = OddItem.getAliases(item);
                        } else {
                            aliases = OddItem.getAliases(args[1]);
                        }
                    } catch (IllegalArgumentException e) {
                        aliases = null;
                        suggestion = e.getMessage();
                    }

                    if (aliases != null) {
                        sender.sendMessage(oddItemBase.logPrefix + aliases);
                    } else {
                        sender.sendMessage(oddItemBase.logPrefix + " Could not find item: " + args[1] + ". Did you mean " + suggestion + "?");
                    }
                    return true;
                }
                break;
        }
        return false;
    }
}
