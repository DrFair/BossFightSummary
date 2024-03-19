package fightsummary;

import fightsummary.handlers.MobTracker;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MobDamageSummary {

    public final long fightStartTime;
    public float minHealthPercent = 1;

    public MobTracker tracker;

    public HashMap<Long, Long> authDamages = new HashMap<>();

    public MobDamageSummary(Server server, MobTracker tracker) {
        this.fightStartTime = server.world.worldEntity.getTime();
        this.tracker = tracker;
    }

    public void applyDamage(int damage, Attacker attacker) {
        minHealthPercent = Math.min(minHealthPercent, tracker.getHealthPercent());
        // Debug message
//        String attackerName = attacker == null ? "N/A" : attacker.getAttackerName().translate();
//        System.out.println("FIGHT SUMMARY: DAMAGED " + mob + ":" + damage + " - " + attackerName);
        if (attacker != null) {
            for (Mob owner : attacker.getAttackOwners()) {
                if (owner.isPlayer) {
                    PlayerMob player = (PlayerMob) owner;
                    if (player.isServerClient()) { // Should always be true, but just in case other mods adds some weird stuff
                        ServerClient client = player.getServerClient();
                        // Add the damage to the auth
                        authDamages.compute(client.authentication, (auth, totalDamage) -> {
                            if (totalDamage == null) totalDamage = 0L;
                            return totalDamage + damage;
                        });
                    }
                }
            }
        }
    }

    public void displaySummary(Server server) {
        // We don't show fight summary if the boss didn't go below 99% hp
        if (minHealthPercent > 0.99) return;
        // Keep track of the clients we need to notify
        LinkedList<ServerClient> clients = new LinkedList<>();
        LinkedList<GameMessage> messages = new LinkedList<>();
        messages.add(new LocalMessage("fightsummary", "header", "mob", tracker.getLocalization()));
        long currentTime = server.world.worldEntity.getTime();
        long elapsedTime = currentTime - fightStartTime;
        String timeString = GameUtils.getTimeStringMillis(elapsedTime);
        if (tracker.isDead()) {
            messages.add(new LocalMessage("fightsummary", "successful", "time", timeString));
        } else {
            String minHealthPercentString = ((int) (minHealthPercent * 1000) / 10f) + "%";
            messages.add(new LocalMessage("fightsummary", "unsuccessful", "time", timeString, "percent", minHealthPercentString));
        }

        for (Map.Entry<Long, Long> e : authDamages.entrySet()) {
            long auth = e.getKey();
            long damage = e.getValue();
            String clientName;
            ServerClient client = server.getClientByAuth(auth);
            if (client != null) {
                clients.add(client);
                clientName = client.getName();
            } else {
                clientName = server.usedNames.get(auth);
            }

            String damageText = GameUtils.formatNumber(damage);
            messages.add(new LocalMessage("fightsummary", "client", "name", clientName, "damage", damageText));
        }

        for (ServerClient client : clients) {
            for (GameMessage message : messages) {
                // We send the translated message, since it's possible the client does not have
                // the proper translation keys
                client.sendChatMessage(message.translate());
            }
        }
    }
}
