package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Set;

public class ThrowsKitchenSinkVisitor extends AntiPatternVisitor {
    static int exceptionsThreshold = 3;

    public ThrowsKitchenSinkVisitor(CompilationUnit compilationUnit) {
        super(compilationUnit);
    }

    @Override
    public boolean visit(MethodDeclaration declaration) {

        var thrownExceptions = declaration.thrownExceptionTypes();

        // Convert thrownExceptions to a set of Strings
        Set<String> thrownExceptionsSet = new HashSet<>();
        for (Object thrownException : thrownExceptions) {
            thrownExceptionsSet.add(thrownException.toString());
        }

        // Handling throws in method declaration
        if (thrownExceptionsSet.size() >= exceptionsThreshold) {
            addNewAntiPatternOccurrence(declaration);
            return true;
        }

        // Handling throws in the code
        Set<String> codeExceptionsSet = new HashSet<>();
        Block body = declaration.getBody();
        if (body == null){
            return true; // It is not an occurrence
        }
        body.accept(new ASTVisitor() {

            public boolean visit(ThrowStatement node) {
                Expression expression = node.getExpression();
                if (expression instanceof ClassInstanceCreation exception) {
                    Type exceptionType = exception.getType();
                    String exceptionTypeName = exceptionType.toString();
                    codeExceptionsSet.add(exceptionTypeName);
                }

                return super.visit(node);
            }

        });

        if (codeExceptionsSet.size() >= exceptionsThreshold){
            addNewAntiPatternOccurrence(declaration);
            return true;
        }

        // Merging both to check if together they exceed the threshold
        Set<String> mergedExceptions = new HashSet<>(thrownExceptionsSet);
        mergedExceptions.addAll(codeExceptionsSet);

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
