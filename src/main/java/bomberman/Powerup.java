package bomberman;

import mindustry.content.*;
import mindustry.entities.type.*;
import mindustry.type.*;
import mindustry.world.*;



public enum Powerup{
    copper    (Mechs.alpha, Slate.State.copper    ,  7, 1, 2), // default, any other upgrade is better
    titanium  (Mechs.delta, Slate.State.titanium  , 14, 2, 2), // faster & more powerful explosions
    plastanium(Mechs.tau  , Slate.State.plastanium, 10, 1, 3), // fast & more overall explosions
    surge     (Mechs.omega, Slate.State.surge     , 20, 3, 2); // fastest & most powerful explosions

    public final Mech mech;
    public final Slate.State slate;
    public final int thorium;
    public final int breaks;
    public final int reactors;

    public static final Powerup starter = copper;

    Powerup(Mech mech, Slate.State block, int thorium, int breaks, int reactors){
        this.mech = mech;
        this.slate = block;
        this.thorium = thorium;
        this.breaks = breaks;
        this.reactors = reactors;
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
