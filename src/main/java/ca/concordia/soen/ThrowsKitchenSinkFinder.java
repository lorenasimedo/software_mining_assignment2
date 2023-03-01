package ca.concordia.soen;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

class ThrowsKitchenSinkFinder {


  public static JsonObject getThrowsKitchenSinkOccurrences( List<String> allFiles) {

    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
    int projectAntiPatternOccurrencesCount = 0;
    JsonObject  ThrowsKitchenSinkOccurrencesJson = new JsonObject();

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
      ThrowsKitchenSinkVisitor visitor = new ThrowsKitchenSinkVisitor(compilationUnit);
      compilationUnit.accept(visitor);

      if (visitor.AntiPatternOccurrencesCount > 0) {
        projectAntiPatternOccurrencesCount += visitor.AntiPatternOccurrencesCount;

        JsonObject fileJson = new JsonObject();
        fileJson.addProperty("Number_of_occurrences_in_the_file", visitor.AntiPatternOccurrencesCount);

        JsonArray occurrencesArray = new JsonArray();
        for (AntiPatternOccurrence ThrowsKitchenSinkOccurrence : visitor.ThrowsKitchenSinkOcurrencesList) {
          JsonObject occurrenceJson = new JsonObject();
          occurrenceJson.addProperty("Function_name", ThrowsKitchenSinkOccurrence.getFunctionName());
          occurrenceJson.addProperty("Line_number", ThrowsKitchenSinkOccurrence.getStartingLine());
          occurrencesArray.add(occurrenceJson);
        }

        ThrowsKitchenSinkOccurrencesJson.add(filename, occurrencesArray);
      }

    }
    ThrowsKitchenSinkOccurrencesJson.addProperty("Number_of_occurrences_in_the_project", projectAntiPatternOccurrencesCount);
    return ThrowsKitchenSinkOccurrencesJson;
  }

}
