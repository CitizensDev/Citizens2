package net.citizensnpcs.api.ai.speech;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.event.SpeechBystanderEvent;
import net.citizensnpcs.api.ai.speech.event.SpeechTargetedEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;

public class TalkableEntity implements Talkable {
    private final Entity entity;

    public TalkableEntity(Entity entity) {
        this.entity = entity;
    }

    public TalkableEntity(NPC npc) {
        entity = npc.getEntity();
    }

    /**
     * Used to compare a LivingEntity to this TalkableEntity
     *
     * @return 0 if the Entities are the same, 1 if they are not, -1 if the object compared is not a valid LivingEntity
     */
    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Entity)) {
            return -1;
            // If NPC and matches, return 0
        } else if (CitizensAPI.getNPCRegistry().isNPC((Entity) o) && CitizensAPI.getNPCRegistry().isNPC(entity)
                && CitizensAPI.getNPCRegistry().getNPC((Entity) o).getUniqueId()
                        .equals(CitizensAPI.getNPCRegistry().getNPC(entity).getUniqueId())) {
            return 0;
        } else if (entity.equals(o)) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public String getName() {
        if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
            return CitizensAPI.getNPCRegistry().getNPC(entity).getFullName();
        } else if (entity instanceof Player) {
            return ((Player) entity).getName();
        } else {
            return entity.getType().name().replace("_", " ");
        }
    }

    private void talk(NPC npc, String message) {
        if (!CitizensAPI.getNPCRegistry().isNPC(entity)) {
            Messaging.sendWithNPCColorless(entity, message, npc);
        }
    }

    @Override
    public void talkNear(SpeechContext context, String text) {
        SpeechBystanderEvent event = new SpeechBystanderEvent(this, context, text);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getContext().getTalker().getEntity());
        talk(npc, event.getMessage());
    }

    @Override
    public void talkTo(SpeechContext context, String text) {
        SpeechTargetedEvent event = new SpeechTargetedEvent(this, context, text);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getContext().getTalker().getEntity());
        talk(npc, event.getMessage());
    }
}
