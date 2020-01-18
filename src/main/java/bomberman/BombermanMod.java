package bomberman;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.plugin.*;
import mindustry.game.EventType.*;
import mindustry.core.GameState.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

import static mindustry.Vars.*;
import static arc.util.Log.info;

public class BombermanMod extends Plugin{
    private final Rules rules = new Rules();

    private BombermanGenerator generator;

    @Override
    public void init(){
        info("senpai bomberman <3");
        rules.tags.put("bomberman", "true");
        rules.infiniteResources = true;
        rules.canGameOver = false;

        Events.on(PlayerJoin.class, event -> {
            if(!active()) return;

            event.player.kill();
            event.player.setTeam(Team.sharded);
            event.player.dead = false;
            event.player.set(5, 5);
            event.player.mech = Powerup.yellow.mech;
            event.player.heal();
        });

        // fixme: break event does not include destroyed block
        Events.on(BlockBuildEndEvent.class, event -> {
            if(event.breaking) return; // fixme: invert ^

            Powerup tmp = Powerup.wall(event.tile.block());
            if(tmp == null) return;

            Log.info(event.player.mech);
            event.player.mech = tmp.mech;
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            if(event.breaking) return;

            if(event.tile.block() != Blocks.thoriumReactor) return;

            Call.transferItemTo(Items.thorium, 7, event.player.x, event.player.y, event.tile);
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if(event.tile.block() != Blocks.thoriumReactor) return;
            generator.breakable.each(tile -> {
                if(tile.block() != Blocks.scrapWallHuge) return;
                if(event.tile.x == tile.x || event.tile.y == tile.y){
                    tile.removeNet();
                    tile.getLinkedTilesAs(Blocks.scrapWallLarge, new Array<>()).each(Fire::create);
                }
            });
        });

        netServer.assigner = (player, players) -> Team.sharded;
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("bomberman", "Begin hosting with the Bomberman gamemode.", args -> {
            logic.reset();
            Log.info("Generating map...");
            world.loadGenerator(generator = new BombermanGenerator());
            info("Map generated.");
            state.rules = rules.copy();
            logic.play();
            netServer.openServer();
        });
    }

    public boolean active(){
        return state.rules.tags.getBool("bomberman") && !state.is(State.menu);
    }

    enum Powerup{
        yellow(Mechs.alpha, Blocks.copperWall),
        blue  (Mechs.delta, Blocks.titaniumWall),
        green (Mechs.tau,   Blocks.plastaniumWall),
        orange(Mechs.omega, Blocks.surgeWall);


        public final Mech mech;
        public final Block block;

        Powerup(Mech mech, Block block){
            this.mech = mech;
            this.block = block;
        }

        public static Powerup wall(Block block){
            for(Powerup powerup : values()){
                if(powerup.block == block) return powerup;
            }

            return null;
        }
    }
}
