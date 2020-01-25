package bomberman;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import bomberman.Slate.*;
import mindustry.content.*;
import mindustry.entities.traits.*;
import mindustry.entities.type.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;

import static bomberman.Bomberman.*;
import static mindustry.Vars.*;

public class Cooties{

    private static final ObjectMap<Block, Cons2<Player, Slate>> map = new ObjectMap<Block, Cons2<Player, Slate>>(){{

        put(State.copper.block.get()    , (player, slate) -> powerup(Powerup.copper    , player));
        put(State.titanium.block.get()  , (player, slate) -> powerup(Powerup.titanium  , player));
        put(State.plastanium.block.get(), (player, slate) -> powerup(Powerup.plastanium, player));
        put(State.surge.block.get()     , (player, slate) -> powerup(Powerup.surge     , player));

        put(State.pyroland.block.get()  , (player, slate) -> pallete());
        put(State.healing.block.get()   , (player, slate) -> player.heal());
        put(State.door.block.get()      , (player, slate) -> {
            for(Direction direction : Direction.values()){
                if(slate.adjecent(direction).state == State.scrap){
                    slate.adjecent(direction).state = State.empty;
                    slate.adjecent(direction).destroy();
                }
            }
        });
    }};

    public static void handle(Player player, Slate slate){

        if(map.get(slate.center().block()) == null) return;
        map.get(slate.center().block()).get(player, slate);

        Call.onConstructFinish(slate.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
        slate.state = Slate.State.empty;
    }

    private static void powerup(Powerup powerup, Player player){
        player.mech = powerup.mech;
        player.heal();
    }

    private static void pallete(){
        pallete = Structs.random(Pallete.values());

        for(int x = 0; x < world.width(); x++){
            for(int y = 0; y < world.height(); y++){
                world.getTiles()[x][y].setFloor(pallete.floor);
            }
        }

        slates(slate -> {
            if(slate.state != State.bomb) slate.place();
        });

        playerGroup.all().each(syncer -> netServer.clientCommands.handleMessage("/sync", syncer));
    }
}
