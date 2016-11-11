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
    private final Conductor con;

    /**
     * Create a new Player.
     *
     * @param n The note to assign
     * @param music The music
     * @param c The conductor
     */
    public Player(Note n, BellNote[] music, Conductor c) {
        song = music;
        myNote = n;
        con = c;
        //System.out.println(this.getName() + " assigned note: " + n.toString());
    }

    @Override
    public void run() {
        while (con.currentNote < song.length) {
            // Get current note
            BellNote bn = song[con.currentNote];

            // Is it my note?
            if (bn.note == myNote) {
                // Yes it is, let's play it
                //System.out.println(bn.note.name() + "\t" + this.getName());
                con.playNote(bn);
                con.currentNote++;
                // Tell everyone something happened
                synchronized (con.line) {
                    con.line.notifyAll();
                }
            } else {
                // No it isn't, let's wait until something happens
                synchronized (con.line) {
                    try {
                        con.line.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }
}
