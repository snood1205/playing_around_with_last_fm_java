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
        initializeOptions();
        parser = new DefaultParser();
        formatter = new HelpFormatter();
        initializeCommandLine(args);
        startPostgresConnection();
        initializeFetcher();
    }

    private void run() {
        if (options.hasOption("--reset")) {
            trackFetcher.deleteTracks();
            trackFetcher.fetchNewTracks();
        } else if (options.hasOption("--delete")) {
            trackFetcher.deleteTracks();
        } else {
            trackFetcher.fetchNewTracks();
            if (postgresConnection == null)
                trackFetcher.dumpTracks(initializeWriter());
            else
                trackFetcher.insertTracks();
        }
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

    private void initializeOptions() {
        options = new Options();

        addOption(new Option("l", "last-time", true, "Time track was last listened at in seconds since epoch"), false);
        addOption(new Option("o", "output-file", true, "Output File"), false);
        addOption(new Option("s", "sql", false, "Insert to SQL instead of printing to file"), false);
        addOption(new Option("r", "reset", false, "Clear the table"), false);
        addOption(new Option("d", "delete", false, "Delete all tracks from table"), false);
    }

    private void addOption(Option option, boolean required) {
        option.setRequired(required);
        options.addOption(option);
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
        if (!(commandLine.hasOption("sql") || commandLine.hasOption("reset"))) return;
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

