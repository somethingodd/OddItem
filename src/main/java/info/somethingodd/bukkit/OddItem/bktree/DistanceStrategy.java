package info.somethingodd.bukkit.OddItem.bktree;

/**
 * A method of calculating the distance between two strings.
 */
public interface DistanceStrategy<E> {
    public int distance(E left, E right);
}
