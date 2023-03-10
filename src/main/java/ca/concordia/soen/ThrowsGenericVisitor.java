package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ThrowsGenericVisitor  extends ASTVisitor {
    private final CompilationUnit compilationUnit;
    int AntiPatternOccurrencesCount = 0;
    List<AntiPatternOccurrence> ThrowsGenericOcurrencesList = new ArrayList<>();

    public ThrowsGenericVisitor(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        List<Type> thrownExceptions = node.thrownExceptionTypes();
        CompilationUnit cu = compilationUnit;
        Block body = node.getBody();
        if(thrownExceptions.size() == 1){
            if (thrownExceptions.get(0) instanceof SimpleType && thrownExceptions.get(0).toString().equals("Exception")) {
                antiPatternDetect(node, cu, thrownExceptions.get(0).getStartPosition());
            }
        }
        else if (thrownExceptions.size() == 0){
            if(hasTryCatch(body)){
                for(Statement statement: (List<Statement>) body.statements()){
                    if (statement.getNodeType() == ASTNode.TRY_STATEMENT) {
                        TryStatement tryStatement = (TryStatement) statement;
                        List<CatchClause> catchClauses = tryStatement.catchClauses();
                        for (CatchClause catchClause : catchClauses){
                            SimpleName exceptionName = catchClause.getException().getName();
                            if (catchClause.getException().getType().toString().equals("Exception")){
                                List<Statement> catchStatements = catchClause.getBody().statements();
                                if (catchStatements!=null){
                                    for (Statement catchStatement : catchStatements){
                                        if (catchStatement instanceof ThrowStatement) {
                                            Expression exception = ((ThrowStatement) catchStatement).getExpression();
                                            if (exception instanceof SimpleName && ((SimpleName) exception).getIdentifier().equals(exceptionName.getIdentifier())) {
                                                antiPatternDetect(node, cu, catchStatement.getStartPosition());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void antiPatternDetect(MethodDeclaration node, CompilationUnit cu, int startPosition) {
        int startLine = cu.getLineNumber(startPosition);
        AntiPatternOccurrencesCount +=1;
        AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(startLine));
        ThrowsGenericOcurrencesList.add(ThrowsGenericOccurrence);
    }

    public static boolean hasTryCatch(Block block) {
        for (Object statement : block.statements()) {
            if (statement instanceof TryStatement) {
                return true;
            }
        }
        return false;
    }

}
