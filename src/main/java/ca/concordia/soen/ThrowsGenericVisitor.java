package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ThrowsGenericVisitor  extends ASTVisitor {
    static int exceptionsThreshold = 3;

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
        //System.out.println(thrownExceptions);
        if(thrownExceptions.size() == 1){
            for (Type thrownException : thrownExceptions) {
                if (thrownException instanceof SimpleType && thrownException.toString().equals("Exception")) {
                    SimpleType type = (SimpleType) thrownException;
                    int startLine = cu.getLineNumber(type.getStartPosition());
                    int endLine = cu.getLineNumber(type.getStartPosition() + type.getLength() - 1);
                    AntiPatternOccurrencesCount +=1;
                    AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(startLine));
                    ThrowsGenericOcurrencesList.add(ThrowsGenericOccurrence);
                }
            }
        }

        Block body = node.getBody();

        if(body != null){
            for (Statement statement : (List<Statement>) body.statements()) {
                if (statement.getNodeType() == ASTNode.THROW_STATEMENT) {
                    Expression expression = ((ThrowStatement) statement).getExpression();
                    if (expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
                        ClassInstanceCreation cic = (ClassInstanceCreation) expression;
                        if (cic.getType().toString().equals("Exception")) {
                            int lineNumber = cu.getLineNumber(statement.getStartPosition());
                            AntiPatternOccurrencesCount +=1;
                            AntiPatternOccurrence ThrowsGenericOccurrence = new AntiPatternOccurrence(node.getName().toString(), Integer.toString(lineNumber));
                            ThrowsGenericOcurrencesList.add(ThrowsGenericOccurrence);
                        }
                    }
                }
            }
        }

        return true;
    }
}
