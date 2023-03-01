package ca.concordia.soen;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

class ThrowsKitchenSinkFinder {


  public static void main(String[] args) {

    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
    String folderName = args[0];
    List<String> allFiles = FileUtil.getAllFileNames(folderName);
    int projectAntiPatternOccurrencesCount = 0;

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

        System.out.println("Number of occurrences on file " + filename + ": " + visitor.AntiPatternOccurrencesCount);

        int occurrenceCount = 0;
        for (AntiPatternOccurrence ThrowsKitchenSinkOccurrence : visitor.ThrowsKitchenSinkOcurrencesList) {
          occurrenceCount += 1;
          System.out.println("Occurrence " + occurrenceCount + " - Function name: " + ThrowsKitchenSinkOccurrence.getFunctionName() + ", line: " + ThrowsKitchenSinkOccurrence.getStartingLine());
        }
        System.out.println("---");
      }
    }

    System.out.println("\n-> Total number of  Throws Kitchen Sink anti-pattern occurrences in the project: " + projectAntiPatternOccurrencesCount);
  }

}
