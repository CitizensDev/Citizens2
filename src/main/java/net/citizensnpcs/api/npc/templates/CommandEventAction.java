package net.citizensnpcs.api.npc.templates;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;

public class CommandEventAction implements Consumer<NPC> {
    private final Set<UUID> uuids = Sets.newHashSet();

    public CommandEventAction(Class<? extends NPCEvent> clazz, Consumer<NPC> commands) {
        try {
            HandlerList handlers = (HandlerList) clazz.getMethod("getHandlerList").invoke(null);
            handlers.register(new RegisteredListener(new Listener() {
            }, (listener, event) -> {
                try {
                    if (event.getClass() != clazz)
                        return;
                    NPC npc = (NPC) GET_NPC.invoke(event);
                    if (uuids.contains(npc.getUniqueId())) {
                        commands.accept(npc);
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }, EventPriority.NORMAL, CitizensAPI.getPlugin(), true));
        } catch (Throwable ex) {
            Messaging.severe("Error registering template event listener");
            ex.printStackTrace();
        }
    }

    @Override
    public void accept(NPC npc) {
        uuids.add(npc.getUniqueId());
    }

    private static MethodHandle GET_NPC = null;
    static {
        try {
            GET_NPC = MethodHandles.publicLookup().findVirtual(NPCEvent.class, "getNPC",
                    MethodType.methodType(NPC.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
