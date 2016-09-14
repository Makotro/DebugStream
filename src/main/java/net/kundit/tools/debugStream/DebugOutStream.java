package net.kundit.tools.debugStream;

import static net.kundit.tools.debugStream.DebugStreamUtils.*;
import static net.kundit.tools.debugStream.DebugStreamUtils.startPurger;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * This class intercepts default System out printstream that goes to the console and
 * adds a date and time prefix to it, it also attempts to add a link to the line
 * of code from where the stream originated.
 * It also starts logging the printstream in this form to a file if the .activate(...) with parameters is used.
 * System.out includes System.out.print*.
 *
 * @author mlpp
 */
public final class DebugOutStream extends PrintStream {
    private static final DebugOutStream INSTANCE = new DebugOutStream();
    private static Logger logger = Logger.getLogger("sysout.logging");
    private static String fileIdentified;
    private static int numOfFiles;
    private static boolean loggingEnabled;
    private static long deletionTime;
    private SimpleDateFormat sdfDate = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private String strDate;
    private Date now;
    private static FileHandler handler = null;
    private static String filePath = "/temp/debugstreamlog/";
    private static IdeType ideType;

    /**
     * activate the Debug stream that inserts current time and stacktrace location for each message
     * idea or netbeans users should set their IdeType enum as parameter since they need more information to generate a hyperlink
     */
    public static void activate(IdeType ide) {
        ideType = ide;
        activate();
    }
    /**
     * activate the Debug stream that inserts current time and stacktrace location for each message
     */
    public static void activate() {
        System.setOut(INSTANCE);
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
        if (fileID.equals(""))
            fileIdentified = generateID();
        else
            fileIdentified = fileID;
        filePath = path.equals("") ? filePath : path;
        numOfFiles = maxFileAmount;
        loggingEnabled = true;
        int sizeOfFiles = maxSizeInMB;
        deletionTime = deleteLogsTime;
        createDirectoryIfNeeded(filePath);

        try {
            handler = new FileHandler(filePath + "uilog-info-"
                    + fileIdentified + ".%g.%u.txt", 1024 * 1024 * sizeOfFiles,
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



    private DebugOutStream() {
        super(System.out);
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

    @Override
    public PrintStream printf(String format, Object... args) {
        now = new Date();
        strDate = sdfDate.format(now);
        if (loggingEnabled)
            log(String.format(String.format("[%s] %s %s", strDate, showLocation(ideType), format), args));

        return super.printf(String.format("[%s] %s %s", strDate, showLocation(ideType), format), args);
    }

    /**
     * @param x text to log to info log file
     */
    private void log(Object x) {
        LogRecord record = new LogRecord(Level.INFO, x.toString());
        logger.log(record);
//		handler.flush();
//		handler.close();
    }

}