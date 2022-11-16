package net.citizensnpcs;

import org.bukkit.entity.Entity;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.SmoothRotationTrait;

public class ProtocolLibListener {
    private final ProtocolManager manager;
    private final Citizens plugin;

    public ProtocolLibListener(Citizens plugin) {
        this.plugin = plugin;
        this.manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.MONITOR, Server.ENTITY_HEAD_ROTATION, Server.ENTITY_LOOK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Entity entity = manager.getEntityFromID(event.getPlayer().getWorld(),
                                event.getPacket().getIntegers().getValues().get(0));
                        if (!(entity instanceof NPCHolder))
                            return;
                        NPC npc = ((NPCHolder) entity).getNPC();
                        SmoothRotationTrait trait = npc.getTraitNullable(SmoothRotationTrait.class);
                        if (trait == null)
                            return;

                        if (event.getPacketType() == Server.ENTITY_HEAD_ROTATION) {
                            byte headYaw = event.getPacket().getBytes().read(0);
                        } else if (event.getPacketType() == Server.ENTITY_LOOK) {
                            byte yaw = event.getPacket().getBytes().read(0);
                            byte pitch = event.getPacket().getBytes().read(1);
                        } else if (event.getPacketType() == Server.ENTITY_MOVE_LOOK
                                || event.getPacketType() == Server.REL_ENTITY_MOVE_LOOK) {
                            byte yaw = event.getPacket().getBytes().read(0);
                            byte pitch = event.getPacket().getBytes().read(1);
                        }
                    }
                });
    }
}
