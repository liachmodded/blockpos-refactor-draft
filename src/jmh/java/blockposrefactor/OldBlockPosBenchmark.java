package blockposrefactor;

import net.minecraft.util.math.BlockPos;
import org.openjdk.jmh.annotations.Benchmark;

public class OldBlockPosBenchmark {

    @Benchmark
    public void work() {
        BlockPos pos = BlockPos.ORIGIN.mutableCopy();
    }

}
