package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.npc.skin.SkinnableEntity;

@TraitName("skinlayers")
public class SkinLayers extends Trait {
    @Persist("cape")
    private boolean cape = true;
    @Persist("hat")
    private boolean hat = true;
    @Persist("jacket")
    private boolean jacket = true;
    @Persist("left-pants")
    private boolean leftPants = true;
    @Persist("left-sleeve")
    private boolean leftSleeve = true;
    @Persist("right-pants")
    private boolean rightPants = true;
    @Persist("right-sleeve")
    private boolean rightSleeve = true;

    public SkinLayers() {
        super("skinlayers");
    }

    public SkinLayers hide() {
        cape = false;
        hat = false;
        jacket = false;
        leftSleeve = false;
        rightSleeve = false;
        leftPants = false;
        rightPants = false;
        setFlags();
        return this;
    }

    public SkinLayers hideCape() {
        cape = false;
        setFlags();
        return this;
    }

    public SkinLayers hideHat() {
        hat = false;
        setFlags();
        return this;
    }

    public SkinLayers hideJacket() {
        jacket = false;
        setFlags();
        return this;
    }

    public SkinLayers hideLeftPants() {
        leftPants = false;
        setFlags();
        return this;
    }

    public SkinLayers hideLeftSleeve() {
        leftSleeve = false;
        setFlags();
        return this;
    }

    public SkinLayers hidePants() {
        leftPants = false;
        rightPants = false;
        setFlags();
        return this;
    }

    public SkinLayers hideRightPants() {
        rightPants = false;
        setFlags();
        return this;
    }

    public SkinLayers hideRightSleeve() {
        rightSleeve = false;
        setFlags();
        return this;
    }

    public SkinLayers hideSleeves() {
        leftSleeve = false;
        rightSleeve = false;
        setFlags();
        return this;
    }

    public boolean isVisible(Layer layer) {
        switch (layer) {
            case CAPE:
                return cape;
            case JACKET:
                return jacket;
            case LEFT_SLEEVE:
                return leftSleeve;
            case RIGHT_SLEEVE:
                return rightSleeve;
            case LEFT_PANTS:
                return leftPants;
            case RIGHT_PANTS:
                return rightPants;
            case HAT:
                return hat;
            default:
                return false;
        }
    }

    @Override
    public void onAttach() {
        setFlags();
    }

    @Override
    public void onSpawn() {
        setFlags();
    }

    private void setFlags() {
        if (!npc.isSpawned())
            return;

        SkinnableEntity skinnable = npc.getEntity() instanceof SkinnableEntity ? (SkinnableEntity) npc.getEntity()
                : null;
        if (skinnable == null)
            return;

        int flags = 0xFF;
        for (Layer layer : Layer.values()) {
            if (!isVisible(layer)) {
                flags &= ~layer.flag;
            }
        }
        skinnable.setSkinFlags((byte) flags);
    }

    public SkinLayers setVisible(Layer layer, boolean isVisible) {
        switch (layer) {
            case CAPE:
                cape = isVisible;
                break;
            case JACKET:
                jacket = isVisible;
                break;
            case LEFT_SLEEVE:
                leftSleeve = isVisible;
                break;
            case RIGHT_SLEEVE:
                rightSleeve = isVisible;
                break;
            case LEFT_PANTS:
                leftPants = isVisible;
                break;
            case RIGHT_PANTS:
                rightPants = isVisible;
                break;
            case HAT:
                hat = isVisible;
                break;
        }
        setFlags();
        return this;
    }

    public SkinLayers show() {
        cape = true;
        hat = true;
        jacket = true;
        leftSleeve = true;
        rightSleeve = true;
        leftPants = true;
        rightPants = true;
        setFlags();
        return this;
    }

    public SkinLayers showCape() {
        cape = true;
        setFlags();
        return this;
    }

    public SkinLayers showHat() {
        hat = true;
        setFlags();
        return this;
    }

    public SkinLayers showJacket() {
        jacket = true;
        setFlags();
        return this;
    }

    public SkinLayers showLeftPants() {
        leftPants = true;
        setFlags();
        return this;
    }

    public SkinLayers showLeftSleeve() {
        leftSleeve = true;
        setFlags();
        return this;
    }

    public SkinLayers showPants() {
        leftPants = true;
        rightPants = true;
        setFlags();
        return this;
    }

    public SkinLayers showRightPants() {
        rightPants = true;
        setFlags();
        return this;
    }

    public SkinLayers showRightSleeve() {
        rightSleeve = true;
        setFlags();
        return this;
    }

    public SkinLayers showSleeves() {
        leftSleeve = true;
        rightSleeve = true;
        setFlags();
        return this;
    }

    @Override
    public String toString() {
        return "SkinLayers{cape:" + cape + ", hat:" + hat + ", jacket:" + jacket + ", leftSleeve:" + leftSleeve
                + ", rightSleeve:" + rightSleeve + ", leftPants:" + leftPants + ", rightPants:" + rightPants + "}";
    }

    public enum Layer {
        CAPE(0),
        HAT(6),
        JACKET(1),
        LEFT_PANTS(4),
        LEFT_SLEEVE(2),
        RIGHT_PANTS(5),
        RIGHT_SLEEVE(3);

        final int flag;

        Layer(int offset) {
            this.flag = 1 << offset;
        }
    }
}
