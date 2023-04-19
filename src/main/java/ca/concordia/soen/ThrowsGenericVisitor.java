package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ThrowsGenericVisitor extends AntiPatternVisitor {
    public ThrowsGenericVisitor(CompilationUnit compilationUnit) {
        super(compilationUnit);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        List<Type> methodDeclarationThrownExceptions = node.thrownExceptionTypes();
        Block body = node.getBody();
        if (methodDeclarationThrownExceptions.size() == 1) {
            if (methodDeclarationThrownExceptions.get(0) instanceof SimpleType && methodDeclarationThrownExceptions.get(0).toString().equals("Exception")) {
                antiPatternDetect(node, methodDeclarationThrownExceptions.get(0).getStartPosition());
            }
        } else if (methodDeclarationThrownExceptions.size() == 0) {
            List<TryStatement> tryStatements = findTryStatements(body);
            for (TryStatement tryStatement : tryStatements) {
                List<CatchClause> catchClauses = tryStatement.catchClauses();
                checkCatchClausesForThrowsGeneric(catchClauses, node);
            }
        }
        return true;
    }

    private void checkCatchClausesForThrowsGeneric(List<CatchClause> catchClauses, MethodDeclaration node) {
        for (CatchClause catchClause : catchClauses) {
            if ("Exception".equals(catchClause.getException().getType().toString())) {
                catchClause.getBody().accept(new ASTVisitor() {
                    @Override
                    public boolean visit(ThrowStatement throwStatement) {
                        Expression exception = throwStatement.getExpression();
                        SimpleName exceptionName = catchClause.getException().getName();
                        if (exception instanceof SimpleName && ((SimpleName) exception).getIdentifier().equals(exceptionName.getIdentifier())) {
                            antiPatternDetect(node, throwStatement.getStartPosition());
                        }
                        return super.visit(throwStatement);
                    }
                });
            }
        }
    }

    public void processCatchStatements(List<Statement> catchStatements, SimpleName exceptionName, MethodDeclaration node) {
        try {
            for (Statement catchStatement : catchStatements) {
                if (catchStatement instanceof ThrowStatement) {
                    Expression exception = ((ThrowStatement) catchStatement).getExpression();
                    if (exception instanceof SimpleName && ((SimpleName) exception).getIdentifier().equals(exceptionName.getIdentifier())) {
                        antiPatternDetect(node, catchStatement.getStartPosition());
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void antiPatternDetect(MethodDeclaration node, int startPosition) {
        int startLine = compilationUnit.getLineNumber(startPosition);
        antiPatternOccurrencesCount +=1;
        AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(startLine));
        antiPatternOcurrencesList.add(ThrowsGenericOccurrence);
    }

//    public static boolean hasTryCatch(Block block) {
//        if (block != null){
//            for (Object statement : block.statements()) {
//                if (statement instanceof TryStatement) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public static List<TryStatement> findTryStatements(Block block) {
        final List<TryStatement> tryStatements = new ArrayList<>();
        if (block != null) {
            block.accept(new ASTVisitor() {
                @Override
                public boolean visit(TryStatement tryStatement) {
                    tryStatements.add(tryStatement);
                    return super.visit(tryStatement);
                }
            });
        }
        return tryStatements;
    }

}
