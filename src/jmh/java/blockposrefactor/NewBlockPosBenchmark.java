package blockposrefactor;

import org.openjdk.jmh.annotations.Benchmark;

public class NewBlockPosBenchmark {

    @Benchmark
    public void work() {
        ((NewBlockPos) new ImmutableNewBlockPos(1, 1, 1)).mutableCopy();
    }

}
