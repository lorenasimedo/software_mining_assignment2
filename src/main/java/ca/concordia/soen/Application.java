package ca.concordia.soen;

import com.google.gson.JsonObject;

import java.io.File;
import java.util.List;

public class Application {

    static String output_folder = "output";


    public static void main(String[] args) {

        String folderName = args[0];
        String splitter = File.separator.replace("\\","\\\\");
        String[] path = folderName.split(splitter);
        String projectName = path[path.length - 1];

        List<String> allFiles = FileUtil.getAllFileNames(folderName);
        JsonObject AntiPatternsJson = new JsonObject();

        JsonObject ThrowsKitchenSinkJson = AntiPatternFinder.getAntiPatternOccurrences("ThrowsKitchenSink", allFiles);
        AntiPatternsJson.add("ThrowsKitchenSink", ThrowsKitchenSinkJson);


        JsonObject DestructiveWrappingJson = AntiPatternFinder.getAntiPatternOccurrences("DestructiveWrapping", allFiles);
        AntiPatternsJson.add("DestructiveWrapping", DestructiveWrappingJson);

        JsonObject ThrowsGenericJson = AntiPatternFinder.getAntiPatternOccurrences("ThrowsGeneric", allFiles);
        AntiPatternsJson.add("ThrowsGeneric", ThrowsGenericJson);


        FileUtil.writeJsonFile(AntiPatternsJson, output_folder,  projectName);
    }
}