// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.citizensnpcs.command;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CommandContext {
    protected String[] args;
    protected final Set<Character> flags = new HashSet<Character>();
    protected final Map<String, String> valueFlags = Maps.newHashMap();
    private final CommandSender sender;
    private Location location = null;

    public Location getSenderLocation() {
        if (location != null)
            return location;
        if (sender instanceof Player)
            location = ((Player) sender).getLocation();
        else if (sender instanceof BlockCommandSender)
            location = ((BlockCommandSender) sender).getBlock().getLocation();
        return location;
    }

    public CommandContext(CommandSender sender, String[] args) {
        this.sender = sender;
        int i = 1;
        for (; i < args.length; i++) {
            // initial pass for quotes
            args[i] = args[i].trim();
            if (args[i].length() == 0) {
                // Ignore this
                continue;
            } else if (args[i].charAt(0) == '\'' || args[i].charAt(0) == '"') {
                char quote = args[i].charAt(0);
                String quoted = args[i].substring(1); // remove initial quote
                for (int inner = i + 1; inner < args.length; inner++) {
                    if (args[inner].isEmpty())
                        continue;
                    String test = args[inner].trim();
                    quoted += " " + test;
                    if (test.charAt(test.length() - 1) == quote) {
                        args[i] = quoted.substring(0, quoted.length() - 1);
                        for (int j = i + 1; j != inner; ++j)
                            args[j] = "";
                        // remove ending quote
                        break;
                    }
                }
            }
        }
        for (i = 1; i < args.length; ++i) {
            // second pass for flags
            if (args[i].length() == 0)
                continue;
            if (i + 1 < args.length && args[i].length() > 2 && args[i].matches("^--[a-zA-Z]+$")) {
                int inner = i + 1;
                while (args[inner].length() == 0) {
                    // later args may have been quoted
                    ++inner;
                    if (inner >= args.length) {
                        inner = -1;
                        break;
                    }
                }

                if (inner != -1) {
                    valueFlags.put(args[i].toLowerCase().replaceFirst("--", ""), args[inner]);
                    args[i] = "";
                    args[inner] = "";
                }
            } else if (args[i].charAt(0) == '-' && args[i].matches("^-[a-zA-Z]+$")) {
                for (int k = 1; k < args[i].length(); k++)
                    flags.add(args[i].charAt(k));
                args[i] = "";
            }
        }
        List<String> copied = Lists.newArrayList();
        for (String arg : args) {
            arg = arg.trim();
            if (arg == null || arg.isEmpty())
                continue;
            copied.add(arg.trim());
        }
        this.args = copied.toArray(new String[copied.size()]);
    }

    public int argsLength() {
        return args.length - 1;
    }

    public String getCommand() {
        return args[0];
    }

    public double getDouble(int index) throws NumberFormatException {
        return Double.parseDouble(args[index + 1]);
    }

    public double getDouble(int index, double def) throws NumberFormatException {
        return index + 1 < args.length ? Double.parseDouble(args[index + 1]) : def;
    }

    public String getFlag(String ch) {
        return valueFlags.get(ch);
    }

    public String getFlag(String ch, String def) {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return value;
    }

    public double getFlagDouble(String ch) throws NumberFormatException {
        return Double.parseDouble(valueFlags.get(ch));
    }

    public double getFlagDouble(String ch, double def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Double.parseDouble(value);
    }

    public int getFlagInteger(String ch) throws NumberFormatException {
        return Integer.parseInt(valueFlags.get(ch));
    }

    public int getFlagInteger(String ch, int def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Integer.parseInt(value);
    }

    public Set<Character> getFlags() {
        return flags;
    }

    public int getInteger(int index) throws NumberFormatException {
        return Integer.parseInt(args[index + 1]);
    }

    public int getInteger(int index, int def) throws NumberFormatException {
        if (index + 1 < args.length) {
            try {
                return Integer.parseInt(args[index + 1]);
            } catch (NumberFormatException ex) {
            }
        }
        return def;
    }

    public String getJoinedStrings(int initialIndex) {
        return getJoinedStrings(initialIndex, ' ');
    }

    public String getJoinedStrings(int initialIndex, char delimiter) {
        initialIndex = initialIndex + 1;
        StringBuilder buffer = new StringBuilder(args[initialIndex]);
        for (int i = initialIndex + 1; i < args.length; i++)
            buffer.append(delimiter).append(args[i]);
        return buffer.toString().trim();
    }

    public String[] getPaddedSlice(int index, int padding) {
        String[] slice = new String[args.length - index + padding];
        System.arraycopy(args, index, slice, padding, args.length - index);
        return slice;
    }

    public String[] getSlice(int index) {
        String[] slice = new String[args.length - index];
        System.arraycopy(args, index, slice, 0, args.length - index);
        return slice;
    }

    public String getString(int index) {
        return args[index + 1];
    }

    public String getString(int index, String def) {
        return index + 1 < args.length ? args[index + 1] : def;
    }

    public Map<String, String> getValueFlags() {
        return valueFlags;
    }

    public boolean hasFlag(char ch) {
        return flags.contains(ch);
    }

    public boolean hasValueFlag(String ch) {
        return valueFlags.containsKey(ch);
    }

    public int length() {
        return args.length;
    }

    public boolean matches(String command) {
        return args[0].equalsIgnoreCase(command);
    }
}