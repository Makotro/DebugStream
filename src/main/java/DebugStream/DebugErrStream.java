package debugStream;


import static debugStreamUtils.DebugStreamUtils.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class intercepts default System err printstream that goes to the console and
 * adds a date and time prefix to it, it also attempts to add a link to the line
 * of code from where the stream originated.
 * It also starts logging the printstream in this form to a file if the .activate(...) with parameters is used
 * System.err includes System.err.print* and most importantly, stacktraces.
 *
 * @author mlpp
 */

public class DebugErrStream extends PrintStream {
    private static final DebugErrStream INSTANCE = new DebugErrStream();
    private static Logger logger = Logger.getLogger("syserr.logging");
    private static String fileIdentifier;
    private SimpleDateFormat sdfDate = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private String strDate;
    private Date now;
    private static FileHandler handler = null;
    private static int numOfFiles;
    private static int sizeOfFiles;
    private static boolean loggingEnabled;
    private static long deletionTime;
    private static String filePath = "/temp/debugstreamlog/";
    private static IdeType ideType;

    /**
     * activate the Debug stream that inserts current time and stacktrace location for each message
     * idea or netbeans users should set their IdeType enum as parameter since they need more information to generate a hyperlink
     * @param ide
     */
    public static void activate(IdeType ide) {
        ideType = ide;
        activate();
    }
    /**
     * activate the Debug stream that inserts current time and stacktrace location for each message
     */
    public static void activate() {
        System.setErr(INSTANCE);
    }

    /**
     * @param fileID         unique id to separated different logs from each other, if none
     *                       given it defaults to the time at which activate() is run, to
     *                       the millisecond
     * @param maxFileAmount  maximum number of files to keep
     * @param maxSizeInMB    maximum size for each file, after which a new file is created
     *                       and the old ones have +1 added to their name, i.e. log.0.txt
     *                       -> log.1.txt
     * @param deleteLogsTime determines how old log files can be before they are deleted, in days
     *                       i.e. if last modified time for the file is older than this, the file will be deleted
     * @param path           path where to save the log files, if not set, will go to /temp/debugstreamlog/
     */
    public static void activate(String fileID, int maxFileAmount,
                                int maxSizeInMB, long deleteLogsTime, String path) {
        fileIdentifier = fileID.equals("") ? generateID() : fileID;
        filePath = path.equals("") ? filePath : path;
        numOfFiles = maxFileAmount;
        loggingEnabled = true;
        sizeOfFiles = maxSizeInMB;
        deletionTime = deleteLogsTime;
        createDirectoryIfNeeded(filePath);

        try {
            // naming of the log file and its size
            handler = new FileHandler(filePath + "uilog-error."
                    + fileIdentifier + ".%g.%u.txt", 1024 * 1024 * sizeOfFiles,
                    numOfFiles, true);
            handler.setFormatter(new MyCustomFormatter());
        } catch (SecurityException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        activate();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        startPurger(deletionTime, filePath);
    }


    private DebugErrStream() {
        super(System.err);
    }

    @Override
    public void println(Object x) {
        now = new Date();
        strDate = sdfDate.format(now);
        super.println("[" + strDate + "] " + showLocation(ideType) + x);
        if (loggingEnabled)
            log("[" + strDate + "] " + showLocation(ideType) + x);
    }

    @Override
    public void println(String x) {
        now = new Date();
        strDate = sdfDate.format(now);
        super.println("[" + strDate + "] " + showLocation(ideType) + x);
        if (loggingEnabled)
            log("[" + strDate + "] " + showLocation(ideType) + x);
    }

    /**
     * @param x text to log to error log file
     */
    private void log(Object x) {
        LogRecord record = new LogRecord(Level.INFO, x.toString());
        logger.log(record);
        handler.flush();
    }
}