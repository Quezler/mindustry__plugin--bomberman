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

                // touching special tiles
                Cooties.handle(player, on);

                // walls
                if(!on.state.flyable()){
                    player.applyEffect(StatusEffects.freezing, 60f);
                    player.applyEffect(StatusEffects.tarred, 60f);
                    player.damage(2.5f);
                }

                // bombs
                if(player.isBoosting){
                    if(bombs(player) >= Powerup.player(player).reactors) continue; // out of bombs

                    if(on.state == Slate.State.empty){
                        on.state = Slate.State.bomb;
                        Call.onConstructFinish(on.center(), Blocks.thoriumReactor, player.id, (byte)0, cake, true);

                        Timer.schedule(() -> Call.transferItemTo(Items.thorium, Powerup.player(player).thorium, player.x, player.y, on.center()), 0.25f);

                        nukes.put(on.center(), player);
                    }
                }
            }

            // end the game if there is only one alive left
            if(playerGroup.count(p -> !p.isDead()) <= 0) stage.set(gameover);
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
