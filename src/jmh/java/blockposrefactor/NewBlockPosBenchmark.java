package blockposrefactor;

import org.openjdk.jmh.annotations.Benchmark;

public class NewBlockPosBenchmark {

    @Benchmark
    public void work() {
        NewBlockPos newPos = NewBlockPos.ORIGIN.mutableCopy();
    }

}
