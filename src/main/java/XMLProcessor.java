import java.io.File;

public class XMLProcessor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Only one file at a time");
        } else {
            File directory = new File(args[0]).getAbsoluteFile();
            boolean directoryExists = checkDir(directory);

            if (directoryExists) {
                baumdurchsuchen(directory);
            } else {
                System.out.println("The directory does not exist.");
            }
        }
    }

    private static boolean checkDir(File dir) {
        return dir.exists() && dir.isDirectory();
    }

    private static void baumdurchsuchen(File file) {
        if (file.isDirectory()) {
            File[] sBaum = file.listFiles();
            if (sBaum != null) {
                for (File f : sBaum) {
                	if(f.getName().equals("meta.xml")) {
                		System.out.println(f.getAbsolutePath()); // Pfad ausgeben
                	
                	}
                    baumdurchsuchen(f);
                }
            }
        } 
    }
}
