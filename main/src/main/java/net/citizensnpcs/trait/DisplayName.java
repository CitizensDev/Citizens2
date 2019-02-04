package net.citizensnpcs.trait;

import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("displayname")
public class DisplayName extends Trait implements CommandConfigurable{

	@Persist public String name;
	
	public DisplayName(){
		super("displayname");
	}

	public void configure(CommandContext args){
		name = args.getJoinedStrings(1);
	}
	
	public String getDisplayName(){
		if (name == null) return npc.getName();
		return name;
	}

}