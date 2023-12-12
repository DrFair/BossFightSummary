package fightsummary;

import necesse.engine.GameEventListener;
import necesse.engine.GameEvents;
import necesse.engine.events.ServerStartEvent;
import necesse.engine.events.ServerStopEvent;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.server.Server;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;

import java.util.HashMap;

@ModEntry
public class BossFightSummary {

    public static Server currentServer;
    public static HashMap<Integer, MobDamageSummary> trackers = new HashMap<>();

    public void init() {
        System.out.println("Loading Fight Summary mod!");

        // When a server starts, we initialize the static data
        GameEvents.addListener(ServerStartEvent.class, new GameEventListener<ServerStartEvent>() {
            @Override
            public void onEvent(ServerStartEvent e) {
                currentServer = e.server;
                trackers.clear();
                System.out.println("Fight Summary started tracking server on world " + e.server.world.displayName);
            }
        });

        // When a server stops, we dispose of the static data so that it gets garbage collected
        GameEvents.addListener(ServerStopEvent.class, new GameEventListener<ServerStopEvent>() {
            @Override
            public void onEvent(ServerStopEvent e) {
                currentServer = null;
                trackers.clear();
            }
        });

        System.out.println("Successfully loaded Fight Summary mod!");
    }

    public static void onMobDamaged(Mob mob, int damage, Attacker attacker) {
        MobDamageSummary summary = trackers.get(mob.getUniqueID());
        if (summary == null) trackers.put(mob.getUniqueID(), summary = new MobDamageSummary(mob));
        summary.applyDamage(damage, attacker);
    }

    public static void onFightDone(Mob mob) {
        // Remove the mob from trackers and display the summary
        MobDamageSummary summary = trackers.remove(mob.getUniqueID());
        if (summary != null && currentServer != null) {
            summary.displaySummary(currentServer);
        }
    }

}
