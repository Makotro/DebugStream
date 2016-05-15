package debugStream;

/**
 * Created by Marko on 15.5.2016.
 */
public class Test {

    public static void main(String[] args) {
        System.out.println("This standard output will not have the debug info activated");
        System.err.println("This standard error output will not have the debug info activated");
        DebugStream.activate(IdeType.IDEA);
        System.out.println("This standard output will have the debug info activated");
        System.err.println("This standard error output will have the debug info activated");
        System.out.println("I use this to quickly debug my small projects so I can naviage nimbly to the source of the message");
    }
}
