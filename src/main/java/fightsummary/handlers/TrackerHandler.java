package fightsummary.handlers;

import necesse.entity.mobs.Mob;

public interface TrackerHandler<T extends Mob> {

    int getUniqueID(T mob);

    MobTracker getTracker(T mob);

}
