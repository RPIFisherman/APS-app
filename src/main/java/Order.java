// Helper class for Orders
package ygong.APS;

public class Order {
    private final String name;
    private final String machine;
    private final int start;
    private final int end;

    public Order(String name, String machine, int start, int end) {
        this.name = name;
        this.machine = machine;
        this.start = start;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public String getMachine() {
        return machine;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getDuration() {
        return end - start;
    }
}