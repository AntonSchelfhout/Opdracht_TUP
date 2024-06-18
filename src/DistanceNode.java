class DistanceNode implements Comparable<DistanceNode> {
    public int distance;
    public Match match;

    public DistanceNode(int distance, Match match) {
        this.distance = distance;
        this.match = match;
    }

    @Override
    public int compareTo(DistanceNode other) {
        return Integer.compare(this.distance, other.distance);
    }
}
