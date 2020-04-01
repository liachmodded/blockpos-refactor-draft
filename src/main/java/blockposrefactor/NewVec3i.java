package blockposrefactor;

import com.google.common.base.MoreObjects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;

public class NewVec3i implements Comparable<NewVec3i> {

    public static final NewVec3i ZERO = new NewVec3i(0, 0, 0);
    private int x;
    private int y;
    private int z;

    public NewVec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public NewVec3i(double x, double y, double z) {
        this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof NewVec3i)) {
            return false;
        } else {
            NewVec3i vec3i = (NewVec3i) object;
            if (this.getX() != vec3i.getX()) {
                return false;
            } else if (this.getY() != vec3i.getY()) {
                return false;
            } else {
                return this.getZ() == vec3i.getZ();
            }
        }
    }

    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public int compareTo(NewVec3i vec3i) {
        if (this.getY() == vec3i.getY()) {
            return this.getZ() == vec3i.getZ() ? this.getX() - vec3i.getX() : this.getZ() - vec3i.getZ();
        } else {
            return this.getY() - vec3i.getY();
        }
    }

    public int getX() {
        return this.x;
    }

    protected void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    protected void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return this.z;
    }

    protected void setZ(int z) {
        this.z = z;
    }

    public NewVec3i down() {
        return this.down(1);
    }

    public NewVec3i down(int i) {
        return this.offset(Direction.DOWN, i);
    }

    public NewVec3i offset(Direction direction, int distance) {
        return distance == 0 ? this : new NewVec3i(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance,
                this.getZ() + direction.getOffsetZ() * distance);
    }

    public NewVec3i crossProduct(NewVec3i vec) {
        return new NewVec3i(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(),
                this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    public boolean isWithinDistance(NewVec3i vec, double distance) {
        return this.getSquaredDistance((double) vec.getX(), (double) vec.getY(), (double) vec.getZ(), false) < distance * distance;
    }

    public boolean isWithinDistance(Position pos, double distance) {
        return this.getSquaredDistance(pos.getX(), pos.getY(), pos.getZ(), true) < distance * distance;
    }

    public double getSquaredDistance(NewVec3i vec) {
        return this.getSquaredDistance((double) vec.getX(), (double) vec.getY(), (double) vec.getZ(), true);
    }

    public double getSquaredDistance(Position pos, boolean treatAsBlockPos) {
        return this.getSquaredDistance(pos.getX(), pos.getY(), pos.getZ(), treatAsBlockPos);
    }

    public double getSquaredDistance(double x, double y, double z, boolean treatAsBlockPos) {
        double d = treatAsBlockPos ? 0.5D : 0.0D;
        double e = (double) this.getX() + d - x;
        double f = (double) this.getY() + d - y;
        double g = (double) this.getZ() + d - z;
        return e * e + f * f + g * g;
    }

    public int getManhattanDistance(NewVec3i vec) {
        float f = (float) Math.abs(vec.getX() - this.getX());
        float g = (float) Math.abs(vec.getY() - this.getY());
        float h = (float) Math.abs(vec.getZ() - this.getZ());
        return (int) (f + g + h);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    @Environment(EnvType.CLIENT)
    public String toShortString() {
        return "" + this.getX() + ", " + this.getY() + ", " + this.getZ();
    }
}
