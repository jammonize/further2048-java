package ai;

public enum Direction {
    UP(270), DOWN(90), LEFT(0), RIGHT(180);

    public final int degrees;
    
    Direction(int degrees) {
        this.degrees = degrees;
    }
}