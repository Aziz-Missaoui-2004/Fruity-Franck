package frank.game;

import java.io.File;
import oop.utils.SoundPlayer;


public class FrankAudio {

    private final SoundPlayer music;
    private final SoundPlayer eat;       // manger fruit / cerise
    private final SoundPlayer death;     // Frank perd une vie
    private final SoundPlayer explosion; // monstre détruit
    private final SoundPlayer shoot;     // tir du projectile
    private final SoundPlayer win;       // victoire
    private final SoundPlayer gameOver;

    public FrankAudio() {
        music     = load("sprites/jon.wav");
        eat       = load("sprites/eat.wav");
        death     = load("sprites/death.wav");
        explosion = load("sprites/explosion.wav");
        shoot     = load("sprites/gun-shot.wav");
        win       = load("sprites/win.wav");
        gameOver  = load("sprites/game-over.wav");
        
    }

    private SoundPlayer load(String path) {
        File f = new File(path);
        if (!f.exists()) {
            System.err.println("[FrankAudio] fichier introuvable : " + f.getAbsolutePath());
            return null;
        }
        try {
            return new SoundPlayer(f);
        } catch (Exception e) {
            System.err.println("[FrankAudio] échec chargement " + path + " : " + e.getMessage());
            return null;
        }
    }

    public void startMusic() { if (music != null)     music.play(0); } 
    public void stopMusic()  { if (music != null)     music.stop(); }
    public void eat()       { play(eat); }
    public void death()     { play(death); }
    public void explosion() { play(explosion); }
    public void shoot()     { play(shoot); }
    public void win()       { play(win); }
    public void gameOver()  { play(gameOver); }

    private void play(SoundPlayer s) { if (s != null) s.play(1); }

}