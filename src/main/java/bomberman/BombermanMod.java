package bomberman;

import arc.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.entities.effect.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.plugin.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.BuildBlock.*;

import static arc.util.Log.info;
import static bomberman.Bomberman.*;
import static bomberman.BombermanGenerator.pallete;
import static mindustry.Vars.*;

public class BombermanMod extends Plugin{
    private final Rules rules = new Rules();

    private BombermanGenerator generator;

    @Override
    public void init(){
        info("senpai bomberman <3");
        rules.tags.put("bomberman", "true");
        rules.infiniteResources = true;
        rules.canGameOver = false;

        //Todo: check for min 2 players and have a countdown (~ 10 seconds)
        //if game is already running -> spectator mode
        Events.on(PlayerJoin.class, event -> {
            if(!active()) return;

            event.player.kill();
            event.player.setTeam(Team.sharded);
            event.player.dead = false;

            //set location
            Call.onPositionSet(event.player.con, generator.spawns[0][0]*8, generator.spawns[0][1]*8);
            event.player.setNet(generator.spawns[0][0]*8, generator.spawns[0][1]*8);
            event.player.set(generator.spawns[0][0]*8, generator.spawns[0][1]*8);

            event.player.mech = Powerup.copper.mech;
            event.player.heal();
        });

        //block flying over walls
        Events.on(Trigger.update, () -> {
            for (Player p: playerGroup){
                if (p.getTeam() != Team.sharded) continue;
                if (world.tile(p.tileX(), p.tileY()) != null && world.tile(p.tileX(), p.tileY()).block() != Blocks.air){
//                    Call.sendMessage("[scarlet]FLYING OVER WALLS == CHEATING\ndisqualified!");
//                    p.setTeam(Team.green);
//                    Call.onPlayerDeath(p);
                }

                Slate tmp = slate(tile(p));

                // player is on the same tile as a powerup
                if(tmp.state.powerup()){

                    Powerup tmp2 = Powerup.wall( tmp.center().block() );
                    if(tmp2 == null) return;
                    p.mech = tmp2.mech;
                    p.heal();

                    Call.onConstructFinish(tmp.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
                    tmp.state = Slate.State.empty;
                }

                if(!tmp.state.flyable()){
                    p.applyEffect(StatusEffects.freezing, 60f);
                    p.applyEffect(StatusEffects.tarred, 60f);
                    p.damage(2.5f);
                }

                if(p.isBoosting){
                    Slate over = slate(tile(p));

                    if(bombs.get(p.getTeam(), 0) >= 2) return; // 2 bombs per team max

                    if(over.state == Slate.State.empty){
                        over.state = Slate.State.bomb;
                        Call.onConstructFinish(over.center(), Blocks.thoriumReactor, p.id, (byte)0, p.getTeam(), true);

                        Powerup tmp3 = Powerup.player(p);
                        if(tmp3 != null) Timer.schedule(() -> Call.transferItemTo(Items.thorium, tmp3.thorium, p.x, p.y, over.center()), 0.25f);

                        bombs.getAndIncrement(p.getTeam(), 0, 1);
                    }
                }
            }
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            if(!event.breaking) return;

            if(event.tile.block() instanceof BuildBlock && ((BuildEntity)event.tile.ent()).previous == pallete.blockade) {
                slate(event.tile).state = Slate.State.empty;
            }
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if(event.tile.block() != Blocks.thoriumReactor) return;

            bombs.getAndIncrement(event.tile.getTeam(), 0, -1);
            Slate reactor = slate(event.tile);
            reactor.compass(Fire::create);
            reactor.state = Slate.State.empty;

            Slate tmp;
            for(Direction direction : Direction.values()){
                tmp = reactor.adjecent(direction);
                do{
                    if (tmp.state == Slate.State.wall) break;
                    if (tmp.state == Slate.State.empty) tmp.compass(Fire::create);

                    if (tmp.state == Slate.State.scrap){
                        omeowamoushindeiru(tmp);
                        tmp.compass(Fire::create);
                        tmp.state = Slate.State.empty;
                        break;
                    }

                    tmp = tmp.adjecent(direction);
                }while(true);
            }
        });

//        //destroys the walls
//        Events.on(BlockDestroyEvent.class, event -> {
//            if(event.tile.block() != Blocks.thoriumReactor) return;
//            int x = event.tile.x;
//            int y = event.tile.y;
//            //delete horizontal
//            boolean hdamage = false;
//            if (!(world.tile(x-3, y).block() == generator.pallete.wall && world.tile(x+3, y).block() == generator.pallete.wall)){
//                hdamage = true;
//                generator.breakable.each(tile -> {
//                    if(tile.block() != generator.pallete.blockade) return;
//                    if(event.tile.y == tile.y) {
//                        /*new method - not in servertestfile
//                        tile.removeNet();*/
//                        //alternative
//                        Time.run(0f, tile.entity::kill);
//                        //TODO: draw laser on airtiles
//                        tile.getLinkedTilesAs(Blocks.scrapWallLarge, new Array<>()).each(Fire::create);
//                    }
//                });
//            }
//            //delete vertical
//            boolean vdamage = false;
//            if (!(world.tile(x, y-3).block() == generator.pallete.wall && world.tile(x, y+3).block() == generator.pallete.wall)){
//                vdamage = true;
//                generator.breakable.each(tile -> {
//                    if(tile.block() != generator.pallete.blockade) return;
//                    if(event.tile.x == tile.x) {
//                        /*new method - not in servertestfile
//                        tile.removeNet();*/
//                        //alternative
//                        Time.run(0f, tile.entity::kill);
//                        tile.getLinkedTilesAs(Blocks.scrapWallLarge, new Array<>()).each(Fire::create);
//                    }
//                });
//            }
//            /*
//            generator.breakable.each(tile -> {
//
//                if(tile.block() != generator.pallete.blockade) return;
//                if(event.tile.x == tile.x || event.tile.y == tile.y){
//                    //new method - not in servertestfile
//                    //tile.removeNet();
//                    //alternative
//                    Time.run(0f, tile.entity::kill);
//                    tile.getLinkedTilesAs(Blocks.scrapWallLarge, new Array<>()).each(Fire::create);
//                }
//            });*/
//            //check if player was in laser/fire
//            for (Player p: playerGroup){
//                //already dead
//                if (p.getTeam() != Team.sharded) continue;
//                if (vdamage){
//                    if (x-1 <= p.tileX() && p.tileX() <= x+1){
//                        //kill player and put on green team
//                        Call.sendMessage('\n' + p.name + "[sky] DIED");
//                        p.setTeam(Team.green);
//                        Call.onPlayerDeath(p);
//                        continue;
//                    }
//                }
//                if (hdamage){
//                    if (y-1 <= p.tileY() && p.tileY() <= y+1){
//                        //kill player and put on green team
//                        Call.sendMessage(p.name + "[sky] DIED");
//                        p.setTeam(Team.green);
//                        Call.onPlayerDeath(p);
//                    }
//                }
//            }
//            //TODO: restart condition
//        });

        //what does this do - dunno, it was there in hexedmod.
        netServer.assigner = (player, players) -> Team.sharded;
    }

    private void omeowamoushindeiru(Slate slate){
        Core.app.post(() -> {
            slate.center().entity.onDeath();
            slate.center().removeNet();
        });
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
}
