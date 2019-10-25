package edu.ucla.cs.jqf.bigfuzz;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import java.io.*;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

/**
 * A guidance that performs coverage-guided fuzzing using JDU (Joint Dataflow and UDF)
 * Mutations: 1. randomly mutation
 * code coverage guidance: control flow coverage, dataflow operators's coverage
 */
public class BigFuzzSalaryGuidance implements Guidance {

    /** The name of the test for display purposes. */
    protected final String testName;

    private boolean keepGoing = true;
    private static boolean KEEP_GOING_ON_ERROR = true;
    private Coverage coverage;

    /** The max amount of time to run for, in milli-seconds */
    protected final long maxDurationMillis;

    /** The number of trials completed. */
    private long numTrials = 0;

    /** The number of valid inputs. */
    protected long numValid = 0;

    private final long maxTrials;
    private final PrintStream out;
    private long numDiscards = 0;
    private final float maxDiscardRatio = 0.9f;

    /** Validity fuzzing -- if true then save valid inputs that increase valid coverage */
    protected boolean validityFuzzing;

    /** Coverage statistics for a single run. */
    protected Coverage runCoverage = new Coverage();

    /** Cumulative coverage statistics. */
    protected Coverage totalCoverage = new Coverage();

    /** Cumulative coverage for valid inputs. */
    protected Coverage validCoverage = new Coverage();

    /** The maximum number of keys covered by any single input found so far. */
    protected int maxCoverage = 0;

    /** The set of unique failures found so far. */
    protected Set<List<StackTraceElement>> uniqueFailures = new HashSet<>();

    // ---------- LOGGING / STATS OUTPUT ------------

    /** Whether to print log statements to stderr (debug option; manually edit). */
    protected final boolean verbose = true;


    /** The file where log data is written. */
    protected File logFile;

    // ------------- TIMEOUT HANDLING ------------

    /** Date when last run was started. */
    protected Date runStart;


    // ------------- FUZZING HEURISTICS ------------

    /** Whether to save inputs that only add new coverage bits (but no new responsibilities). */
    static final boolean SAVE_NEW_COUNTS = true;

    /** Whether to steal responsibility from old inputs (this increases computation cost). */
    static final boolean STEAL_RESPONSIBILITY = Boolean.getBoolean("jqf.ei.STEAL_RESPONSIBILITY");

    protected final String initialInputFile;
    SalaryAnalysisMutation mutation = new SalaryAnalysisMutation();
    private String currentInputFile;

    ArrayList<String> testInputFiles = new ArrayList<String>();


    public BigFuzzSalaryGuidance(String testName, String initialInputFile, long maxTrials, Duration duration, PrintStream out) throws IOException {

        this.testName = testName;
        this.maxDurationMillis = duration != null ? duration.toMillis() : Long.MAX_VALUE;
        //this.outputDirectory = outputDirectory;

        if (maxTrials <= 0) {
            throw new IllegalArgumentException("maxTrials must be greater than 0");
        }
        this.initialInputFile = initialInputFile;
        this.currentInputFile = initialInputFile;
        this.maxTrials = maxTrials;
        this.out = out;
    }

    @Override
    public InputStream getInput()
    {
        System.out.println("BigFuzzSalaryGuidance::getInput");
        //return Guidance.createInputStream(() -> random.nextInt(256));
        // Clear coverage stats for this run
        runCoverage.clear();

        if(!testInputFiles.isEmpty())
        {
            try
            {
                mutation.mutate(currentInputFile);
                String fileName = new SimpleDateFormat("yyyyMMddHHmm'.csv'").format(new Date());
                currentInputFile = fileName;
                mutation.writeFile(fileName);
            }
            catch (IOException e)
            {

            }
        }
        testInputFiles.add(currentInputFile);

        System.out.println("BigFuzzSalaryGuidance::getInput: "+numTrials+": "+currentInputFile );
        InputStream targetStream = new ByteArrayInputStream(currentInputFile.getBytes());

        return targetStream;
    }

