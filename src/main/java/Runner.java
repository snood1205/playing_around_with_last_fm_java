import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

import static java.lang.System.exit;

public class Runner {
    private Options options;
    private CommandLineParser parser;
    private HelpFormatter formatter;
    private PostgresConnection postgresConnection;
    private CommandLine commandLine;
    private TrackFetcher trackFetcher;
    private Dotenv dotenv;

    public static void main(String... args) {
        new Runner(args).run();
    }

    private Runner(String... args) {
        options = generateOptions();
        parser = new DefaultParser();
        formatter = new HelpFormatter();
        initializeCommandLine(args);
        startPostgresConnection();
        initializeFetcher();
    }

    private void run() {
        trackFetcher.fetchNewTracks();
        if (postgresConnection == null)
            trackFetcher.dumpTracks(initializeWriter());
        else
            trackFetcher.insertTracks();
    }

    public void initializeCommandLine(String... args) {
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            formatter.printHelp("track-fetcher", options);

            exit(1);
        }
    }

    private static Options generateOptions() {
        Options options = new Options();

        Option lastTime = new Option("l", "last-time", true, "Time track was last listened at in seconds since epoch");
        lastTime.setRequired(false);
        options.addOption(lastTime);

        Option outputFile = new Option("o", "output-file", true, "Output File");
        outputFile.setRequired(false);
        options.addOption(outputFile);

        Option sql = new Option("s", "sql", false, "Insert to SQL instead of printing to file");
        sql.setRequired(false);
        options.addOption(sql);

        return options;
    }

    private PrintWriter initializeWriter() {
        PrintWriter printerWriter = null;
        String outputFile = commandLine.getOptionValue("output-file");

        if (outputFile == null)
            printerWriter = new PrintWriter(System.out);
        else {
            try {
                printerWriter = new PrintWriter(new File(outputFile));
            } catch (IOException e) {
                e.printStackTrace();
                exit(2);
            }
        }
        return printerWriter;
    }

    private void initializeFetcher() {
        Date lastTime = null;
        String lastTimeStr = dotenv.get("last-time");
        if (lastTimeStr != null)
            lastTime = new Date(Long.parseLong(lastTimeStr) * 1000L);

        if (postgresConnection != null && lastTime != null)
            trackFetcher = new TrackFetcher(postgresConnection, lastTime);
        else if (postgresConnection != null)
            trackFetcher = new TrackFetcher(postgresConnection);
        else if (lastTime != null)
            trackFetcher = new TrackFetcher(lastTime);
        else
            trackFetcher = new TrackFetcher();
    }

    private void startPostgresConnection() {
        dotenv = Dotenv.load();
        if (!commandLine.hasOption("sql")) return;
        String host = dotenv.get("DB_HOST") == null ? "localhost" : dotenv.get("DB_HOST");
        int port = dotenv.get("DB_PORT") == null ? 5432 : Integer.parseInt(dotenv.get("DB_PORT"));
        postgresConnection = new PostgresConnection(dotenv.get("DB_NAME"));
        postgresConnection.setupCredentials(dotenv.get("DB_USER"), dotenv.get("DB_PASSWORD"));
        try {
            postgresConnection.connectToHost(host, port);
        } catch (SQLException e) {
            e.printStackTrace();
            exit(5);
        }
    }
}

