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
        boolean throwInTry = false;
        boolean throwInCatch = false;
        boolean throwDetected = false;
        boolean genericExceptionInCatch = false;
        boolean containsOnlyExceptionInTry = false;
        Block body = node.getBody();
        if(thrownExceptions.size() == 1){
            for (Type thrownException : thrownExceptions) {
                if (thrownException instanceof SimpleType && thrownException.toString().equals("Exception")) {
                    if (hasTryCatch(body)){
                        for (Statement statement : (List<Statement>) body.statements()) {
                            if (statement.getNodeType() == ASTNode.TRY_STATEMENT) {
                                TryStatement tryStatement = (TryStatement) statement;
                                List<Statement> tryStatementsInBody = tryStatement.getBody().statements();
                                containsOnlyExceptionInTry = containsOnlyExceptionThrowStatements(tryStatementsInBody);
                                List<CatchClause> catchClauses = tryStatement.catchClauses();
                                for (CatchClause catchClause: catchClauses){
                                    if (!catchClause.getException().getType().toString().equals("Exception")){
                                        genericExceptionInCatch = false;
                                    }
                                    else {
                                        genericExceptionInCatch = true;
                                    }
                                    break;
                                }
                            }
                        }
                        if(genericExceptionInCatch && containsOnlyExceptionInTry){
                            antiPatternDetect(node, cu, thrownException.getStartPosition());
                        }
                    }
                    else {
                        if (body.statements().isEmpty()) throwDetected = true;
                        for (Statement statement : (List<Statement>) body.statements()) {
                            if(statement.getNodeType() == ASTNode.THROW_STATEMENT){
                                Expression expression = ((ThrowStatement) statement).getExpression();
                                if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION){
                                    ClassInstanceCreation cic = (ClassInstanceCreation) expression;
                                    if (cic.getType().toString().equals("Exception")){
                                        throwDetected = true;
                                    }
                                    break;
                                }
                            }
                            else {
                                throwDetected = true;
                            }
                        }
                        if (throwDetected){
                            antiPatternDetect(node, cu, thrownException.getStartPosition());
                        }
                    }
                }
            }
        }
        else if (thrownExceptions.size() == 0){
            Statement s = null;
            int throwCount = 0;
            if(body != null){
                for (Statement statement : (List<Statement>) body.statements()) {
                    if (statement.getNodeType() == ASTNode.TRY_STATEMENT) {
                        TryStatement tryStatement = (TryStatement) statement;
                        Block tryBody = tryStatement.getBody();
                        List<Statement> tryStatementsInBody = tryBody.statements();
                        if (tryStatementsInBody.isEmpty()) {
                            throwInTry = true;
                            s = statement;
                        }
                        for (Statement tryStatementInBody : tryStatementsInBody){
                            if (tryStatementInBody.getNodeType() == ASTNode.THROW_STATEMENT){
                                throwCount += 1;
                                Expression expression = ((ThrowStatement) tryStatementInBody).getExpression();
                                if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION){
                                    ClassInstanceCreation cic = (ClassInstanceCreation) expression;
                                    if (cic.getType().toString().equals("Exception")){
                                        throwInTry = true;
                                        s = tryStatementInBody;
                                    }
                                    else {
                                        throwInTry = false;
                                    }
                                    break;
                                }
                            }
                            else {
                                throwInTry = true;
                            }
                        }

                        List<CatchClause> catchClauses = tryStatement.catchClauses();
                        if (catchClauses.size() == 1 && catchClauses.get(0).getException().getType().toString().equals("Exception")){
                            Block catchBody = catchClauses.get(0).getBody();
                            List<Statement> catchStatementsInBody = catchBody.statements();
                            if (catchStatementsInBody.size() == 0){
                                throwInCatch = true;
                            }
                            else {
                                for(Statement catchStatementInBody : catchStatementsInBody){
                                    if (catchStatementInBody.getNodeType() == ASTNode.THROW_STATEMENT){
                                        throwCount += 1;
                                        Expression expression = ((ThrowStatement) catchStatementInBody).getExpression();
                                        if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION){
                                            ClassInstanceCreation cic = (ClassInstanceCreation) expression;
                                            if (cic.getType().toString().equals("Exception")){
                                                throwInCatch = true;
                                            }
                                            else {
                                                throwInCatch = false;
                                            }
                                            break;
                                        }
                                    }
                                    else {
                                        throwInCatch = true;
                                    }
                                }
                            }
                        }
                    }

                    if (throwInCatch && throwInTry && throwCount>0){
                        antiPatternDetect(node, cu, s.getStartPosition());
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
    public boolean containsOnlyExceptionThrowStatements(List<Statement> bodyStatements) {
        boolean foundException = true;
        //System.out.println(bodyStatements);
        for (Statement statement : bodyStatements) {
            System.out.println(statement.getNodeType());
            if(statement.getNodeType() == ASTNode.THROW_STATEMENT){
                Expression expression = ((ThrowStatement) statement).getExpression();
                if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION){
                    ClassInstanceCreation cic = (ClassInstanceCreation) expression;
                    if (!cic.getType().toString().equals("Exception")) {
                        foundException = false;
                        break;
                    }
                }
            }
        }

        return foundException;
    }

}
