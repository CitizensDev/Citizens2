package net.citizensnpcs.command.command;

import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.DefaultInstanceFactory;
import net.citizensnpcs.npc.CitizensNPCManager;

public class AdminCommands {
    private final CitizensNPCManager npcManager;
    private final DefaultInstanceFactory<Character> characterManager;

    public AdminCommands(CitizensNPCManager npcManager, DefaultInstanceFactory<Character> characterManager) {
        this.npcManager = npcManager;
        this.characterManager = characterManager;
    }
}