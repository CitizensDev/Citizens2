package net.citizensnpcs.api.ai;

public class NavigatorParameters implements Cloneable {
    private float range;
    private float speed;

    @Override
    public NavigatorParameters clone() {
        try {
            return (NavigatorParameters) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public float range() {
        return range;
    }

    public NavigatorParameters range(float range) {
        this.range = range;
        return this;
    }

    public float speed() {
        return speed;
    }

    public NavigatorParameters speed(float speed) {
        this.speed = speed;
        return this;
    }
}
