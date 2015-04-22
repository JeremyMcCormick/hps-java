package org.hps.conditions.cli;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.hps.conditions.api.ConditionsObjectCollection;
import org.hps.conditions.api.ConditionsObjectException;
import org.hps.conditions.api.ConditionsRecord;
import org.hps.conditions.api.ConditionsRecord.ConditionsRecordCollection;
import org.hps.conditions.database.DatabaseConditionsManager;
import org.hps.conditions.database.TableMetaData;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;
import org.lcsim.util.log.LogUtil;

/**
 * Create a conditions system tag.
 *
 * @author <a href="mailto:jeremym@slac.stanford.edu">Jeremy McCormick</a>
 */
public class TagCommand extends AbstractCommand {

    /**
     * The default detector name (dummy detector).
     */
    private static final String DETECTOR_NAME = "HPS-dummy-detector";

    /**
     * Setup logger.
     */
    private static final Logger LOGGER = LogUtil.create(TagCommand.class);

    /**
     * Defines command options.
     */
    private static Options OPTIONS = new Options();

    /**
     * Define command options.
     */
    static {
        OPTIONS.addOption(new Option("h", false, "Show help for tag command"));
        OPTIONS.addOption(new Option("r", true, "List of run numbers to scan (at least one must be provided)"));
        OPTIONS.getOption("r").setArgs(Option.UNLIMITED_VALUES);
        OPTIONS.getOption("r").setRequired(true);
        OPTIONS.addOption(new Option("t", true, "The new conditions tag"));
        OPTIONS.getOption("t").setRequired(true);
        OPTIONS.addOption(new Option("f", false, "Don't prompt before making tag (careful!)"));
    }

    /**
     * Class constructor.
     */
    TagCommand() {
        super("tag", "Tag a set of collections by copying their conditions records", OPTIONS);
    }

    /**
     * Execute the tag command.
     */
    @Override
    void execute(final String[] arguments) {

        final CommandLine commandLine = parse(arguments);

        final Set<Integer> runNumbers = new LinkedHashSet<Integer>();
        for (final String value : commandLine.getOptionValues("r")) {
            runNumbers.add(Integer.parseInt(value));
        }
        if (runNumbers.size() == 0) {
            throw new RuntimeException("At least one run number must be provided with the -r switch.");
        }

        final String newTag;
        if (commandLine.hasOption("t")) {
            newTag = commandLine.getOptionValue("t");
        } else {
            throw new RuntimeException("Missing required -t argument with the tag name.");
        }

        boolean dontPrompt = false;
        if (commandLine.hasOption("f")) {
            dontPrompt = true;
        }

        final ConditionsRecordCollection tagRecords = new ConditionsRecordCollection();
        final Set<Integer> addedIds = new HashSet<Integer>();

        final DatabaseConditionsManager manager = DatabaseConditionsManager.getInstance();
        manager.setXmlConfig("/org/hps/conditions/config/conditions_database_no_svt.xml");
        manager.setLogLevel(Level.ALL);

        // Scan through all the runs between the start and end run, inclusive.
        for (final Integer run : runNumbers) {
            try {
                // Setup the conditions manager with the run number.
                manager.setDetector(TagCommand.DETECTOR_NAME, run);
            } catch (final ConditionsNotFoundException e) {
                throw new RuntimeException(e);
            }

            // The records from this run.
            final ConditionsRecordCollection records = manager.getConditionsRecords();

            // The unique conditions keys from this run.
            final Set<String> keys = records.getConditionsKeys();

            // Scan through all the unique keys.
            for (final String key : keys) {

                // Get the table meta data for the key.
                final TableMetaData tableMetaData = manager.findTableMetaData(key);

                // Get the default collection for this key in the run.
                final ConditionsObjectCollection<?> collection = manager.getCachedConditions(
                        tableMetaData.getCollectionClass(), tableMetaData.getTableName()).getCachedData();

                // Get the ConditionsRecord from the collection.
                final ConditionsRecord record = collection.getConditionsRecord();

                // Is this record already part of the new tag?
                if (!addedIds.contains(record.getRowId())) {
                    // Create a new record copied from the old one.
                    final ConditionsRecord newRecord = new ConditionsRecord(record);

                    // Set the tag value.
                    newRecord.setFieldValue("tag", newTag);

                    // Add the record to the tag.
                    tagRecords.add(newRecord);

                    // Flag the record's ID as used so it is only added once.
                    addedIds.add(record.getRowId());
                }
            }
        }

        // Print out all the records that were found.
        LOGGER.info("found ConditionsRecords for tag " + newTag + " ...");
        for (final ConditionsRecord record : tagRecords) {
            LOGGER.info(record.toString());
        }

        // Prompt user to verify with console input.
        boolean makeTag = true;
        if (!dontPrompt) {
            LOGGER.info("Create conditions tag " + newTag + " in database?  (Y/N)");
            final String line = System.console().readLine();
            if (!line.equals("Y")) {
                makeTag = false;
            }
        }

        // Create the tag in the database if user verified or force option was present.
        if (makeTag) {
            try {
                tagRecords.insert();
            } catch (ConditionsObjectException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}