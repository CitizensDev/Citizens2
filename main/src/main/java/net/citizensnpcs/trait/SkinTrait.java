package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("skintrait")
public class SkinTrait extends Trait {
    @Persist
    private boolean fetchDefaultSkin = true;

    public SkinTrait() {
        super("skintrait");
    }

    public boolean fetchDefaultSkin() {
        return fetchDefaultSkin;
    }

    public void setFetchDefaultSkin(boolean fetch) {
        this.fetchDefaultSkin = fetch;
    }
}
