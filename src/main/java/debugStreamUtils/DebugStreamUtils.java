package debugStreamUtils;

import debugStream.IdeType;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by Marko on 15.5.2016.
 */
public class DebugStreamUtils {

    private static Runnable runnable;

    /**
     * @return Unique file identifier as String, or in other words, current time to the
     * millisecond
     */
    public static String generateID() {
        SimpleDateFormat sdfDate = new SimpleDateFormat(
                "yyyy-MM-dd HH-mm-ss.SSS");
        return sdfDate.format(new Date());
    }
    /**
     * @return location, i.e. filename and row from where the message originates
     */
    public static String showLocation(IdeType ideType) {
        StackTraceElement element = null;
        String rtn = "";
        try {
            element = Thread.currentThread().getStackTrace()[3];
        } catch (IndexOutOfBoundsException e) {
        }
        if (element == null || element.getFileName() == null
                || element.getFileName().equals(""))
            rtn = "(unknown) : ";
        try {
            if (ideType == null)
                ideType = IdeType.ECLIPSE;
            switch(ideType) {
                case IDEA:
                    rtn = (MessageFormat.format("{0}({1}:{2, number,#}) : ", element.getClassName(),
                            element.getFileName(), element.getLineNumber()));
                    break;
                case NETBEANS:
                    rtn = (MessageFormat.format("{0}.{1}({2}:{3, number,#}) : ", element.getClassName(), element.getMethodName(),
                            element.getFileName(), element.getLineNumber()));
                    break;
                default:
                    rtn = (MessageFormat.format("({0}:{1, number,#}) : ",
                            element.getFileName(), element.getLineNumber()));
                    break;
            }
        } catch (NullPointerException e) {
            rtn =  "(unknown) : ";
        }
        return rtn;
    }    /**
     * Deletes all old error logs, this should be called from somewhere that is
     * run regularly
     *
     * @param daysBack How old files are deleted, files age is determined by its last
     *                 modified time
     */
    private static void deleteOldLogs(long daysBack, String filePath) {
        String dirWay = filePath;
        File directory = new File(dirWay);
        if (directory.exists()) {

            File[] listFiles = directory.listFiles();
            long purgeTime = System.currentTimeMillis()
                    - (daysBack * 24 * 60 * 60 * 1000);
            for (File listFile : listFiles) {
                if (listFile.lastModified() < purgeTime
                        && listFile.getName().contains("uilog")
                        && (listFile.getName().contains("error") || listFile.getName().contains("info"))
                        && listFile.getName().contains(".txt")) {
                    if (!listFile.delete()) {
//						System.err.println("Unable to delete file: " + listFile);
                    }
                }
            }
//			System.err.println(MessageFormat.format("Info log files older than {0} days, purged.", daysBack));
        }
    }
    /**
     * This starts an Executor that deletes old files regularly
     */
    public static void startPurger(final long deletionTime, final String filePath) {
        System.out.println("Starting error log file deletion executor.");
        if (runnable == null) {
            runnable = new Runnable() {
                public void run() {
                    deleteOldLogs(deletionTime, filePath);
                }
            };
            ScheduledExecutorService executor = Executors
                    .newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(runnable, 0, 60, TimeUnit.MINUTES);
        }
    }    /**
     * Creates the directory if needed.
     *
     * @param directoryName the directory name
     */
    public static void createDirectoryIfNeeded(String directoryName) {
        File theDir = new File(directoryName);

        if (!theDir.exists()) {
            try {
                theDir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * used to format the message output to log files
     */
    public static class MyCustomFormatter extends Formatter {
        String newline = System.getProperty("line.separator");

        public String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            sb.append(formatMessage(record));
            sb.append(newline);
            return sb.toString();
        }
    }
}
