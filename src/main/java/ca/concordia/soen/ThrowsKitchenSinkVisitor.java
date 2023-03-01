package ca.concordia.soen;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.ArrayList;
import java.util.List;

public class ThrowsKitchenSinkVisitor extends ASTVisitor {
    static int exceptionsThreshold = 3;

    private final CompilationUnit compilationUnit;
    int AntiPatternOccurrencesCount = 0;
    List<AntiPatternOccurrence> ThrowsKitchenSinkOcurrencesList = new ArrayList<>();

    public ThrowsKitchenSinkVisitor(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    @Override
    public boolean visit(MethodDeclaration declaration) {

        List thrownExceptions = declaration.thrownExceptionTypes();

        if (thrownExceptions.size() >= exceptionsThreshold) {
            AntiPatternOccurrencesCount += 1;
            int starLine = compilationUnit.getLineNumber(declaration.getStartPosition());
            String functionName = declaration.getName().toString();
            AntiPatternOccurrence ThrowsKitchenSinkOccurrence = new AntiPatternOccurrence(functionName, Integer.toString(starLine));
            ThrowsKitchenSinkOcurrencesList.add(ThrowsKitchenSinkOccurrence);
        }
        return false;
    }
}
