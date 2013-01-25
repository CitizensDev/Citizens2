package net.citizensnpcs.npc.ai.speech;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.ai.speech.event.SpeechBystanderEvent;
import net.citizensnpcs.api.ai.speech.event.SpeechTargetedEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TalkableEntity implements Talkable {

    LivingEntity entity;

    public TalkableEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public TalkableEntity(NPC npc) {
        entity = npc.getBukkitEntity();
    }

    public TalkableEntity(Player player) {
        entity = (LivingEntity) player;
    }

    /**
     * Used to compare a LivingEntity to this TalkableEntity
     * 
     * @return 0 if the Entities are the same, 1 if they are not, -1 if the
     *         object compared is not a valid LivingEntity
     */
    @Override
    public int compareTo(Object o) {
        // If not living entity, return -1
        if (!(o instanceof LivingEntity))
            return -1;
        // If NPC and matches, return 0
        else if (CitizensAPI.getNPCRegistry().isNPC((LivingEntity) o)
                && CitizensAPI.getNPCRegistry().isNPC((LivingEntity) entity)
                && CitizensAPI.getNPCRegistry().getNPC((LivingEntity) o).getId() == CitizensAPI.getNPCRegistry()
                        .getNPC((LivingEntity) entity).getId())
            return 0;
        else if ((LivingEntity) o == entity)
            return 0;
        // Not a match, return 1
        else
            return 1;
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public String getName() {
        if (CitizensAPI.getNPCRegistry().isNPC(entity))
            return CitizensAPI.getNPCRegistry().getNPC(entity).getName();
        else if (entity instanceof Player)
            return ((Player) entity).getName();
        else
            return entity.getType().name().replace("_", " ");
    }

    private void talk(String message) {
        if (entity instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(entity))
            Messaging.send((Player) entity, message);
    }

    @Override
    public void talkNear(SpeechContext context, String text, VocalChord vocalChord) {
        SpeechBystanderEvent event = new SpeechBystanderEvent(this, context, text, vocalChord);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        else
            talk(event.getMessage());
    }

    @Override
    public void talkTo(SpeechContext context, String text, VocalChord vocalChord) {
        SpeechTargetedEvent event = new SpeechTargetedEvent(this, context, text, vocalChord);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        else
            talk(event.getMessage());
    }

}
