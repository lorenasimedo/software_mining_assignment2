package ca.concordia.soen;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

class ThrowsDestructiveWrappingFinder {


  public static JsonObject getThrowsDestructiveWrappingOccurrences( List<String> allFiles) {

    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
    int projectAntiPatternOccurrencesCount = 0;
    JsonObject  ThrowsDestructiveWrappingOccurrencesJson = new JsonObject();

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
      ThrowsDestructiveWrappingVisitor visitor = new ThrowsDestructiveWrappingVisitor(compilationUnit);
      compilationUnit.accept(visitor);

      if (visitor.AntiPatternOccurrencesCount > 0) {
        projectAntiPatternOccurrencesCount += visitor.AntiPatternOccurrencesCount;

        JsonObject fileJson = new JsonObject();
        fileJson.addProperty("Number_of_occurrences_in_the_file", visitor.AntiPatternOccurrencesCount);

        JsonArray occurrencesArray = new JsonArray();
        for (AntiPatternOccurrence ThrowsDestructiveWrappingOccurrence : visitor.ThrowsDestructiveWrappingOcurrencesList) {
          JsonObject occurrenceJson = new JsonObject();
          occurrenceJson.addProperty("Function_name", ThrowsDestructiveWrappingOccurrence.getFunctionName());
          occurrenceJson.addProperty("Line_number", ThrowsDestructiveWrappingOccurrence.getStartingLine());
          occurrencesArray.add(occurrenceJson);
        }

        ThrowsDestructiveWrappingOccurrencesJson.add(filename, occurrencesArray);
      }

    }
    ThrowsDestructiveWrappingOccurrencesJson.addProperty("Number_of_occurrences_in_the_project", projectAntiPatternOccurrencesCount);
    return ThrowsDestructiveWrappingOccurrencesJson;
  }

}
