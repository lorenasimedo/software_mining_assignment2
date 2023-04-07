package ca.concordia.soen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.util.List;

class AntiPatternFinder {


  public static JsonObject getAntiPatternOccurrences( String antiPatternType, List<String> allFiles) {

    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
    int projectAntiPatternOccurrencesCount = 0;
    JsonObject antiPatternOccurrencesJson = new JsonObject();

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

      AntiPatternVisitor visitor;
      switch (antiPatternType) {
        case "ThrowsKitchenSink" -> visitor = new ThrowsKitchenSinkVisitor(compilationUnit);
        case "ThrowsGeneric" -> visitor = new ThrowsGenericVisitor(compilationUnit);
        case "DestructiveWrapping" -> visitor = new DestructiveWrappingVisitor(compilationUnit);
        default -> {
          System.out.println("The anti-pattern" + antiPatternType + " is not supported by the current implementation");
          return antiPatternOccurrencesJson;
        }
      }
      compilationUnit.accept(visitor);

      if (visitor.antiPatternOccurrencesCount > 0) {
        projectAntiPatternOccurrencesCount += visitor.antiPatternOccurrencesCount;

        JsonObject fileJson = new JsonObject();
        fileJson.addProperty("Number_of_occurrences_in_the_file", visitor.antiPatternOccurrencesCount);

        JsonArray occurrencesArray = new JsonArray();
        for (AntiPatternOccurrence ThrowsKitchenSinkOccurrence : visitor.antiPatternOcurrencesList) {
          JsonObject occurrenceJson = new JsonObject();
          occurrenceJson.addProperty("Function_name", ThrowsKitchenSinkOccurrence.functionName());
          occurrenceJson.addProperty("Line_number", ThrowsKitchenSinkOccurrence.startingLine());
          occurrencesArray.add(occurrenceJson);
        }

        antiPatternOccurrencesJson.add(filename, occurrencesArray);
      }

    }
    antiPatternOccurrencesJson.addProperty("Number_of_occurrences_in_the_project", projectAntiPatternOccurrencesCount);
    return antiPatternOccurrencesJson;
  }

}
