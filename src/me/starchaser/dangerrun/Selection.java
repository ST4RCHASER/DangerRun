package me.starchaser.dangerrun;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Selection {
    Double minX;
    Double maxX;

    Double minY;
    Double maxY;

    Double minZ;
    Double maxZ;

    World world;

    Location pos1;
    Location pos2;

    public Selection(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;

        if (pos1.getWorld() != pos2.getWorld())
            throw new IllegalArgumentException("pos2 must in the same world as pos1.");
        world = pos1.getWorld();
        this.recalculateValue();
    }

    Selection setPos1(Location _pos1){
        if (this.pos2.getWorld() != _pos1.getWorld())
            throw new IllegalArgumentException("pos1 must in the same world as pos2.");
        this.pos1 = _pos1;
        this.recalculateValue();
        return this;
    }

    Selection setPos2(Location _pos2){
        if (pos1.getWorld() != _pos2.getWorld())
            throw new IllegalArgumentException("pos2 must in the same world as pos1.");
        this.pos2 = _pos2;
        this.recalculateValue();
        return this;
    }

    private void recalculateValue() {
        minX = Math.min(this.pos2.getX(), this.pos1.getX());
        maxX = Math.max(this.pos2.getX(), this.pos1.getX());
        minY = Math.min(this.pos2.getY(), this.pos1.getY());
        maxY = Math.max(this.pos2.getY(), this.pos1.getY());
        minZ = Math.min(this.pos2.getZ(), this.pos1.getZ());
        maxZ = Math.max(this.pos2.getZ(), this.pos1.getZ());

        pos1 = new Location(pos1.getWorld(), minX, minY, minZ);
        pos2 = new Location(pos1.getWorld(), maxX, maxY, maxZ);
    }

//    public String toString(){
//        return "(pos1 = [(${"x = %.2f".format(pos1.x)}),(${"y = %.2f".format(pos1.y)}),(${"z = %.2f".format(pos1.z)})]" +
//                "pos2 = [(${"x = %.2f".format(pos2.x)}),(${"y = %.2f".format(pos2.y)}),(${"z = %.2f".format(pos2.z)})])"
//    }
//
//    Boolean isPlayerInArea(p:Player):Boolean
//
//    {
//        return p.location.blockX in pos1.x - 1..pos2.x - 1 && p.location.blockZ in pos1.z..pos2.z
//    }
//
//    Boolean isPlayerInCubic(p:Player):Boolean
//
//    {
//        return p.location.blockX in pos1.x..pos2.x && p.location.blockY in pos1.y..pos2.y && p.location.blockZ in pos1.
//        z..pos2.z
//    }

}
