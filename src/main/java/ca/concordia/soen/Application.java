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

        JsonObject ThrowsKitchenSinkJson = ThrowsKitchenSinkFinder.getThrowsKitchenSinkOccurrences(allFiles);
        //TODO: Add the other antipatterns here

        AntiPatternsJson.add("ThrowsKitchenSink", ThrowsKitchenSinkJson);
        FileUtil.writeJsonFile(AntiPatternsJson, "output",  projectName);
    }
}
