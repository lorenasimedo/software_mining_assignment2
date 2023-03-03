package ca.concordia.soen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.util.List;

public class ThrowsGenericFinder {
    public static JsonObject getThrowsGenericFinderOccurences(List<String> allFiles) {

        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        int projectAntiPatternOccurrencesCount = 0;
        JsonObject  ThrowsGenericOccurrencesJson = new JsonObject();

        for (String filename : allFiles) {
            String source;
            try {
                source = FileUtil.read(filename);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            parser.setSource(source.toCharArray());
            CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
            ThrowsGenericVisitor visitor = new ThrowsGenericVisitor(compilationUnit);
            compilationUnit.accept(visitor);

            if (visitor.AntiPatternOccurrencesCount > 0) {
                projectAntiPatternOccurrencesCount += visitor.AntiPatternOccurrencesCount;

                JsonObject fileJson = new JsonObject();
                fileJson.addProperty("Number_of_occurrences_in_the_file", visitor.AntiPatternOccurrencesCount);

                JsonArray occurrencesArray = new JsonArray();
                for (AntiPatternOccurrence ThrowsGenericOccurrence : visitor.ThrowsGenericOcurrencesList) {
                    JsonObject occurrenceJson = new JsonObject();
                    occurrenceJson.addProperty("Function_name", ThrowsGenericOccurrence.getFunctionName());
                    occurrenceJson.addProperty("Line_number", ThrowsGenericOccurrence.getStartingLine());
                    occurrencesArray.add(occurrenceJson);
                }

                ThrowsGenericOccurrencesJson.add(filename, occurrencesArray);
            }

        }
        ThrowsGenericOccurrencesJson.addProperty("Number_of_occurrences_in_the_project", projectAntiPatternOccurrencesCount);
        return ThrowsGenericOccurrencesJson;
    }
}
