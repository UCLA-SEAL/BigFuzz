package edu.ucla.cs.jqf.bigfuzz;

//import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class BigFuzzDriver {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java " + BigFuzzDriver.class + " TEST_CLASS TEST_METHOD [OUTPUT_DIR [SEEDS...]]");
            System.exit(1);
        }

        String testClassName = args[0];
        String testMethodName = args[1];
        Long maxTrials = args.length > 2 ? Long.parseLong(args[2]) : Long.MAX_VALUE;
        System.out.println("maxTrials: "+maxTrials);
//        File outputDirectory = new File("../fuzz-results");

//        String file = "/Users/zhuhaichao/Documents/Workspace/github/BigFuzz/dataset/salary1.csv";
        String file = "dataset/salary1.csv";
       try {
            String title = testClassName+"#"+testMethodName;
            //NoGuidance guidance = new NoGuidance(file, maxTrials, System.err);
            Duration duration = Duration.of(100, ChronoUnit.SECONDS);
            BigFuzzGuidance guidance = new BigFuzzGuidance("Test1", file, maxTrials, duration, System.err);
            // Run the Junit test
            GuidedFuzzing.run(testClassName, testMethodName, guidance, System.out);

            if (Boolean.getBoolean("jqf.logCoverage")) {
                System.out.println(String.format("Covered %d edges.",
                        guidance.getCoverage().getNonZeroCount()));
            }

        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(2);
        }

    }
}
