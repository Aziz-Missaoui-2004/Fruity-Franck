package frank.bots;

import engine.entities.Bot;
import engine.entities.Entity;
import engine.entities.Stunt;
import engine.geometry.ISU;
import frank.entities.Frank;
import frank.game.FruityFrankModel;
import oop.graphics.VirtualKeyCodes;
import engine.entities.BasicStunt;

public class FrankBot extends Bot {

    private final double speed;

    public FrankBot(Entity entity, double speed) {
        super(entity);
        this.speed = speed;
    }

    @Override
    public void pressed(int keyCode, char keyChar) {
        if (entity == null)
            return;

        FruityFrankModel m = (FruityFrankModel) entity.model();

        if (keyCode == VirtualKeyCodes.VK_SPACE) {
            if (entity instanceof Frank frank)
                if (frank.hasCherry())
                    m.throwCherryInternal(frank);
            return;
        }

        Stunt stunt = stunt();
        if (stunt == null)
            return;

        double dvx = 0;
        double dvy = 0;

        switch (keyCode) {
            case VirtualKeyCodes.VK_RIGHT -> dvx = speed;
            case VirtualKeyCodes.VK_LEFT -> dvx = -speed;
            case VirtualKeyCodes.VK_UP -> dvy = -speed;
            case VirtualKeyCodes.VK_DOWN -> dvy = speed;
            default -> {
                return;
            }
        }

        // Mémorise la direction pressée (sert au tir de cerise et à l'orientation).
        if (entity instanceof Frank f)
            f.setFacing((int) Math.signum(dvx), (int) Math.signum(dvy));

        ISU.Vector current = stunt.velocity();
        boolean moving = current != null && (current.x() != 0 || current.y() != 0);
        boolean perpendicular = moving && (current.x() != 0) != (dvx != 0);

        if (perpendicular)
            stunt.request(dvx, dvy);
        else
            stunt.drive(dvx, dvy);
    }

    @Override
    public void released(int keyCode, char keyChar) {
        Stunt stunt = stunt();
        if (stunt == null) return;

        switch (keyCode) {
            case VirtualKeyCodes.VK_RIGHT,
                 VirtualKeyCodes.VK_LEFT,
                 VirtualKeyCodes.VK_UP,
                 VirtualKeyCodes.VK_DOWN -> {
                // Request to stop at the next cell centre
                if (stunt instanceof BasicStunt basicStunt) {
                    basicStunt.requestStopAtNextCell();
                }
            }
        }
    }
}