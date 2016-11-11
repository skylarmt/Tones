/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.skylarismy;

import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author skylar
 */
public class Player extends Thread {

    private final BellNote[] song;
    private final Note myNote;
    private final Conductor con;
    private final SourceDataLine line;

    public Player(Note n, BellNote[] music, SourceDataLine l, Conductor c) {
        song = music;
        myNote = n;
        con = c;
        line = l;
        //System.out.println(this.getName() + " assigned note: " + n.toString());
    }

    @Override
    public void run() {
        while (con.currentNote < song.length) {
            BellNote bn = song[con.currentNote];
            if (bn.note == myNote) {
                //System.out.println(bn.note.name() + "\t" + this.getName());
                con.playNote(bn, line);
                con.currentNote++;
                synchronized (con.line) {
                    con.line.notifyAll();
                }
            } else {
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
