package net.citizensnpcs.listener;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityListen implements Listener {

	@EventHandler(event = EntityDamageEvent.class, priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled() || !CitizensAPI.getNPCManager().isNPC(event.getEntity()))
			return;

		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if (e.getDamager() instanceof Player) {
				NPC npc = CitizensAPI.getNPCManager().getNPC(event.getEntity());
				npc.getCharacter().onLeftClick(npc, (Player) e.getDamager());
			}
		}
	}

	@EventHandler(event = EntityTargetEvent.class, priority = EventPriority.NORMAL)
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled() || !CitizensAPI.getNPCManager().isNPC(event.getEntity())
				|| !(event.getTarget() instanceof Player))
			return;

		NPC npc = CitizensAPI.getNPCManager().getNPC(event.getEntity());
		npc.getCharacter().onRightClick(npc, (Player) event.getTarget());
	}
}