package net.citizensnpcs.api.ai;

public class NavigatorParameters implements Cloneable {
    private float range;
    private float speed;
    private float speedModifier = 1F;

    /**
     * 
     * @return The base movement speed
     */
    public float baseSpeed() {
        return speed;
    }

    @Override
    public NavigatorParameters clone() {
        try {
            return (NavigatorParameters) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * @return The pathfinding range of the navigator in blocks.
     * @see #range(float)
     */
    public float range() {
        return range;
    }

    /**
     * Sets the pathfinding range in blocks. The pathfinding range determines
     * how far away the {@link Navigator} will attempt to pathfind before giving
     * up to save computations.
     * 
     * @param range
     *            The new range
     */
    public NavigatorParameters range(float range) {
        this.range = range;
        return this;
    }

    /**
     * @return The modified movement speed as given by {@link #baseSpeed()}
     *         multiplied by {@link #speedModifier()}
     */
    public float speed() {
        return speed * speedModifier;
    }

    /**
     * Sets the base movement speed of the {@link Navigator}. Note that this is
     * mob-specific and may not always be sane. Using {@link #speedModifier()}
     * is preferred.
     * 
     * @see #speedModifier()
     * @param speed
     *            The new movement speed
     */
    public NavigatorParameters speed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * @return The movement speed modifier
     * @see #speedModifier(float)
     */
    public float speedModifier() {
        return speedModifier;
    }

    /**
     * Sets the movement speed modifier of the {@link Navigator}. This is a
     * percentage modifier that alters the movement speed returned in
     * {@link #speed()}.
     * 
     * @param percent
     *            The new speed modifier
     */
    public NavigatorParameters speedModifier(float percent) {
        speedModifier = percent;
        return this;
    }
}
