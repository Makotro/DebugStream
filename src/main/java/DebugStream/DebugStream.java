package debugStream;

/**
 * Created by Marko on 15.5.2016.
 */
public class DebugStream {

    /**
     * activate debugging for all streams, this will insert current time and source to each System.*.print* message
     * idea or netbeans users should set their IdeType enum as parameter since they need more information to generate a hyperlink
     * @param ide
     */
    public static void activate(IdeType ide) {
        DebugErrStream.activate(ide);
        DebugOutStream.activate(ide);
    }
    /**
     * activate debugging for all streams, this will insert current time and source to each System.*.print* message
     */
    public static void activate() {
        DebugErrStream.activate();
        DebugOutStream.activate();
    }

    /**
     * @param fileID
     *            unique id to separated different logs from each other, if none
     *            given it defaults to the time at which activate() is run, to
     *            the millisecond
     * @param maxFileAmount
     *            maximum number of files to keep
     * @param maxSizeInMB
     *            maximum size for each file, after which a new file is created
     *            and the old ones have +1 added to their name, i.e. log.0.txt
     *            -> log.1.txt
     * @param deleteLogsTime
     * 			  determines how old log files can be before they are deleted, in days
     * 			  i.e. if last modified time for the file is older than this, the file will be deleted
     */
    public static void activate(String fileID, int maxFileAmount,
                                int maxSizeInMB, long deleteLogsTime, String path) {
        DebugErrStream.activate(fileID, maxFileAmount, maxSizeInMB, deleteLogsTime, path);
        DebugOutStream.activate(fileID, maxFileAmount, maxSizeInMB, deleteLogsTime, path);
    }
}
