import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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

        PostgresConnection postgresConnection = null;
        try {
            postgresConnection = startPostgresConnection(commandLine.hasOption("sql"));
        } catch (SQLException e) {
            e.printStackTrace();
            exit(5);
        }

        TrackFetcher fetcher = parseFetcher(commandLine.getOptionValue("last-time"), postgresConnection);

        fetcher.fetchNewTracks();
        if (postgresConnection == null)
            fetcher.dumpTracks(writer);
        else
            fetcher.insertTracks();
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

    private static TrackFetcher parseFetcher(String lastTimeOpt, PostgresConnection postgresConnection) {
        TrackFetcher fetcher;
        Date lastTime = null;
        if (lastTimeOpt != null)
            lastTime = new Date(Long.parseLong(lastTimeOpt) * 1000L);

        if (postgresConnection != null && lastTime != null)
            fetcher = new TrackFetcher(postgresConnection, lastTime);
        else if (postgresConnection != null)
            fetcher = new TrackFetcher(postgresConnection);
        else if (lastTime != null)
            fetcher = new TrackFetcher(lastTime);
        else
            fetcher = new TrackFetcher();

        return fetcher;
    }

    private static PostgresConnection startPostgresConnection(boolean sql) throws SQLException {
        if (!sql) return null;
        Dotenv dotenv = Dotenv.load();
        String host = dotenv.get("DB_HOST") == null ? "localhost" : dotenv.get("DB_HOST");
        int port = dotenv.get("DB_PORT") == null ? 5432 : Integer.parseInt(dotenv.get("DB_PORT"));
        PostgresConnection connection = new PostgresConnection(dotenv.get("DB_NAME"));
        connection.setupCredentials(dotenv.get("DB_USER"), dotenv.get("DB_PASSWORD"));
        connection.connectToHost(host, port);
        return connection;
    }
}