    /** Writes a line of text to a given log file. */
    protected void appendLineToFile(File file, String line) throws GuidanceException {
        //System.out.println("appendLineToFile: ");
        //System.out.println("appendLineToFile: "+line);
        /*if(file == null)
        {
            System.out.println("File is NULL");
        }*/
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println(line);
        } catch (IOException e) {
            //System.out.println("appendLineToFile:throw: "+e.getMessage());
            throw new GuidanceException(e);
        }

    }

    /** Writes a line of text to the log file. */
    protected void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            if (logFile != null) {
                appendLineToFile(logFile, line);

            } else {
                System.err.println(line);
            }
        }
    }


    @Override
    public boolean hasInput() {
        return keepGoing;
    }

    @Override
    public void handleResult(Result result, Throwable error) {

        // Stop timeout handling
        this.runStart = null;

        System.out.println("BigFuzz::handleResult");
        System.out.println(result);

        this.numTrials++;

        boolean valid = result == Result.SUCCESS;

        if (valid) {
            // Increment valid counter
            this.numValid++;
        }

        // Display error stack trace in case of failure
        if (result == Result.FAILURE) {
            if (out != null) {
                error.printStackTrace(out);
            }
            this.keepGoing = KEEP_GOING_ON_ERROR;
        }

        // Keep track of discards
        if (result == Result.INVALID) {
            numDiscards++;
        }

        // Stopping criteria
        if (numTrials >= maxTrials) {
            this.keepGoing = false;
        }

        if (numTrials > 10 && ((float) numDiscards)/((float) (numTrials)) > maxDiscardRatio) {
            throw new GuidanceException("Assumption is too strong; too many inputs discarded");
        }

        if (result == Result.SUCCESS || result == Result.INVALID) {

            // Coverage before
            int nonZeroBefore = totalCoverage.getNonZeroCount();
            int validNonZeroBefore = validCoverage.getNonZeroCount();

            // Compute a list of keys for which this input can assume responsiblity.
            // Newly covered branches are always included.
            // Existing branches *may* be included, depending on the heuristics used.
            // A valid input will steal responsibility from invalid inputs
            Set<Object> responsibilities = computeResponsibilities(valid);
            System.out.println("Responsibilities of this input: "+responsibilities);

            // Update total coverage
            boolean coverageBitsUpdated = totalCoverage.updateBits(runCoverage);
            if (valid) {
                validCoverage.updateBits(runCoverage);
            }

            // Coverage after
            int nonZeroAfter = totalCoverage.getNonZeroCount();
            if (nonZeroAfter > maxCoverage) {
                maxCoverage = nonZeroAfter;
            }
            int validNonZeroAfter = validCoverage.getNonZeroCount();

            // Possibly save input
            boolean toSave = false;
            String why = "";


//            if (SAVE_NEW_COUNTS && coverageBitsUpdated) {
//                toSave = true;
//                why = why + "+count";
//            }

            // Save if new total coverage found
            if (nonZeroAfter > nonZeroBefore) {
                // Must be responsible for some branch
                assert(responsibilities.size() > 0);
                toSave = true;
                why = why + "+cov";
            }

            // Save if new valid coverage is found
            if (this.validityFuzzing && validNonZeroAfter > validNonZeroBefore) {
                // Must be responsible for some branch
                assert(responsibilities.size() > 0);
                toSave = true;
                why = why + "+valid";
            }

            if (toSave) {

                infoLog("Saving new input (at run %d): " +
//                                "input #%d " +
//                                "of size %d; " +
                                "total coverage = %d",
                        numTrials,
//                        savedInputs.size(),
//                        currentInput.size(),
                        nonZeroAfter);

                // Change current inputfile name

                File src = new File(currentInputFile);
                currentInputFile += why;
                File des = new File(currentInputFile);
                if (!src.renameTo(des)) {
                    System.out.println("Failed to renameTo file");
                }


            }

        } else if (result == Result.FAILURE || result == Result.TIMEOUT) {
            String msg = error.getMessage();

            // Get the root cause of the failure
            Throwable rootCause = error;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            // Attempt to add this to the set of unique failures
            if (uniqueFailures.add(Arrays.asList(rootCause.getStackTrace()))) {

            }
        }

    }

    // Compute a set of branches for which the current input may assume responsibility
    private Set<Object> computeResponsibilities(boolean valid) {
        Set<Object> result = new HashSet<>();

        // This input is responsible for all new coverage
        Collection<?> newCoverage = runCoverage.computeNewCoverage(totalCoverage);
        if (newCoverage.size() > 0) {
            result.addAll(newCoverage);
        }

        // If valid, this input is responsible for all new valid coverage
        if (valid) {
            Collection<?> newValidCoverage = runCoverage.computeNewCoverage(validCoverage);
            if (newValidCoverage.size() > 0) {
                result.addAll(newValidCoverage);
            }
        }

        // Perhaps it can also steal responsibility from other inputs
        if (STEAL_RESPONSIBILITY) {

        }
//        System.out.println("Result:" + result);

        return result;
    }

    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {

//        if (appThread != null) {
//            throw new IllegalStateException(ZestGuidance.class +
//                    " only supports single-threaded apps at the moment");
//        }
//        appThread = thread;

        return this::handleEvent;

        //print out the trace events generated during test execution
//        return (event) -> {
//            System.out.println(String.format("Thread %s produced event %s",
//                    thread.getName(), event));
//        };
    }

    /** Handles a trace event generated during test execution */
    protected void handleEvent(TraceEvent e) {
        // Collect totalCoverage
        runCoverage.handleEvent(e);
//        System.out.println(runCoverage.getNonZeroCount());
//        System.out.println(runCoverage.getCovered());

        // Check for possible timeouts every so often
//        if (this.singleRunTimeoutMillis > 0 &&
//                this.runStart != null && (++this.branchCount) % 10_000 == 0) {
//            long elapsed = new Date().getTime() - runStart.getTime();
//            if (elapsed > this.singleRunTimeoutMillis) {
//                throw new TimeoutException(elapsed, this.singleRunTimeoutMillis);
//            }
//        }
    }


    /**
     * Returns a reference to the coverage statistics.
     * @return a reference to the coverage statistics
     */
    public Coverage getCoverage() {
        if (coverage == null) {
            coverage = new Coverage();
        }
        return coverage;
    }
}
