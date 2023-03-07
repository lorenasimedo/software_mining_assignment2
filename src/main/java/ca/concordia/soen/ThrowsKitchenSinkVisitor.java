package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;


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

        // Handling throws in method declaration
        if (thrownExceptions.size() >= exceptionsThreshold) {
            addNewAntiPatternOccurrence(declaration);
            return true;
        }

        // Handling throws in the code
        Block body = declaration.getBody();
        List<Type> exceptionTypesList = new ArrayList<>();
        if (body != null) {
            for (Object statement : body.statements()) {
                if (statement instanceof ThrowStatement throwStatement) {
                    Expression expression = throwStatement.getExpression();
                    if (expression instanceof ClassInstanceCreation exception) {
                        Type exceptionType = exception.getType();
                        if (!exceptionTypesList.contains(exceptionType)) {
                            exceptionTypesList.add(exceptionType);
                        }
                    }
                }
            }
        }
        if (exceptionTypesList.size() >= exceptionsThreshold){
            addNewAntiPatternOccurrence(declaration);
            return true;
        }
        return true;
    }


    public void addNewAntiPatternOccurrence(MethodDeclaration declaration){
        AntiPatternOccurrencesCount += 1;
        int starLine = compilationUnit.getLineNumber(declaration.getStartPosition());
        String functionName = declaration.getName().toString();
        AntiPatternOccurrence ThrowsKitchenSinkOccurrence = new AntiPatternOccurrence(functionName, Integer.toString(starLine));
        ThrowsKitchenSinkOcurrencesList.add(ThrowsKitchenSinkOccurrence);
    }
}
