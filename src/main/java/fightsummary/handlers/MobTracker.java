package fightsummary.handlers;

import necesse.engine.localization.message.GameMessage;

public interface MobTracker {

    float getHealthPercent();

    GameMessage getLocalization();

    boolean isFightDone();

    boolean isDead();

}
