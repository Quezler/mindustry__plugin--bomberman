package bomberman;

import mindustry.content.*;
import mindustry.entities.type.*;
import mindustry.type.*;
import mindustry.world.*;



public enum Powerup{
    copper    (Mechs.alpha, Slate.State.copper    ,  7, 1),
    titanium  (Mechs.delta, Slate.State.titanium  , 14, 2),
    plastanium(Mechs.tau  , Slate.State.plastanium, 12, 1),
    surge     (Mechs.omega, Slate.State.surge     , 20, 3);

    public final Mech mech;
    public final Slate.State slate;
    public final int thorium;
    public final int breaks;

    public static final Powerup starter = copper;

    Powerup(Mech mech, Slate.State block, int thorium, int breaks){
        this.mech = mech;
        this.slate = block;
        this.thorium = thorium;
        this.breaks = breaks;
    }

    public static Powerup wall(Block block){
        for(Powerup powerup : values()){
            if(powerup.slate.block == block) return powerup;
        }

        return null;
    }

    public static Powerup player(Player player){
        for(Powerup powerup : values()){
            if(powerup.mech == player.mech) return powerup;
        }

        return starter;
    }
}
