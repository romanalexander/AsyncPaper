package net.minecraft.server;

import org.github.paperspigot.ConcurrentHashSet;

// CraftBukkit - imported class because the constructor is package private
class BlockActionDataList extends ConcurrentHashSet {

    private BlockActionDataList() {
    }

    BlockActionDataList(BananaAPI bananaapi) {
        this();
    }
}
