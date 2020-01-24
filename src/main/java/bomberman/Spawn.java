package bomberman;

import arc.math.geom.*;

public class Spawn implements Position{
    private float x;
    private float y;

    Spawn(float x, float y){
        this.x = x;
        this.y = y;
    }

    @Override
    public float getX(){
        return x;
    }

    @Override
    public float getY(){
        return y;
    }

    @Override
    public String toString(){
        return x +":"+ y;
    }
}
