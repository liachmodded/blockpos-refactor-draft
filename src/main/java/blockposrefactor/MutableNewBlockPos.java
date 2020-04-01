package blockposrefactor;

import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class MutableNewBlockPos implements NewBlockPos {
    
    private int x;
    private int y;
    private int z;

    public MutableNewBlockPos() {
        this(0, 0, 0);
    }

    public MutableNewBlockPos(int i, int j, int k) {
        this.x = i;
        this.y = j;
        this.z = k;
    }

    public MutableNewBlockPos(double d, double e, double f) {
        this(MathHelper.floor(d), MathHelper.floor(e), MathHelper.floor(f));
    }

    /**
     * Sets the x, y, and z of this mutable block position.
     */
    public MutableNewBlockPos set(int x, int y, int z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        return this;
    }

    public MutableNewBlockPos set(double x, double y, double z) {
        return this.set(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public MutableNewBlockPos set(NewVec3i pos) {
        return this.set(pos.getX(), pos.getY(), pos.getZ());
    }

    public MutableNewBlockPos set(long pos) {
        return this.set(NewBlockPos.unpackLongX(pos), NewBlockPos.unpackLongY(pos), NewBlockPos.unpackLongZ(pos));
    }

    public MutableNewBlockPos set(AxisCycleDirection axis, int x, int y, int z) {
        return this.set(axis.choose(x, y, z, Direction.Axis.X), axis.choose(x, y, z, Direction.Axis.Y), axis.choose(x, y, z, Direction.Axis.Z));
    }

    /**
     * Sets this mutable block position to the offset position of the given
     * pos by the given direction.
     */
    public MutableNewBlockPos set(NewVec3i pos, Direction direction) {
        return this.set(pos.getX() + direction.getOffsetX(), pos.getY() + direction.getOffsetY(), pos.getZ() + direction.getOffsetZ());
    }

    /**
     * Sets this mutable block position to the sum of the given position and the
     * given x, y, and z.
     */
    public MutableNewBlockPos set(NewVec3i pos, int x, int y, int z) {
        return this.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    /**
     * Moves this mutable block position by 1 block in the given direction.
     */
    public MutableNewBlockPos move(Direction direction) {
        return this.move(direction, 1);
    }

    /**
     * Moves this mutable block position by the given distance in the given
     * direction.
     */
    public MutableNewBlockPos move(Direction direction, int distance) {
        return this.set(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance,
                this.getZ() + direction.getOffsetZ() * distance);
    }

    /**
     * Moves the mutable block position by the delta x, y, and z provided.
     */
    public MutableNewBlockPos move(int dx, int dy, int dz) {
        return this.set(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public ImmutableNewBlockPos toImmutable() {
        return new ImmutableNewBlockPos(x, y, z);
    }

    @Override public int getX() {
        return x;
    }

    @Override public int getY() {
        return y;
    }

    @Override public int getZ() {
        return z;
    }
}
