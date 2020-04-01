package blockposrefactor;

import com.google.common.collect.AbstractIterator;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.dynamic.DynamicSerializable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Direction;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface NewBlockPos extends DynamicSerializable {

    /**
     * The block position which x, y, and z values are all zero.
     */
    ImmutableNewBlockPos ORIGIN = new ImmutableNewBlockPos(0, 0, 0);

    int getX();
    
    int getY();
    
    int getZ();

    static long offset(long value, Direction direction) {
        return add(value, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    static long add(long value, int x, int y, int z) {
        return asLong(unpackLongX(value) + x, unpackLongY(value) + y, unpackLongZ(value) + z);
    }

    static int unpackLongX(long x) {
        return (int) (x << 64 - ImmutableNewBlockPos.BIT_SHIFT_X - ImmutableNewBlockPos.SIZE_BITS_X >> 64 - ImmutableNewBlockPos.SIZE_BITS_X);
    }

    static int unpackLongY(long y) {
        return (int) (y << 64 - ImmutableNewBlockPos.SIZE_BITS_Y >> 64 - ImmutableNewBlockPos.SIZE_BITS_Y);
    }

    static int unpackLongZ(long z) {
        return (int) (z << 64 - ImmutableNewBlockPos.BIT_SHIFT_Z - ImmutableNewBlockPos.SIZE_BITS_Z >> 64 - ImmutableNewBlockPos.SIZE_BITS_Z);
    }

    static long asLong(int x, int y, int z) {
        long l = 0L;
        l |= ((long) x & ImmutableNewBlockPos.BITS_X) << ImmutableNewBlockPos.BIT_SHIFT_X;
        l |= ((long) y & ImmutableNewBlockPos.BITS_Y) << 0;
        l |= ((long) z & ImmutableNewBlockPos.BITS_Z) << ImmutableNewBlockPos.BIT_SHIFT_Z;
        return l;
    }

    static long removeChunkSectionLocalY(long y) {
        return y & -16L;
    }

    /**
     * Iterates block positions around the {@code center}. The iteration order
     * is mainly based on the manhattan distance of the position from the
     * center.
     *
     * <p>For the same manhattan distance, the positions are iterated by y
     * offset, from negative to positive. For the same y offset, the positions
     * are iterated by x offset, from negative to positive. For the two
     * positions with the same x and y offsets and the same manhattan distance,
     * the one with a positive z offset is visited first before the one with a
     * negative z offset.
     *
     * @param center the center of iteration
     * @param xRange the maximum x difference from the center
     * @param yRange the maximum y difference from the center
     * @param zRange the maximum z difference from the center
     */
    static Iterable<MutableNewBlockPos> iterateOutwards(NewBlockPos center, int xRange, int yRange, int zRange) {
        int i = xRange + yRange + zRange;
        int j = center.getX();
        int k = center.getY();
        int l = center.getZ();
        return () -> {
            return new AbstractIterator<MutableNewBlockPos>() {
                private final MutableNewBlockPos field_23378 = new MutableNewBlockPos();
                private int manhattanDistance;
                private int limitX;
                private int limitY;
                private int dx;
                private int dy;
                private boolean field_23379;

                protected MutableNewBlockPos computeNext() {
                    if (this.field_23379) {
                        this.field_23379 = false;
                        this.field_23378.setZ(i - (this.field_23378.getZ() - i));
                        return this.field_23378;
                    } else {
                        MutableNewBlockPos blockPos;
                        for (blockPos = null; blockPos == null; ++this.dy) {
                            if (this.dy > this.limitY) {
                                ++this.dx;
                                if (this.dx > this.limitX) {
                                    ++this.manhattanDistance;
                                    if (this.manhattanDistance > j) {
                                        return this.endOfData();
                                    }

                                    this.limitX = Math.min(k, this.manhattanDistance);
                                    this.dx = -this.limitX;
                                }

                                this.limitY = Math.min(l, this.manhattanDistance - Math.abs(this.dx));
                                this.dy = -this.limitY;
                            }

                            int ix = this.dx;
                            int jx = this.dy;
                            int kx = this.manhattanDistance - Math.abs(ix) - Math.abs(jx);
                            if (kx <= 0/*m decomp*/) {
                                this.field_23379 = kx != 0;
                                blockPos = this.field_23378.set(j + ix, k + jx, l + kx);
                            }
                        }

                        return blockPos;
                    }
                }
            };
        };
    }

    static Optional<MutableNewBlockPos> findClosest(NewBlockPos pos, int horizontalRange, int verticalRange, Predicate<NewBlockPos> condition) {
        return streamOutwards(pos, horizontalRange, verticalRange, horizontalRange).filter(condition).findFirst();
    }

    static Stream<MutableNewBlockPos> streamOutwards(NewBlockPos center, int maxX, int maxY, int maxZ) {
        return StreamSupport.stream(iterateOutwards(center, maxX, maxY, maxZ).spliterator(), false);
    }

    static Iterable<MutableNewBlockPos> iterate(NewBlockPos start, NewBlockPos end) {
        return iterate(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()),
                Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
    }

    static Stream<MutableNewBlockPos> stream(NewBlockPos start, NewBlockPos end) {
        return StreamSupport.stream(iterate(start, end).spliterator(), false);
    }

    static Stream<MutableNewBlockPos> stream(BlockBox box) {
        return stream(Math.min(box.minX, box.maxX), Math.min(box.minY, box.maxY), Math.min(box.minZ, box.maxZ), Math.max(box.minX, box.maxX),
                Math.max(box.minY, box.maxY), Math.max(box.minZ, box.maxZ));
    }

    static Stream<MutableNewBlockPos> stream(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return StreamSupport.stream(iterate(startX, startY, startZ, endX, endY, endZ).spliterator(), false);
    }

    static Iterable<MutableNewBlockPos> iterate(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        int i = endX - startX + 1;
        int j = endY - startY + 1;
        int k = endZ - startZ + 1;
        int l = i * j * k;
        return () -> {
            return new AbstractIterator<MutableNewBlockPos>() {
                private final MutableNewBlockPos field_23380 = new MutableNewBlockPos();
                private int index;

                protected MutableNewBlockPos computeNext() {
                    if (this.index == i) {
                        return this.endOfData();
                    } else {
                        int ix = this.index % j;
                        int jx = this.index / j;
                        int kx = jx % k;
                        int lx = jx / k;
                        ++this.index;
                        return this.field_23380.set(startX + ix, startY + kx, startZ + lx);
                    }
                }
            };
        };
    }

    default <T> T serialize(DynamicOps<T> ops) {
        return ops.createIntList(IntStream.of(new int[]{this.getX(), this.getY(), this.getZ()}));
    }

    default long asLong() {
        return asLong(this.getX(), this.getY(), this.getZ());
    }

    default ImmutableNewBlockPos add(double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D ? toImmutable() :
                new ImmutableNewBlockPos((double) this.getX() + x, (double) this.getY() + y, (double) this.getZ() + z);
    }

    default ImmutableNewBlockPos add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? toImmutable() : new ImmutableNewBlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    default ImmutableNewBlockPos add(NewVec3i pos) {
        return this.add(pos.getX(), pos.getY(), pos.getZ());
    }

    default ImmutableNewBlockPos subtract(NewVec3i pos) {
        return this.add(-pos.getX(), -pos.getY(), -pos.getZ());
    }

    default ImmutableNewBlockPos up() {
        return this.offset(Direction.UP);
    }

    default ImmutableNewBlockPos up(int distance) {
        return this.offset(Direction.UP, distance);
    }

    default ImmutableNewBlockPos down() {
        return this.offset(Direction.DOWN);
    }

    default ImmutableNewBlockPos down(int distance) {
        return this.offset(Direction.DOWN, distance);
    }

    default ImmutableNewBlockPos north() {
        return this.offset(Direction.NORTH);
    }

    default ImmutableNewBlockPos north(int distance) {
        return this.offset(Direction.NORTH, distance);
    }

    default ImmutableNewBlockPos south() {
        return this.offset(Direction.SOUTH);
    }

    default ImmutableNewBlockPos south(int distance) {
        return this.offset(Direction.SOUTH, distance);
    }

    default ImmutableNewBlockPos west() {
        return this.offset(Direction.WEST);
    }

    default ImmutableNewBlockPos west(int distance) {
        return this.offset(Direction.WEST, distance);
    }

    default ImmutableNewBlockPos east() {
        return this.offset(Direction.EAST);
    }

    default ImmutableNewBlockPos east(int distance) {
        return this.offset(Direction.EAST, distance);
    }

    default ImmutableNewBlockPos offset(Direction direction) {
        return new ImmutableNewBlockPos(this.getX() + direction.getOffsetX(), this.getY() + direction.getOffsetY(), this.getZ() + direction.getOffsetZ());
    }

    default ImmutableNewBlockPos offset(Direction direction, int i) {
        return i == 0 ? toImmutable() : new ImmutableNewBlockPos(this.getX() + direction.getOffsetX() * i, this.getY() + direction.getOffsetY() * i,
                this.getZ() + direction.getOffsetZ() * i);
    }

    default ImmutableNewBlockPos rotate(BlockRotation rotation) {
        switch (rotation) {
            case NONE:
            default:
                return toImmutable();
            case CLOCKWISE_90:
                return new ImmutableNewBlockPos(-this.getZ(), this.getY(), this.getX());
            case CLOCKWISE_180:
                return new ImmutableNewBlockPos(-this.getX(), this.getY(), -this.getZ());
            case COUNTERCLOCKWISE_90:
                return new ImmutableNewBlockPos(this.getZ(), this.getY(), -this.getX());
        }
    }

    default ImmutableNewBlockPos crossProduct(NewVec3i pos) {
        return new ImmutableNewBlockPos(this.getY() * pos.getZ() - this.getZ() * pos.getY(), this.getZ() * pos.getX() - this.getX() * pos.getZ(),
                this.getX() * pos.getY() - this.getY() * pos.getX());
    }

    ImmutableNewBlockPos toImmutable();

    /**
     * Returns a mutable copy of this block position.
     *
     * <p>If this block position is a mutable one, mutation to this block
     * position won't affect the returned position.
     */
    default MutableNewBlockPos mutableCopy() {
        return new MutableNewBlockPos(this.getX(), this.getY(), this.getZ());
    }

}
