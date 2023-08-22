package fightsummary;


import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.server.Server;
import necesse.entity.mobs.Mob;
import net.bytebuddy.asm.Advice;

import java.util.LinkedList;
import java.util.Map;

@ModMethodPatch(target = Server.class, name = "tick", arguments = {})
public class ServerTickPatch {

    // This is basically if a boss is removed in some other way than losing health, we need to track those

    @Advice.OnMethodExit
    static void onExit(@Advice.This Server server) {
        // Only need to check once a second
        if (server.tickManager().isFirstGameTickInSecond()) {
            // To avoid concurrency modification exception
            LinkedList<Mob> removed = new LinkedList<>();
            for (Map.Entry<Integer, MobDamageSummary> e : BossFightSummary.trackers.entrySet()) {
                if (e.getValue().mob.removed()) {
                    removed.add(e.getValue().mob);
                }
            }
            for (Mob mob : removed) {
                BossFightSummary.onFightDone(mob);
            }
        }
    }

}
