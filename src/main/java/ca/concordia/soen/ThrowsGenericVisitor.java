package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;

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
            if (hasTryCatch(body)) {
                for (Object statement : body.statements()) {
                    if (statement instanceof TryStatement tryStatement) {
                        List<CatchClause> catchClauses = tryStatement.catchClauses();
                        checkCatchClausesForThrowsGeneric(catchClauses, node);
                    }
                }
            }
        }
        return true;
    }

    private void checkCatchClausesForThrowsGeneric(List<CatchClause> catchClauses, MethodDeclaration node) {
        for (CatchClause catchClause : catchClauses) {
            if ("Exception".equals(catchClause.getException().getType().toString())) {
                SimpleName exceptionName = catchClause.getException().getName();
                catchClause.accept(new ASTVisitor() {
                    @Override
                    public boolean visit(ThrowStatement catchStatement) {
                        Expression exception = catchStatement.getExpression();
                        if (exception instanceof SimpleName && ((SimpleName) exception).getIdentifier().equals(exceptionName.getIdentifier())) {
                            antiPatternDetect(node, catchStatement.getStartPosition());
                        }
                        return super.visit(catchStatement);
                    }
                });
            }
        }
    }

    private void antiPatternDetect(MethodDeclaration node, int startPosition) {
        int startLine = compilationUnit.getLineNumber(startPosition);
        antiPatternOccurrencesCount += 1;
        AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(startLine));
        antiPatternOcurrencesList.add(ThrowsGenericOccurrence);
    }

    public static boolean hasTryCatch(Block block) {
        if (block != null) {
            for (Object statement : block.statements()) {
                if (statement instanceof TryStatement) {
                    return true;
                }
            }
        }
        return false;
    }

}
