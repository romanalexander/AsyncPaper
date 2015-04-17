package org.github.paperspigot;

import java.util.LinkedList;

public class PaperPhaserProvider {
    private LinkedList<PaperPhaser> phaserList = new LinkedList<>();
    private boolean isTerminated = false;

    public PaperPhaserProvider() {
        phaserList.add(new PaperPhaser(1));
    }

    public synchronized PaperPhaser get() {
        if (isTerminated) {
            throw new IllegalStateException("PhaserProvider is already terminated.");
        }
        PaperPhaser phaser = phaserList.getLast();
        if (phaser.getRegisteredParties() > 60000) {
            phaser = new PaperPhaser(1);
            phaserList.add(phaser);
        }
        return phaser;
    }

    public synchronized PaperPhaser getAndRegister() {
        PaperPhaser retr = get();
        retr.register();
        return retr;
    }

    public synchronized void await() {
        for (PaperPhaser paperPhaser : phaserList) {
            paperPhaser.arriveAndAwaitAdvance();
        }
        isTerminated = true;
    }

    public synchronized void arrive() {
        for (PaperPhaser paperPhaser : phaserList) {
            paperPhaser.arrive();
        }
        isTerminated = true;
    }

}
