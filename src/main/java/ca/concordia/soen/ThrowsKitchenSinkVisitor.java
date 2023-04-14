package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ThrowsKitchenSinkVisitor extends AntiPatternVisitor {
    static int exceptionsThreshold = 3;

    public ThrowsKitchenSinkVisitor(CompilationUnit compilationUnit) {
        super(compilationUnit);
    }

    @Override
    public boolean visit(MethodDeclaration declaration) {

        var thrownExceptions = declaration.thrownExceptionTypes();

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

        // Merging both to check if together they exceed the threshold
        List<Type> mergedExceptions;
        mergedExceptions = exceptionTypesList;
        for (Object exception : thrownExceptions) {
            if (!mergedExceptions.contains(exception)) {
                mergedExceptions.add((Type) exception);
            }
        }
        if (mergedExceptions.size() >= exceptionsThreshold){
            addNewAntiPatternOccurrence(declaration);
            return true;
        }

        // It is not an occurrence
        return true;
    }


    public void addNewAntiPatternOccurrence(MethodDeclaration declaration){
        antiPatternOccurrencesCount += 1;
        int startLine = compilationUnit.getLineNumber(declaration.getStartPosition());
        String functionName = declaration.getName().toString();
        AntiPatternOccurrence ThrowsKitchenSinkOccurrence = new AntiPatternOccurrence(functionName, Integer.toString(startLine));
        antiPatternOcurrencesList.add(ThrowsKitchenSinkOccurrence);
    }
}
