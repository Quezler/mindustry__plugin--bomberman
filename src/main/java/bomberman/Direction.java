package bomberman;

public enum Direction{
    up   ( 0, 1),
    right( 1, 0),
    down ( 0,-1),
    left (-1, 0);

    public int x;
    public int y;

    Direction(int x, int y){
        this.x = x;
        this.y = y;
    }
}
