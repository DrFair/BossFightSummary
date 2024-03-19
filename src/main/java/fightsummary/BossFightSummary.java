package fightsummary;

import fightsummary.handlers.DefaultTrackerHandler;
import fightsummary.handlers.NightSwarmTrackerHandler;
import fightsummary.handlers.TrackerHandler;
import necesse.engine.GameEventListener;
import necesse.engine.GameEvents;
import necesse.engine.events.ServerStartEvent;
import necesse.engine.events.ServerStopEvent;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.server.Server;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.hostile.bosses.NightSwarmBatMob;

import java.util.HashMap;

@ModEntry
public class BossFightSummary {

    public static Server currentServer;
    public static HashMap<Integer, MobDamageSummary> trackers = new HashMap<>();

    public static TrackerHandler<Mob> defaultHandler = new DefaultTrackerHandler();

    public static HashMap<Class<? extends Mob>, TrackerHandler<?>> extraMobs = new HashMap<>();
    static {
        extraMobs.put(NightSwarmBatMob.class, new NightSwarmTrackerHandler());
    }

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

    public static boolean shouldHandleMob(Mob mob) {
        return mob.isBoss() || extraMobs.containsKey(mob.getClass());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onMobDamaged(Server server, Mob mob, int damage, Attacker attacker) {
        TrackerHandler handler = extraMobs.getOrDefault(mob.getClass(), defaultHandler);
        int uniqueID = handler.getUniqueID(mob);
        MobDamageSummary summary = trackers.get(uniqueID);
        if (summary == null) {
            trackers.put(uniqueID, summary = new MobDamageSummary(server, handler.getTracker(mob)));
        }
        summary.applyDamage(damage, attacker);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onFightDone(Mob mob) {
        TrackerHandler handler = extraMobs.getOrDefault(mob.getClass(), defaultHandler);
        int uniqueID = handler.getUniqueID(mob);
        // Remove the mob from trackers and display the summary
        MobDamageSummary summary = trackers.get(uniqueID);
        if (summary != null && currentServer != null && summary.tracker.isFightDone()) {
            summary.displaySummary(currentServer);
            trackers.remove(uniqueID);
        }
    }

}
