package net.citizensnpcs.api.event;

/**
 * Called when Citizens is reloaded
 */
public class CitizensReloadEvent extends CitizensEvent {
	private static final long serialVersionUID = -3880546787412641097L;

	public CitizensReloadEvent(String name) {
		super("CitizensReloadEvent");
	}
}