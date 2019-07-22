import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static java.lang.System.exit;

public class Runner {
    public static void main(String... args) {
        Options options = generateOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            formatter.printHelp("track-fetcher", options);

            exit(1);
        }


        PrintWriter writer = parseWriter(commandLine.getOptionValue("output-file"));
        TrackFetcher fetcher = parseFetcher(commandLine.getOptionValue("last-time"));

        fetcher.fetchNewTracks();
        fetcher.dumpTracks(writer);
    }

    private static Options generateOptions() {
        Options options = new Options();

        Option lastTime = new Option("l", "last-time", true, "Time track was last listened at in seconds since epoch");
        lastTime.setRequired(false);
        options.addOption(lastTime);

        Option outputFile = new Option("o", "output-file", true, "Output File");

        outputFile.setRequired(false);
        options.addOption(outputFile);

        return options;
    }

    private static PrintWriter parseWriter(String outputFileOpt) {
        PrintWriter writer = null;
        if (outputFileOpt == null) {
            writer = new PrintWriter(System.out);
        } else {
            try {
                writer = new PrintWriter(new File(outputFileOpt));
            } catch (IOException e) {
                e.printStackTrace();
                exit(2);
            }
        }
        return writer;
    }

    private static TrackFetcher parseFetcher(String lastTimeOpt) {
        TrackFetcher fetcher;
        if (lastTimeOpt == null) {
            fetcher = new TrackFetcher();
        } else {
            Date lastTime = new Date(Long.parseLong(lastTimeOpt) * 1000L);
            fetcher = new TrackFetcher(lastTime);
        }
        return fetcher;
    }
}
