package ca.concordia.soen;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import com.google.gson.JsonObject;

import java.util.List;

public class Application {

    static String output_path = "../../../output/";

    public static void main(String[] args) {

        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

        String folderName = args[0];
        String[] path = folderName.split("/");
        String projectName = path[path.length - 1];

        List<String> allFiles = FileUtil.getAllFileNames(folderName);
        JsonObject AntiPatternsJson = new JsonObject();

        JsonObject ThrowsKitchenSinkJson = ThrowsKitchenSinkFinder.getThrowsKitchenSinkOccurrences(allFiles);
        //TODO: Add the other antipatterns here
        AntiPatternsJson.add("ThrowsKitchenSink", ThrowsKitchenSinkJson);
        FileUtil.writeJsonFile(AntiPatternsJson, "output",  projectName);
    }
}
