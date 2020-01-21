package bomberman;

import arc.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.type.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.plugin.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.core.GameState.*;

import static bomberman.Bomberman.*;
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
                if(tmp.state == Slate.State.copper || tmp.state == Slate.State.titanium || tmp.state == Slate.State.plastanium || tmp.state == Slate.State.surge){

                    Powerup tmp2 = Powerup.wall( tmp.center().block() );
                    if(tmp2 == null) return;
                    p.mech = tmp2.mech;
                    p.heal();

                    Call.onConstructFinish(tmp.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
                    tmp.state = Slate.State.empty;
                }

                if(tmp.state == Slate.State.wall || tmp.state == Slate.State.scrap){
//                    Log.info("wall");
//                    p.velocity().y = 10f;
//                    Bullet.create(Bullets.waterShot, p, tmp.center().drawx(), tmp.center().drawy(), 90f);
                    p.applyEffect(StatusEffects.freezing, 60f);
                    p.damage(1f);
                }
            }
        });

        // construct reactor
        Events.on(BlockBuildEndEvent.class, event -> {
            if(event.breaking) return;

            if(event.tile.block() == Blocks.thoriumReactor) {
                Powerup tmp = Powerup.player(event.player);
                if(tmp == null) return;
                Timer.schedule(() -> Call.transferItemTo(Items.thorium, tmp.thorium, event.player.x, event.player.y, event.tile), 0.5f);
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

        //what does this do
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
}
