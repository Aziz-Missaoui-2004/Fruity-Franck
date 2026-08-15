package frank.entities;

import engine.entities.BasicStunt;
import engine.entities.Entity;

public class EnemyStunt extends BasicStunt {
    private final Entity owner;
    public EnemyStunt(Entity owner) { super(owner); this.owner = owner; }

    @Override
    public void drive(double vx, double vy) {
        var v = owner.getLinearSpeed();
        boolean immobile = (v == null) || (v.x() == 0 && v.y() == 0);
        if (immobile)
            super.drive(vx, vy);   // démarrage immédiat (sinon figé au spawn)
        else
            request(vx, vy);       // sinon : tourne seulement au prochain croisement
    }
}