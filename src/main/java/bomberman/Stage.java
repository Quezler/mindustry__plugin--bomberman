package bomberman;

import arc.util.*;
import mindustry.content.*;
import mindustry.entities.type.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;

import static bomberman.Bomberman.*;
import static mindustry.Vars.*;

public class Stage{
    private static final float toast = 2f;
    public static final UnitState

    waiting = new UnitState(){
        public void entered(){
        }

        public void exited(){

        }

        public void update(){
            // reset the map when there are no alive players
            if(playerGroup.size() > 0 && playerGroup.count(p -> !p.isDead()) == 0) stage.set(resetting);
        }
    },

    resetting = new UnitState(){
        public void entered(){
            Timer.schedule(() -> BombermanMod.reset(() -> stage.set(playing)), 1.5f);
        }
    },

    playing = new UnitState(){
        public void update(){

            for(Player player : playerGroup){
                if(!Structs.contains(teams, player.getTeam())) continue; // not in an alive team

                Slate on = slate(tile(player));

                // powerups
                if(on.state.powerup()){
                    Powerup tmp2 = Powerup.wall(on.center().block());
                    if(tmp2 == null) return;
                    player.mech = tmp2.mech;
                    player.heal();
                    Call.onConstructFinish(on.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
                    on.state = Slate.State.empty;
                }

                // illuminators
                if(on.state == Slate.State.pyroland){
                    pallete = Structs.random(Pallete.values());

                    for(int x = 0; x < world.width(); x++){
                        for(int y = 0; y < world.height(); y++){
                            world.getTiles()[x][y].setFloor(pallete.floor);
                        }
                    }

                    slates(slate -> {
                        if(slate.center().block() != Blocks.thoriumReactor) slate.place();
                    });

                    playerGroup.all().each(syncer -> netServer.clientCommands.handleMessage("/sync", syncer));
                    Call.onConstructFinish(on.center(), Blocks.air, -1, (byte)0, Team.derelict, true);
                    on.state = Slate.State.empty;
                }

                // walls
                if(!on.state.flyable()){
                    player.applyEffect(StatusEffects.freezing, 60f);
                    player.applyEffect(StatusEffects.tarred, 60f);
                    player.damage(2.5f);
                }

                // bombs
                if(player.isBoosting){

                    if(bombs.get(player.getTeam(), 0) >= bombs(player.getTeam())) continue;

                    if(on.state == Slate.State.empty){
                        on.state = Slate.State.bomb;
                        Call.onConstructFinish(on.center(), Blocks.thoriumReactor, player.id, (byte)0, player.getTeam(), true);

                        Timer.schedule(() -> Call.transferItemTo(Items.thorium, Powerup.player(player).thorium, player.x, player.y, on.center()), 0.25f);

                        bombs.getAndIncrement(player.getTeam(), 0, 1);
                        nukes.put(on.center(), player);
                    }
                }
            }

            // end the game if there is only one alive left
            if(playerGroup.count(p -> !p.isDead()) <= 1) stage.set(gameover);
        }
    },

    gameover = new UnitState(){
        public void entered(){
            Call.onInfoToast("gameover", toast);
        }

        public void update(){
            // slowly kill the last remaining player(s)
            playerGroup.all().select(p -> !p.isDead()).each(p -> p.applyEffect(StatusEffects.corroded, 60f));

            // reset the map when there are no alive players
            if(playerGroup.size() > 0 && playerGroup.count(p -> !p.isDead()) == 0) stage.set(resetting);
        }
    };
}
