package fightsummary;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = Mob.class, name = "setHealthHidden", arguments = {int.class, float.class, float.class, Attacker.class, boolean.class})
public class MobHealthPatch {

    @Advice.OnMethodEnter()
    static void onEnter(@Advice.This Mob mob, @Advice.Local("beforeHealth") int beforeHealth) {
        // Only count bosses
        if (mob.isBoss()) {
            beforeHealth = mob.getHealth();
        }
    }

    @Advice.OnMethodExit
    static void onExit(@Advice.This Mob mob, @Advice.Argument(3) Attacker attacker, @Advice.Local("beforeHealth") int beforeHealth) {
        // Mobs can set their health before being assigned a level
        if (mob.getLevel() != null && mob.isBoss() && mob.shouldSendSpawnPacket()) {
            if (mob.isServer()) {
                int damage = beforeHealth - mob.getHealth();
                if (damage > 0) {
                    BossFightSummary.onMobDamaged(mob, damage, attacker);
                }
                // Some bosses reset if all players die, and their health is set to max again
                // If this happens, we should display the damage dealt for that fight
                // Or if the health is 0, it means they are dead
                if (mob.getHealth() >= mob.getMaxHealth() || mob.getHealth() <= 0) {
                    BossFightSummary.onFightDone(mob);
                }
            }
        }
    }

}
