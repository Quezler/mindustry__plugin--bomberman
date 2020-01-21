package bomberman;

import mindustry.content.*;
import mindustry.entities.type.*;
import mindustry.type.*;
import mindustry.world.*;



public enum Powerup{
    copper    (Mechs.alpha, Slate.State.copper    ,  7),
    titanium  (Mechs.delta, Slate.State.titanium  , 14),
    plastanium(Mechs.tau  , Slate.State.plastanium, 12),
    surge     (Mechs.omega, Slate.State.surge     , 20);

    public final Mech mech;
    public final Slate.State slate;
    public final int thorium;

    Powerup(Mech mech, Slate.State block, int thorium){
        this.mech = mech;
        this.slate = block;
        this.thorium = thorium;
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

        return null;
    }
}
