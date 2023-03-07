package ai_csp;


// A representation of a location on the board for java collections.
public class Location {

    // Instance creator from string.
    public static Location fromString(String locationString) {
        // This is just used in tests, to convert output into objects.
        String[] numbers = locationString.split(",");
        return new Location(
                Integer.parseInt(numbers[0].substring(1)),
                Integer.parseInt(numbers[1].substring(0, numbers[1].length() - 1))
        );
    }

    private final int x;
    private final int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

    @Override
    public boolean equals(Object o) {
        Location other = (Location)o;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return this.x * 31 + this.y;
    }
}
