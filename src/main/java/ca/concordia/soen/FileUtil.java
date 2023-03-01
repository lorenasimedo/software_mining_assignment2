package ca.concordia.soen;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

    public static List<String> getAllFileNames(String directoryName) {

        File directory = new File(directoryName);
        List<String> fileNames = new ArrayList<>();
        File[] fileList = directory.listFiles();

        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile() && file.toString().endsWith(".java")) {
                    fileNames.add(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    fileNames.addAll(getAllFileNames(file.getAbsolutePath()));
                }
            }
        }

        return fileNames;

    }


    public static String read(String filename) throws IOException {

        Path path = Paths.get(filename);
        try (Stream<String> lines = Files.lines(path)) {
            return lines.collect(Collectors.joining("\n"));
        }

    }
}
