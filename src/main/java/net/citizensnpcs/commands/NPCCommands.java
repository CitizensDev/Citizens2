package net.citizensnpcs.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.NPCSkeletonType;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.ZombieModifier;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Paginator;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Requirements(selected = true, ownership = true)
public class NPCCommands {
    private final NPCRegistry npcRegistry;
    private final NPCSelector selector;

    public NPCCommands(Citizens plugin) {
        npcRegistry = CitizensAPI.getNPCRegistry();
        selector = plugin.getNPCSelector();
    }

    @Command(
            aliases = { "시민" },
            usage = "나이 [나이] (-l)",
            desc = "NPC의 나이를 설정합니다",
            help = Messages.COMMAND_AGE_HELP,
            flags = "l",
            modifiers = { "나이" },
            min = 1,
            max = 2,
            permission = "citizens.npc.age")
    @Requirements(selected = true, ownership = true)
    public void age(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.isSpawned() || !(npc.getBukkitEntity() instanceof Ageable))
            throw new CommandException(Messages.MOBTYPE_CANNOT_BE_AGED);
        Age trait = npc.getTrait(Age.class);

        boolean toggleLock = args.hasFlag('l');
        if (toggleLock) {
            Messaging.sendTr(sender, trait.toggle() ? Messages.AGE_LOCKED : Messages.AGE_UNLOCKED);
        }
        if (args.argsLength() <= 1) {
            if (!toggleLock)
                trait.describe(sender);
            return;
        }
        int age = 0;
        try {
            age = args.getInteger(1);
            if (age < -24000 || age > 0)
                throw new CommandException(Messages.INVALID_AGE);
            Messaging.sendTr(sender, Messages.AGE_SET_NORMAL, npc.getName(), age);
        } catch (NumberFormatException ex) {
            if (args.getString(1).equalsIgnoreCase("아기")) {
                age = -24000;
                Messaging.sendTr(sender, Messages.AGE_SET_BABY, npc.getName());
            } else if (args.getString(1).equalsIgnoreCase("어른")) {
                age = 0;
                Messaging.sendTr(sender, Messages.AGE_SET_ADULT, npc.getName());
            } else
                throw new CommandException(Messages.INVALID_AGE);
        }

