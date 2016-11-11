/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.skylarismy;

/**
 *
 * @author skylar
 */
public class Player extends Thread {

    private final BellNote[] song;
    private final Note myNote;

    public Player(Note n, BellNote[] music) {
        song = music;
        myNote = n;
        System.out.println(this.getName() + " assigned note: " + n.toString());
    }

    @Override
    public void run() {
        while (Conductor.currentNote < song.length) {
            BellNote bn = song[Conductor.currentNote];
            if (bn.note == myNote) {
                System.out.println(bn.note.name() + "\t" + this.getName());
                Conductor.playNote(bn);
                Conductor.currentNote++;
                synchronized (Conductor.line) {
                    Conductor.line.notifyAll();
                }
            } else {
                synchronized (Conductor.line) {
                    try {
                        Conductor.line.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }
}
