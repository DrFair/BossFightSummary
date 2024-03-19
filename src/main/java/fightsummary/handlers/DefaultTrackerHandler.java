package fightsummary.handlers;

import necesse.engine.localization.message.GameMessage;
import necesse.entity.mobs.Mob;

public class DefaultTrackerHandler implements TrackerHandler<Mob> {
    @Override
    public int getUniqueID(Mob mob) {
        return mob.getUniqueID();
    }

    @Override
    public MobTracker getTracker(Mob mob) {
        return new MobTracker() {
            @Override
            public float getHealthPercent() {
                return mob.getHealthPercent();
            }

            @Override
            public GameMessage getLocalization() {
                return mob.getLocalization();
            }

            @Override
            public boolean isFightDone() {
                return mob.removed();
            }

            @Override
            public boolean isDead() {
                return mob.getHealth() <= 0 || mob.hasDied();
            }
        };
    }
}
