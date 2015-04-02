package org.github.paperspigot;

import java.util.concurrent.Phaser;

public class PaperPhaser extends Phaser {
    public PaperPhaser() {
    }

    public PaperPhaser(int parties) {
        super(parties);
    }

    public PaperPhaser(Phaser parent) {
        super(parent);
    }

    public PaperPhaser(Phaser parent, int parties) {
        super(parent, parties);
    }

    @Override
    protected boolean onAdvance(int phase, int registeredParties) {
        return false;
    }
}
