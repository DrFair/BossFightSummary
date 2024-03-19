package fightsummary.handlers;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.nightSwarmEvent.NightSwarmLevelEvent;
import necesse.entity.mobs.LevelMob;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.hostile.bosses.NightSwarmBatMob;

public class NightSwarmTrackerHandler implements TrackerHandler<NightSwarmBatMob> {
    @Override
    public int getUniqueID(NightSwarmBatMob mob) {
        return mob.nightSwarmEventUniqueID == 0 ? mob.getUniqueID() : mob.nightSwarmEventUniqueID;
    }

    @Override
    public MobTracker getTracker(NightSwarmBatMob mob) {
        return new MobTracker() {
            @Override
            public float getHealthPercent() {
                LevelEvent event = mob.getLevel().entityManager.getLevelEvent(mob.nightSwarmEventUniqueID, true);
                if (event instanceof NightSwarmLevelEvent) {
                    return 1f - ((NightSwarmLevelEvent) event).lastHealthProgress;
                }
                return 1f;
            }

            @Override
            public GameMessage getLocalization() {
                return new LocalMessage("mob", "nightswarm");
            }

            @Override
            public boolean isFightDone() {
                LevelEvent event = mob.getLevel().entityManager.getLevelEvent(mob.nightSwarmEventUniqueID, true);
                return event == null || event.isOver();
            }

            @Override
            public boolean isDead() {
                LevelEvent event = mob.getLevel().entityManager.getLevelEvent(mob.nightSwarmEventUniqueID, true);
                if (event instanceof NightSwarmLevelEvent) {
                    boolean oneAlive = false;
                    for (LevelMob<NightSwarmBatMob> levelMob : ((NightSwarmLevelEvent) event).bats) {
                        Mob bat = mob.getLevel().entityManager.mobs.get(levelMob.uniqueID, true);
                        if (bat != null && bat.getHealth() > 0) {
                            oneAlive = true;
                            break;
                        }
                    }
                    NightSwarmLevelEvent swarmEvent = (NightSwarmLevelEvent) event;
                    return !oneAlive || swarmEvent.lastHealthProgress <= 0;
                }
                return false;
            }
        };
    }
}