        trait.setAge(age);
    }

    @Command(
            aliases = { "시민" },
            usage = "고정 (--저장 [이름]|--가정 [이름]|--삭제 [이름]) (-a)(-c)",
            desc = "NPC의 고정된 위치를 저장/변경/나열합니다",
            flags = "ac",
            modifiers = { "고정" },
            min = 1,
            max = 3,
            permission = "citizens.npc.anchor")
    @Requirements(selected = true, ownership = true)
    public void anchor(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Anchors trait = npc.getTrait(Anchors.class);
        if (args.hasValueFlag("저장")) {
            if (args.getFlag("저장").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (args.hasFlag('c')) {
                if (trait.addAnchor(args.getFlag("저장"), args.getSenderTargetBlockLocation())) {
                    Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
                } else
                    throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, args.getFlag("저장"));
            } else {
                if (trait.addAnchor(args.getFlag("저장"), args.getSenderLocation())) {
                    Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
                } else
                    throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, args.getFlag("저장"));
            }
        } else if (args.hasValueFlag("가정")) {
            if (args.getFlag("가정").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            Anchor anchor = trait.getAnchor(args.getFlag("가정"));
            if (anchor == null)
                throw new CommandException(Messages.ANCHOR_MISSING, args.getFlag("가정"));
            npc.getBukkitEntity().teleport(anchor.getLocation());
        } else if (args.hasValueFlag("제거")) {
            if (args.getFlag("제거").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);
            if (trait.removeAnchor(trait.getAnchor(args.getFlag("제거"))))
                Messaging.sendTr(sender, Messages.ANCHOR_REMOVED);
            else
                throw new CommandException(Messages.ANCHOR_MISSING, args.getFlag("제거"));
        } else if (!args.hasFlag('a')) {
            Paginator paginator = new Paginator().header("Anchors");
            paginator.addLine("<e>키: <a>ID  <b>이름  <c>월드  <d>위치 (X,Y,Z)");
            for (int i = 0; i < trait.getAnchors().size(); i++) {
                String line = "<a>" + i + "<b>  " + trait.getAnchors().get(i).getName() + "<c>  "
                        + trait.getAnchors().get(i).getLocation().getWorld().getName() + "<d>  "
                        + trait.getAnchors().get(i).getLocation().getBlockX() + ", "
                        + trait.getAnchors().get(i).getLocation().getBlockY() + ", "
                        + trait.getAnchors().get(i).getLocation().getBlockZ();
                paginator.addLine(line);
            }

            int page = args.getInteger(1, 1);
            if (!paginator.sendPage(sender, page))
                throw new CommandException(Messages.COMMAND_PAGE_MISSING);
        }

        // Assume Player's position
        if (!args.hasFlag('a'))
            return;
        if (sender instanceof ConsoleCommandSender)
            throw new ServerCommandException();
        npc.getBukkitEntity().teleport(args.getSenderLocation());
    }

    @Command(
            aliases = { "시민" },
            usage = "controllable|조작 -f",
            desc = "NPC를 조작 그리고 탑승할 수 있게 전환합니다",
            modifiers = { "controllable", "조작" },
            min = 1,
            max = 1,
            flags = "f")
    public void controllable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if ((npc.isSpawned() && !sender.hasPermission("citizens.npc.controllable."
                + npc.getBukkitEntity().getType().toString().toLowerCase()))
                || !sender.hasPermission("citizens.npc.controllable"))
            throw new NoPermissionsException();
        if (!npc.hasTrait(Controllable.class)) {
            npc.addTrait(new Controllable(false));
        }
        Controllable trait = npc.getTrait(Controllable.class);
        boolean enabled = trait.toggle();
        String key = enabled ? Messages.CONTROLLABLE_SET : Messages.CONTROLLABLE_REMOVED;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "복사 (--이름 새이름)",
            desc = "NPC를 복사합니다",
            modifiers = { "복사" },
            min = 1,
            max = 1,
            permission = "citizens.npc.copy")
    public void copy(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        EntityType type = npc.getTrait(MobType.class).getType();
        String name = args.getFlag("이름", npc.getFullName());
        CitizensNPC copy = (CitizensNPC) npcRegistry.createNPC(type, name);
        CitizensNPC from = (CitizensNPC) npc;

        DataKey key = new MemoryDataKey();
        from.save(key);
        copy.load(key);

        if (from.isSpawned() && args.getSenderLocation() != null) {
            Location location = args.getSenderLocation();
            location.getChunk().load();
            copy.getBukkitEntity().teleport(location);
            copy.getTrait(CurrentLocation.class).setLocation(location);
        }

        for (Trait trait : copy.getTraits())
            trait.onCopy();

        CommandSenderCreateNPCEvent event = sender instanceof Player ? new PlayerCreateNPCEvent((Player) sender, copy)
                : new CommandSenderCreateNPCEvent(sender, copy);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            event.getNPC().destroy();
            String reason = "NPC를 만들 수 업슷ㅂ니다.";
            if (!event.getCancelReason().isEmpty())
                reason += " 이유: " + event.getCancelReason();
            throw new CommandException(reason);
        }

        Messaging.sendTr(sender, Messages.NPC_COPIED, npc.getName());
        selector.select(sender, copy);
    }

    @Command(
            aliases = { "시민" },
            usage = "만들기 [이름] ((-b,u) --위치 (x:y:z:월드) --타입 (타입) --특성 ('특성1, 특성2...') --행동 (행동들))",
            desc = "새 NPC를 만듭니다",
            flags = "bu",
            modifiers = { "만들기" },
            min = 2,
            permission = "citizens.npc.create")
    @Requirements
    public void create(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String name = Colorizer.parseColors(args.getJoinedStrings(1));
        if (name.length() > 16) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG);
            name = name.substring(0, 15);
        }
        if (name.length() <= 0)
            throw new CommandException();

        EntityType type = EntityType.PLAYER;
        if (args.hasValueFlag("타입")) {
            String inputType = args.getFlag("타입");
            type = Util.matchEntityType(inputType);
            if (type == null) {
                Messaging.sendErrorTr(sender, Messages.NPC_CREATE_INVALID_MOBTYPE, inputType);
                type = EntityType.PLAYER;
            } else if (!LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                Messaging.sendErrorTr(sender, Messages.NOT_LIVING_MOBTYPE, type);
                type = EntityType.PLAYER;
            }
        }
        if (!sender.hasPermission("citizens.npc.create.*") && !sender.hasPermission("citizens.npc.createall")
                && !sender.hasPermission("citizens.npc.create." + type.name().toLowerCase().replace("_", "")))
            throw new NoPermissionsException();

        npc = npcRegistry.createNPC(type, name);
        String msg = "당신은 [[" + npc.getName() + "]]을/를 만들었습니다";

        int age = 0;
        if (args.hasFlag('b')) {
            if (!Ageable.class.isAssignableFrom(type.getEntityClass()))
                Messaging.sendErrorTr(sender, Messages.MOBTYPE_CANNOT_BE_AGED,
                        type.name().toLowerCase().replace("_", "-"));
            else {
                age = -24000;
                msg += " 아기인체로";
            }
        }

        // Initialize necessary traits
        if (!Setting.SERVER_OWNS_NPCS.asBoolean())
            npc.getTrait(Owner.class).setOwner(sender.getName());
        npc.getTrait(MobType.class).setType(type);

        Location spawnLoc = null;
        if (sender instanceof Player) {
            spawnLoc = args.getSenderLocation();
        } else if (sender instanceof BlockCommandSender) {
            spawnLoc = args.getSenderLocation();
        }
        CommandSenderCreateNPCEvent event = sender instanceof Player ? new PlayerCreateNPCEvent((Player) sender, npc)
                : new CommandSenderCreateNPCEvent(sender, npc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            npc.destroy();
            String reason = "NPC를 만들 수 없습니다.";
            if (!event.getCancelReason().isEmpty())
                reason += " 이유: " + event.getCancelReason();
            throw new CommandException(reason);
        }

        if (args.hasValueFlag("위치")) {
            String[] parts = Iterables.toArray(Splitter.on(':').split(args.getFlag("위치")), String.class);
            if (parts.length > 0) {
                String worldName = args.getSenderLocation() != null ? args.getSenderLocation().getWorld().getName()
                        : "";
                int x = 0, y = 0, z = 0;
                float yaw = 0F, pitch = 0F;
                switch (parts.length) {
                    case 6:
                        pitch = Float.parseFloat(parts[5]);
                    case 5:
                        yaw = Float.parseFloat(parts[4]);
                    case 4:
                        worldName = parts[3];
                    case 3:
                        x = Integer.parseInt(parts[0]);
                        y = Integer.parseInt(parts[1]);
                        z = Integer.parseInt(parts[2]);
                        break;
                    default:
                        throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
                }
                World world = Bukkit.getWorld(worldName);
                if (world == null)
                    throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
                spawnLoc = new Location(world, x, y, z, yaw, pitch);
            } else {
                Player search = Bukkit.getPlayerExact(args.getFlag("위치"));
                if (search == null)
                    throw new CommandException(Messages.PLAYER_NOT_FOUND_FOR_SPAWN);
                spawnLoc = search.getLocation();
            }
        }
        if (spawnLoc == null) {
            npc.destroy();
            throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
        }

        if (!args.hasFlag('u')) {
            npc.spawn(spawnLoc);
        }

        if (args.hasValueFlag("특성")) {
            Iterable<String> parts = Splitter.on(',').trimResults().split(args.getFlag("특성"));
            StringBuilder builder = new StringBuilder();
            for (String tr : parts) {
                Trait trait = CitizensAPI.getTraitFactory().getTrait(tr);
                if (trait == null)
                    continue;
                npc.addTrait(trait);
                builder.append(StringHelper.wrap(tr) + ", ");
            }
            if (builder.length() > 0)
                builder.delete(builder.length() - 2, builder.length());
            msg += " 특성들과 함께 " + builder.toString();
        }

        if (args.hasValueFlag("템플릿")) {
            Iterable<String> parts = Splitter.on(',').trimResults().split(args.getFlag("템플릿"));
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                Template template = Template.byName(part);
                if (template == null)
                    continue;
                template.apply(npc);
                builder.append(StringHelper.wrap(part) + ", ");
            }
            if (builder.length() > 0)
                builder.delete(builder.length() - 2, builder.length());
            msg += " 템플릿과 함께 " + builder.toString();
        }

        // Set age after entity spawns
        if (npc.getBukkitEntity() instanceof Ageable)
            npc.getTrait(Age.class).setAge(age);
        selector.select(sender, npc);
        Messaging.send(sender, msg + '.');
    }

    @Command(
            aliases = { "시민" },
            usage = "제거 (id)",
            desc = "NPC를 제거합니다",
            modifiers = { "제거" },
            min = 1,
            max = 2,
            permission = "citizens.npc.despawn")
    @Requirements
    public void despawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc == null || args.argsLength() == 2) {
            if (args.argsLength() < 2)
                throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
            int id = args.getInteger(1);
            npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc == null)
                throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, id);
        }
        npc.getTrait(Spawned.class).setSpawned(false);
        npc.despawn(DespawnReason.REMOVAL);
        Messaging.sendTr(sender, Messages.NPC_DESPAWNED, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "게임모드 [게임모드]",
            desc = "게임모드를 변경합니다",
            modifiers = { "게임모드" },
            min = 1,
            max = 2,
            permission = "citizens.npc.gravity")
    @Requirements(selected = true, ownership = true, types = { EntityType.PLAYER })
    public void gamemode(CommandContext args, CommandSender sender, NPC npc) {
        Player player = (Player) npc.getBukkitEntity();
        if (args.argsLength() == 1) {
            Messaging.sendTr(sender, Messages.GAMEMODE_DESCRIBE, npc.getName(), player.getGameMode().name()
                    .toLowerCase());
            return;
        }
        GameMode mode = null;
        try {
            int value = args.getInteger(1);
            mode = GameMode.getByValue(value);
        } catch (NumberFormatException ex) {
            try {
                mode = GameMode.valueOf(args.getString(1));
            } catch (IllegalArgumentException e) {
            }
        }
        if (mode == null) {
            Messaging.sendErrorTr(sender, Messages.GAMEMODE_INVALID, args.getString(1));
            return;
        }
        player.setGameMode(mode);
        Messaging.sendTr(sender, Messages.GAMEMODE_SET, mode.name().toLowerCase());
    }

    @Command(
            aliases = { "시민" },
            usage = "위험",
            desc = "위험으로 전환시킵니다",
            modifiers = { "위험" },
            min = 1,
            max = 1,
            permission = "citizens.npc.gravity")
    public void gravity(CommandContext args, CommandSender sender, NPC npc) {
        boolean enabled = npc.getTrait(Gravity.class).toggle();
        String key = enabled ? Messages.GRAVITY_ENABLED : Messages.GRAVITY_DISABLED;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "id",
            desc = "선택된 NPC의 ID를 보여줍니다",
            modifiers = { "id" },
            min = 1,
            max = 1,
            permission = "citizens.npc.id")
    public void id(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender, npc.getId());
    }

    @Command(
            aliases = { "시민" },
            usage = "목록 (페이지) ((-a) --주인 (주인) --타입 (타입) --대화 (대화))",
            desc = "NPC들의 목록",
            flags = "a",
            modifiers = { "목록" },
            min = 1,
            max = 2,
            permission = "citizens.npc.list")
    @Requirements
    public void list(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<NPC> npcs = new ArrayList<NPC>();

        if (args.hasFlag('a')) {
            for (NPC add : npcRegistry)
                npcs.add(add);
        } else if (args.getValueFlags().size() == 0 && sender instanceof Player) {
            for (NPC add : npcRegistry) {
                if (!npcs.contains(add) && add.getTrait(Owner.class).isOwnedBy(sender))
                    npcs.add(add);
            }
        } else {
            if (args.hasValueFlag("주인")) {
                String name = args.getFlag("주인");
                for (NPC add : npcRegistry) {
                    if (!npcs.contains(add) && add.getTrait(Owner.class).isOwnedBy(name))
                        npcs.add(add);
                }
            }

            if (args.hasValueFlag("타입")) {
                EntityType type = Util.matchEntityType(args.getFlag("타입"));

                if (type == null)
                    throw new CommandException(Messages.COMMAND_INVALID_MOBTYPE, type);

                for (NPC add : npcRegistry) {
                    if (!npcs.contains(add) && add.getTrait(MobType.class).getType() == type)
                        npcs.add(add);
                }
            }
        }

        Paginator paginator = new Paginator().header("NPCs");
        paginator.addLine("<e>키: <a>ID  <b>이름");
        for (int i = 0; i < npcs.size(); i += 2) {
            String line = "<a>" + npcs.get(i).getId() + "<b>  " + npcs.get(i).getName();
            if (npcs.size() >= i + 2)
                line += "      " + "<a>" + npcs.get(i + 1).getId() + "<b>  " + npcs.get(i + 1).getName();
            paginator.addLine(line);
        }

        int page = args.getInteger(1, 1);
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING);
    }

    @Command(
            aliases = { "시민" },
            usage = "근접보기",
            desc = "NPC가 주변의 플레이어를 보도록 전환합니다",
            modifiers = { "근접보기", "look", "rotate" },
            min = 1,
            max = 1,
            permission = "citizens.npc.lookclose")
    public void lookClose(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.sendTr(sender, npc.getTrait(LookClose.class).toggle() ? Messages.LOOKCLOSE_SET
                : Messages.LOOKCLOSE_STOPPED, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "탑승",
            desc = "조작가능한 NPC에 올라탑니다",
            modifiers = { "탑승" },
            min = 1,
            max = 1,
            permission = "citizens.npc.controllable")
    public void mount(CommandContext args, Player player, NPC npc) {
        boolean enabled = npc.hasTrait(Controllable.class) && npc.getTrait(Controllable.class).isEnabled();
        if (!enabled) {
            Messaging.sendTr(player, Messages.NPC_NOT_CONTROLLABLE, npc.getName());
            return;
        }
        boolean success = npc.getTrait(Controllable.class).mount(player);
        if (!success)
            Messaging.sendTr(player, Messages.FAILED_TO_MOUNT_NPC, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "보내기 x:y:z:world | x y z world",
            desc = "시민을 해당 위치로 보내버립니다",
            modifiers = "보내기",
            min = 1,
            permission = "citizens.npc.moveto")
    public void moveto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        Location current = npc.getBukkitEntity().getLocation();
        Location to;
        if (args.argsLength() > 1) {
            String[] parts = Iterables.toArray(Splitter.on(':').split(args.getJoinedStrings(1, ':')), String.class);
            if (parts.length != 4 && parts.length != 3)
                throw new CommandException(Messages.MOVETO_FORMAT);
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            World world = parts.length == 4 ? Bukkit.getWorld(parts[3]) : current.getWorld();
            if (world == null)
                throw new CommandException(Messages.WORLD_NOT_FOUND);
            to = new Location(world, x, y, z, current.getYaw(), current.getPitch());
        } else {
            to = current.clone();
            if (args.hasValueFlag("x"))
                to.setX(args.getFlagDouble("x"));
            if (args.hasValueFlag("y"))
                to.setY(args.getFlagDouble("y"));
            if (args.hasValueFlag("z"))
                to.setZ(args.getFlagDouble("z"));
            if (args.hasValueFlag("yaw"))
                to.setYaw((float) args.getFlagDouble("yaw"));
            if (args.hasValueFlag("pitch"))
                to.setPitch((float) args.getFlagDouble("pitch"));
            if (args.hasValueFlag("world")) {
                World world = Bukkit.getWorld(args.getFlag("world"));
                if (world == null)
                    throw new CommandException(Messages.WORLD_NOT_FOUND);
                to.setWorld(world);
            }
        }

        npc.getBukkitEntity().teleport(to, TeleportCause.COMMAND);
        Messaging.sendTr(sender, Messages.MOVETO_TELEPORTED, npc.getName(), to);
    }

    @Command(
            aliases = { "시민" },
            modifiers = { "이름" },
            usage = "이름",
            desc = "명찰을 투명하게 전환시킵니다",
            min = 1,
            max = 1,
            permission = "citizens.npc.name")
    @Requirements(selected = true, ownership = true)
    public void name(CommandContext args, CommandSender sender, NPC npc) {
        npc.getBukkitEntity().setCustomNameVisible(!npc.getBukkitEntity().isCustomNameVisible());
        Messaging.sendTr(sender, Messages.NAMEPLATE_VISIBILITY_TOGGLED);
    }

    @Command(aliases = { "시민" }, desc = "NPC의 기본 정보를 보여줍니다", max = 0, permission = "citizens.npc.info")
    public void npc(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(sender, "    <a>ID: <e>" + npc.getId());
        Messaging.send(sender, "    <a>타입: <e>" + npc.getTrait(MobType.class).getType());
        if (npc.isSpawned()) {
            Location loc = npc.getBukkitEntity().getLocation();
            String format = "    <a>소환된 장소: <e>%d, %d, %d <a>in world<e> %s";
            Messaging.send(sender,
                    String.format(format, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
        }
        Messaging.send(sender, "    <a>특성<e>");
        for (Trait trait : npc.getTraits()) {
            if (CitizensAPI.getTraitFactory().isInternalTrait(trait))
                continue;
            String message = "     <e>- <a>" + trait.getName();
            Messaging.send(sender, message);
        }
    }

    @Command(
            aliases = { "시민" },
            usage = "주인 [이름]",
            desc = "NPC의 주인을 설정합니다",
            modifiers = { "주인" },
            min = 1,
            max = 2,
            permission = "citizens.npc.owner")
    public void owner(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Owner ownerTrait = npc.getTrait(Owner.class);
        if (args.argsLength() == 1) {
            Messaging.sendTr(sender, Messages.NPC_OWNER, npc.getName(), ownerTrait.getOwner());
            return;
        }
        String name = args.getString(1);
        if (ownerTrait.isOwnedBy(name))
            throw new CommandException(Messages.ALREADY_OWNER, name, npc.getName());
        ownerTrait.setOwner(name);
        boolean serverOwner = name.equalsIgnoreCase(Owner.SERVER);
        Messaging.sendTr(sender, serverOwner ? Messages.OWNER_SET_SERVER : Messages.OWNER_SET, npc.getName(), name);
    }

    @Command(aliases = { "시민" }, usage = "경로범위 [범위]", desc = "NPC의 경로 탐색 범위를 설정합니다", modifiers = {
            "경로범위", "pathfindingrange", "prange" }, min = 2, max = 2, permission = "citizens.npc.pathfindingrange")
    public void pathfindingRange(CommandContext args, CommandSender sender, NPC npc) {
        double range = Math.max(1, args.getDouble(1));
        npc.getNavigator().getDefaultParameters().range((float) range);
        Messaging.sendTr(sender, Messages.PATHFINDING_RANGE_SET, range);
    }

    @Command(
            aliases = { "시민" },
            usage = "플레이어목록 (-a,r)",
            desc = "NPC가 플레이어 목록에 표시되게 됩니다",
            modifiers = { "플레이어목록" },
            min = 1,
            max = 1,
            flags = "ar",
            permission = "citizens.npc.playerlist")
    @Requirements(types = EntityType.PLAYER)
    public void playerlist(CommandContext args, CommandSender sender, NPC npc) {
        boolean remove = !npc.data().get("removefromplayerlist", Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
        if (args.hasFlag('a'))
            remove = false;
        else if (args.hasFlag('r'))
            remove = true;
        npc.data().setPersistent("removefromplayerlist", remove);
        if (npc.isSpawned())
            NMS.addOrRemoveFromPlayerList(npc.getBukkitEntity(), remove);
        Messaging.sendTr(sender, remove ? Messages.REMOVED_FROM_PLAYERLIST : Messages.ADDED_TO_PLAYERLIST,
                npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "자세 (--저장 [이름]|--가정 [이름]|--제거 [이름]) (-a)",
            desc = "NPC의 머리 자세를 변경/저장/나열합니다",
            flags = "a",
            modifiers = { "자세" },
            min = 1,
            max = 2,
            permission = "citizens.npc.pose")
    @Requirements(selected = true, ownership = true)
    public void pose(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Poses trait = npc.getTrait(Poses.class);
        if (args.hasValueFlag("저장")) {
            if (args.getFlag("저장").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (trait.addPose(args.getFlag("저장"), args.getSenderLocation())) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, args.getFlag("저장"));
        } else if (args.hasValueFlag("가정")) {
            String pose = args.getFlag("가정");
            if (pose.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (!trait.hasPose(pose))
                throw new CommandException(Messages.POSE_MISSING, pose);
            trait.assumePose(pose);
        } else if (args.hasValueFlag("제거")) {
            if (args.getFlag("제거").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);
            if (trait.removePose(args.getFlag("제거"))) {
                Messaging.sendTr(sender, Messages.POSE_REMOVED);
            } else
                throw new CommandException(Messages.POSE_MISSING, args.getFlag("제거"));
        } else if (!args.hasFlag('a')) {
            trait.describe(sender, args.getInteger(1, 1));
        }

        // Assume Player's pose
        if (!args.hasFlag('a'))
            return;
        if (args.getSenderLocation() == null)
            throw new ServerCommandException();
        Location location = args.getSenderLocation();
        trait.assumePose(location);
    }

    @Command(
            aliases = { "시민" },
            usage = "힘",
            desc = "크리퍼 NPC가 힘세지는걸 전환합니다",
            modifiers = { "힘" },
            min = 1,
            max = 1,
            permission = "citizens.npc.power")
    @Requirements(selected = true, ownership = true, types = { EntityType.CREEPER })
    public void power(CommandContext args, CommandSender sender, NPC npc) {
        Messaging
                .sendTr(sender, npc.getTrait(Powered.class).toggle() ? Messages.POWERED_SET : Messages.POWERED_STOPPED);
    }

    @Command(
            aliases = { "시민" },
            usage = "직업|prof [직업]",
            desc = "NPC의 직업을 설정합니다",
            modifiers = { "직업", "prof" },
            min = 2,
            max = 2,
            permission = "citizens.npc.profession")
    @Requirements(selected = true, ownership = true, types = { EntityType.VILLAGER })
    public void profession(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String profession = args.getString(1);
        Profession parsed;
        try {
            parsed = Profession.valueOf(profession.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CommandException(Messages.INVALID_PROFESSION);
        }
        npc.getTrait(VillagerProfession.class).setProfession(parsed);
        Messaging.sendTr(sender, Messages.PROFESSION_SET, npc.getName(), profession);
    }

    @Command(
            aliases = { "시민" },
            usage = "삭제|rem (모두)",
            desc = "NPC를 삭제합니다",
            modifiers = { "삭제", "rem" },
            min = 1,
            max = 2)
    @Requirements
    public void remove(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.argsLength() == 2) {
        	  if (!args.getString(1).equalsIgnoreCase("모두"))
                throw new CommandException(Messages.REMOVE_INCORRECT_SYNTAX);
            if (!sender.hasPermission("citizens.admin.remove.all") && !sender.hasPermission("citizens.admin"))
                throw new NoPermissionsException();
            npcRegistry.deregisterAll();
            Messaging.sendTr(sender, Messages.REMOVED_ALL_NPCS);
            return;
        }
        if (!(sender instanceof Player))
            throw new CommandException(Messages.COMMAND_MUST_BE_INGAME);
        Player player = (Player) sender;
        if (npc == null)
            throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
        if (!npc.getTrait(Owner.class).isOwnedBy(player))
            throw new CommandException(Messages.COMMAND_MUST_BE_OWNER);
        if (!player.hasPermission("citizens.npc.remove") && !player.hasPermission("citizens.admin"))
            throw new NoPermissionsException();
        npc.destroy();
        Messaging.sendTr(player, Messages.NPC_REMOVED, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "이름변경 [이름]",
            desc = "NPC의 이름을 변경합니다",
            modifiers = { "이름변경" },
            min = 2,
            permission = "citizens.npc.rename")
    public void rename(CommandContext args, CommandSender sender, NPC npc) {
        String oldName = npc.getName();
        String newName = args.getJoinedStrings(1);
        if (newName.length() > 16) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG);
            newName = newName.substring(0, 15);
        }
        Location prev = npc.isSpawned() ? npc.getBukkitEntity().getLocation() : null;
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.setName(newName);
        if (prev != null)
            npc.spawn(prev);

        Messaging.sendTr(sender, Messages.NPC_RENAMED, oldName, newName);
    }

    @Command(
            aliases = { "시민" },
            usage = "선택|sel [id|이름] (--r range)",
            desc = "NPC의 주어진 ID나 이름을 통해 선택합니다",
            modifiers = { "선택", "sel" },
            min = 1,
            max = 2,
            permission = "citizens.npc.select")
    @Requirements
    public void select(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPC toSelect = null;
        if (args.argsLength() <= 1) {
            if (!(sender instanceof Player))
                throw new ServerCommandException();
            double range = Math.abs(args.getFlagDouble("r", 10));
            Entity player = (Player) sender;
            final Location location = args.getSenderLocation();
            List<Entity> search = player.getNearbyEntities(range, range, range);
            Collections.sort(search, new Comparator<Entity>() {
                @Override
                public int compare(Entity o1, Entity o2) {
                    double d = o1.getLocation().distanceSquared(location) - o2.getLocation().distanceSquared(location);
                    return d > 0 ? 1 : d < 0 ? -1 : 0;
                }
            });
            for (Entity possibleNPC : search) {
                NPC test = npcRegistry.getNPC(possibleNPC);
                if (test == null)
                    continue;
                toSelect = test;
                break;
            }
        } else {
            try {
                int id = args.getInteger(1);
                toSelect = npcRegistry.getById(id);
            } catch (NumberFormatException ex) {
                String name = args.getString(1);
                List<NPC> possible = Lists.newArrayList();
                for (NPC test : npcRegistry) {
                    if (test.getName().equalsIgnoreCase(name))
                        possible.add(test);
                }
                if (possible.size() == 1) {
                    toSelect = possible.get(0);
                } else if (possible.size() > 1) {
                    SelectionPrompt.start(selector, (Player) sender, possible);
                    return;
                }
            }
        }
        if (toSelect == null || !toSelect.getTrait(Spawned.class).shouldSpawn())
            throw new CommandException(Messages.NPC_NOT_FOUND);
        if (npc != null && toSelect.getId() == npc.getId())
            throw new CommandException(Messages.NPC_ALREADY_SELECTED);
        selector.select(sender, toSelect);
        Messaging.sendWithNPC(sender, Setting.SELECTION_MESSAGE.asString(), toSelect);
    }

    @Command(aliases = { "시민" }, usage = "스켈레톤타입 [타입]", desc = "NPC의 스켈레톤 타입을 설정합니다", modifiers = {
            "스켈레톤타입", "sktype" }, min = 2, max = 2, permission = "citizens.npc.skeletontype")
    @Requirements(selected = true, ownership = true, types = EntityType.SKELETON)
    public void skeletonType(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        SkeletonType type = (type = SkeletonType.getType(args.getInteger(1))) == null ? SkeletonType.valueOf(args
                .getString(1)) : type;
        if (type == null)
            throw new CommandException(Messages.INVALID_SKELETON_TYPE);
        npc.getTrait(NPCSkeletonType.class).setType(type);
        Messaging.sendTr(sender, Messages.SKELETON_TYPE_SET, npc.getName(), type);
    }

    @Command(
    		   aliases = { "시민" },
               usage = "크기 [크기]",
               desc = "NPC의 크기를 설정합니다",
               modifiers = { "크기" },
            min = 1,
            max = 2,
            permission = "citizens.npc.size")
    @Requirements(selected = true, ownership = true, types = { EntityType.MAGMA_CUBE, EntityType.SLIME })
    public void slimeSize(CommandContext args, CommandSender sender, NPC npc) {
        SlimeSize trait = npc.getTrait(SlimeSize.class);
        if (args.argsLength() <= 1) {
            trait.describe(sender);
            return;
        }
        int size = Math.max(1, args.getInteger(1));
        trait.setSize(size);
        Messaging.sendTr(sender, Messages.SIZE_SET, npc.getName(), size);
    }

    @Command(
    	    aliases = { "시민" },
            usage = "스폰 [id]",
            desc = "존재하는 NPC를 소환합니다",
            modifiers = { "스폰" },
            min = 2,
            max = 2,
            permission = "citizens.npc.spawn")
    @Requirements(ownership = true)
    public void spawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPC respawn = npcRegistry.getById(args.getInteger(1));
        if (respawn == null)
            throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, args.getInteger(1));
        if (respawn.isSpawned())
            throw new CommandException(Messages.NPC_ALREADY_SPAWNED, respawn.getName());

        Location location = respawn.getTrait(CurrentLocation.class).getLocation();
        if (location == null) {
            if (args.getSenderLocation() == null)
                throw new CommandException(Messages.NO_STORED_SPAWN_LOCATION);

            location = args.getSenderLocation();
        }
        if (respawn.spawn(location)) {
            selector.select(sender, respawn);
            Messaging.sendTr(sender, Messages.NPC_SPAWNED, respawn.getName());
        }
    }

    @Command(
    		aliases = { "시민" },
            usage = "메세지를 말하기 --목표 npcid|플레이어 이름 --타입 vocal_type",
            desc = "NPC의 말을 조정하는 것을 사용합니다",
            modifiers = { "말하기" },
            min = 2,
            permission = "citizens.npc.speak")
    public void speak(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String type = npc.getTrait(Speech.class).getDefaultVocalChord();
        String message = Colorizer.parseColors(args.getJoinedStrings(1));

        if (message.length() <= 0) {
            Messaging.send(sender, "기본 음성 가락 " + npc.getName() + ": "
                    + npc.getTrait(Speech.class).getDefaultVocalChord());
            return;
        }

        SpeechContext context = new SpeechContext(message);

        if (args.hasValueFlag("목표")) {
            if (args.getFlag("목표").matches("\\d+")) {
                NPC target = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(args.getFlag("목표")));
                if (target != null)
                    context.addRecipient(target.getBukkitEntity());
            } else {
                Player player = Bukkit.getPlayer(args.getFlag("목표"));
                if (player != null)
                    context.addRecipient(player);
            }
        }

        if (args.hasValueFlag("타입")) {
            if (CitizensAPI.getSpeechFactory().isRegistered(args.getFlag("타입")))
                type = args.getFlag("타입");
        }

        npc.getDefaultSpeechController().speak(context, type);
    }

    @Command(
    		aliases = { "시민" },
            usage = "속도 [속도]",
            desc = "NPC의 이동 속도를 퍼센트로 설정합니다",
            modifiers = { "속도" },
            min = 2,
            max = 2,
            permission = "citizens.npc.speed")
    public void speed(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        float newSpeed = (float) Math.abs(args.getDouble(1));
        if (newSpeed >= Setting.MAX_SPEED.asDouble())
            throw new CommandException(Messages.SPEED_MODIFIER_ABOVE_LIMIT);
        npc.getNavigator().getDefaultParameters().speedModifier(newSpeed);

        Messaging.sendTr(sender, Messages.SPEED_MODIFIER_SET, newSpeed);
    }

    @Command(
            aliases = { "시민" },
            usage = "텔포",
            desc = "NPC에게로 텔포합니다",
            modifiers = { "텔포", "텔레포트" },
            min = 1,
            max = 1,
            permission = "citizens.npc.tp")
    public void tp(CommandContext args, Player player, NPC npc) {
        Location to = npc.getTrait(CurrentLocation.class).getLocation();
        player.teleport(to, TeleportCause.COMMAND);
        Messaging.sendTr(player, Messages.TELEPORTED_TO_NPC, npc.getName());
    }

    @Command(aliases = { "시민" }, usage = "부르기", desc = "NPC를 당신의 위치로 텔포시킵니다", modifiers = { "부르기",
            "tph", "move" }, min = 1, max = 1, permission = "citizens.npc.tphere")
    public void tphere(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.getSenderLocation() == null)
            throw new ServerCommandException();
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned()) {
            npc.spawn(args.getSenderLocation());
            if (!sender.hasPermission("citizens.npc.tphere.multiworld")
                    && npc.getBukkitEntity().getLocation().getWorld() != args.getSenderLocation().getWorld()) {
                npc.despawn(DespawnReason.REMOVAL);
                throw new CommandException(Messages.CANNOT_TELEPORT_ACROSS_WORLDS);
            }
        } else {
            if (!sender.hasPermission("citizens.npc.tphere.multiworld")
                    && npc.getBukkitEntity().getLocation().getWorld() != args.getSenderLocation().getWorld()) {
                npc.despawn(DespawnReason.REMOVAL);
                throw new CommandException(Messages.CANNOT_TELEPORT_ACROSS_WORLDS);
            }
            npc.getBukkitEntity().teleport(args.getSenderLocation(), TeleportCause.COMMAND);
        }
        Messaging.sendTr(sender, Messages.NPC_TELEPORTED, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "이동 [플레이어 이름|npc id] [플레이어 이름|npc id]",
            desc = "NPC 나 플레이어에서 다른 NPC나 플레이어에게로 텔포합니다",
            modifiers = { "이동" },
            min = 3,
            max = 3,
            permission = "citizens.npc.tpto")
    @Requirements
    public void tpto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Entity from = null, to = null;
        if (npc != null)
            from = npc.getBukkitEntity();
        boolean firstWasPlayer = false;
        try {
            int id = args.getInteger(1);
            NPC fromNPC = CitizensAPI.getNPCRegistry().getById(id);
            if (fromNPC != null)
                from = fromNPC.getBukkitEntity();
        } catch (NumberFormatException e) {
            from = Bukkit.getPlayerExact(args.getString(1));
            firstWasPlayer = true;
        }
        try {
            int id = args.getInteger(2);
            NPC toNPC = CitizensAPI.getNPCRegistry().getById(id);
            if (toNPC != null)
                to = toNPC.getBukkitEntity();
        } catch (NumberFormatException e) {
            if (!firstWasPlayer)
                to = Bukkit.getPlayerExact(args.getString(2));
        }
        if (from == null)
            throw new CommandException(Messages.FROM_ENTITY_NOT_FOUND);
        if (to == null)
            throw new CommandException(Messages.TO_ENTITY_NOT_FOUND);
        from.teleport(to);
        Messaging.sendTr(sender, Messages.TPTO_SUCCESS);
    }

    @Command(
            aliases = { "시민" },
            usage = "타입 [타입]",
            desc = "NPC의 실제 타입을 설정합니다",
            modifiers = { "타입" },
            min = 2,
            max = 2,
            permission = "citizens.npc.type")
    public void type(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        EntityType type = Util.matchEntityType(args.getString(1));
        if (type == null)
            throw new CommandException(Messages.INVALID_ENTITY_TYPE, args.getString(1));
        npc.setBukkitEntityType(type);
        Messaging.sendTr(sender, Messages.ENTITY_TYPE_SET, npc.getName(), args.getString(1));
    }

    @Command(
            aliases = { "시민" },
            usage = "허약 (-t)",
            desc = "NPC가 허약해지게 전환합니다",
            modifiers = { "허약" },
            min = 1,
            max = 1,
            flags = "t",
            permission = "citizens.npc.vulnerable")
    public void vulnerable(CommandContext args, CommandSender sender, NPC npc) {
        boolean vulnerable = !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
        if (args.hasFlag('t')) {
            npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, vulnerable);
        } else {
            npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, vulnerable);
        }
        String key = vulnerable ? Messages.VULNERABLE_STOPPED : Messages.VULNERABLE_SET;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "시민" },
            usage = "늑대 (-앉(기) 화(남) 길(든)) --목걸이 [삼원색]",
            desc = "늑대를 수정합니다",
            modifiers = { "늑대" },
            min = 1,
            max = 1,
            flags = "앉화길",
            permission = "citizens.npc.wolf")
    @Requirements(selected = true, ownership = true, types = EntityType.WOLF)
    public void wolf(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Wolf wolf = (Wolf) npc.getBukkitEntity();
        wolf.setAngry(args.hasFlag('화'));
        wolf.setSitting(args.hasFlag('앉'));
        wolf.setTamed(args.hasFlag('길'));
        if (args.hasValueFlag("목걸이")) {
            String unparsed = args.getFlag("목걸이");
            DyeColor color = null;
            try {
                DyeColor.valueOf(unparsed.toUpperCase().replace(' ', '_'));
            } catch (IllegalArgumentException e) {
                int rgb = Integer.parseInt(unparsed.replace("#", ""));
                color = DyeColor.getByColor(org.bukkit.Color.fromRGB(rgb));
            }
            if (color == null)
                throw new CommandException(Messages.COLLAR_COLOUR_NOT_RECOGNISED);
            wolf.setCollarColor(color);
        }
    }

    @Command(
            aliases = { "시민" },
            usage = "좀비모드 (-아(기), -주(민))",
            desc = "좀비 NPC를 아기나 주민으로 설정합니다",
            modifiers = { "zombie", "좀비모드" },
            flags = "아주",
            min = 1,
            max = 1,
            permission = "citizens.npc.zombiemodifier")
    @Requirements(selected = true, ownership = true, types = EntityType.ZOMBIE)
    public void zombieModifier(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        ZombieModifier trait = npc.getTrait(ZombieModifier.class);
        if (args.hasFlag('아')) {
            boolean isBaby = trait.toggleBaby();
            Messaging.sendTr(sender, isBaby ? Messages.ZOMBIE_BABY_SET : Messages.ZOMBIE_BABY_UNSET, npc.getName());
        }
        if (args.hasFlag('주')) {
            boolean isVillager = trait.toggleVillager();
            Messaging.sendTr(sender, isVillager ? Messages.ZOMBIE_VILLAGER_SET : Messages.ZOMBIE_VILLAGER_UNSET,
                    npc.getName());
        }
    }
}
