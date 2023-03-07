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
        if(thrownExceptions.size() == 1){
            for (Type thrownException : thrownExceptions) {
                if (thrownException instanceof SimpleType && thrownException.toString().equals("Exception")) {
                    SimpleType type = (SimpleType) thrownException;
                    int startLine = cu.getLineNumber(type.getStartPosition());
                    int endLine = cu.getLineNumber(type.getStartPosition() + type.getLength() - 1);
                    AntiPatternOccurrencesCount +=1;
                    throwDetected = true;
                    AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(startLine));
                    ThrowsGenericOcurrencesList.add(ThrowsGenericOccurrence);
                }
            }
        }

        Block body = node.getBody();

        if(body != null && throwDetected == false){
//            for (Statement statement : (List<Statement>) body.statements()) {
//                if (statement.getNodeType() == ASTNode.THROW_STATEMENT) {
//                    Expression expression = ((ThrowStatement) statement).getExpression();
//                    if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
//                        ClassInstanceCreation cic = (ClassInstanceCreation) expression;
//                        if (cic.getType().toString().equals("Exception") && throwsGenericFound == false) {
//                            int lineNumber = cu.getLineNumber(statement.getStartPosition());
//                            AntiPatternOccurrencesCount +=1;
//                            AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(lineNumber));
//                            ThrowsGenericOcurrencesList.add(ThrowsGenericOccurrence);
//                        }
//                    }
//                }
//            }

            for (Statement statement : (List<Statement>) body.statements()) {
                if (statement.getNodeType() == ASTNode.TRY_STATEMENT) {
                    TryStatement tryStatement = (TryStatement) statement;
                    Block tryBody = tryStatement.getBody();
                    List<Statement> tryStatementsInBody = tryBody.statements();
                    for (Statement tryStatementInBody : tryStatementsInBody){
                        if (tryStatementInBody.getNodeType() == ASTNode.THROW_STATEMENT){
                            Expression expression = ((ThrowStatement) tryStatementInBody).getExpression();
                            if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION){
                                ClassInstanceCreation cic = (ClassInstanceCreation) expression;
                                if (cic.getType().toString().equals("Exception")){
                                    throwInTry = true;
                                }
                                else {
                                    throwInTry = false;
                                    break;
                                }
                            }
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
                                    Expression expression = ((ThrowStatement) catchStatementInBody).getExpression();
                                    if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION){
                                        ClassInstanceCreation cic = (ClassInstanceCreation) expression;
                                        if (cic.getType().toString().equals("Exception")){
                                            throwInCatch = true;
                                        }
                                        else {
                                            throwInCatch = false;
                                            break;
                                        }
                                    }
                                }
                                else {
                                    throwInCatch = true;
                                }
                            }
                        }
                    }
                }

                if (throwInCatch == true && throwInTry == true){
                    int lineNumber = cu.getLineNumber(statement.getStartPosition());
                    AntiPatternOccurrencesCount +=1;
                    AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(lineNumber));
                    ThrowsGenericOcurrencesList.add(ThrowsGenericOccurrence);

                }
            }
        }

        return true;
    }
}
