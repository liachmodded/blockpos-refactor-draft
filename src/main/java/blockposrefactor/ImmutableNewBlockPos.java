package blockposrefactor;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Spliterator;
import java.util.function.IntConsumer;

/**
 * Represents the position of a block in a three-dimensional volume.
 *
 * <p>The position is integer-valued.
 *
 * <p>A block position may be mutable; hence, when using block positions
 * obtained from other places as map keys, etc., you should call {@link
 * #toImmutable()} to obtain an immutable block position.
 */
public class ImmutableNewBlockPos extends NewVec3i implements NewBlockPos {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final int SIZE_BITS_X = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    public static final int SIZE_BITS_Z;
    public static final int SIZE_BITS_Y;
    public static final long BITS_X;
    public static final long BITS_Y;
    public static final long BITS_Z;
    public static final int BIT_SHIFT_Z;
    public static final int BIT_SHIFT_X;

    static {
        SIZE_BITS_Z = SIZE_BITS_X;
        SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
        BITS_X = (1L << SIZE_BITS_X) - 1L;
        BITS_Y = (1L << SIZE_BITS_Y) - 1L;
        BITS_Z = (1L << SIZE_BITS_Z) - 1L;
        BIT_SHIFT_Z = SIZE_BITS_Y;
        BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;
    }

    public ImmutableNewBlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public ImmutableNewBlockPos(double d, double e, double f) {
        super(d, e, f);
    }

    public ImmutableNewBlockPos(Vec3d pos) {
        this(pos.x, pos.y, pos.z);
    }

    public ImmutableNewBlockPos(Position pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public ImmutableNewBlockPos(NewVec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public static <T> ImmutableNewBlockPos deserialize(Dynamic<T> dynamic) {
        Spliterator.OfInt ofInt = dynamic.asIntStream().spliterator();
        int[] is = new int[3];
        if (ofInt.tryAdvance((IntConsumer) (i) -> {
            is[0] = i;
        }) && ofInt.tryAdvance((IntConsumer) (i) -> {
            is[1] = i;
        })) {
            ofInt.tryAdvance((IntConsumer) (i) -> {
                is[2] = i;
            });
        }

        return new ImmutableNewBlockPos(is[0], is[1], is[2]);
    }

    public static ImmutableNewBlockPos fromLong(long value) {
        return new ImmutableNewBlockPos(NewBlockPos.unpackLongX(value), NewBlockPos.unpackLongY(value), NewBlockPos.unpackLongZ(value));
    }

    /**
     * Returns an immutable block position with the same x, y, and z as this
     * position.
     *
     * <p>This method should be called when a block position is used as map
     * keys as to prevent side effects of mutations of mutable block positions.
     */
    @Override public ImmutableNewBlockPos toImmutable() {
        return this;
    }

    @Override public ImmutableNewBlockPos down() {
        return NewBlockPos.super.down();
    }

    @Override public ImmutableNewBlockPos down(int i) {
        return NewBlockPos.super.down(i);
    }

    @Override public ImmutableNewBlockPos offset(Direction direction, int distance) {
        return NewBlockPos.super.offset(direction, distance);
    }

    @Override public ImmutableNewBlockPos crossProduct(NewVec3i vec) {
        return NewBlockPos.super.crossProduct(vec);
    }
}
