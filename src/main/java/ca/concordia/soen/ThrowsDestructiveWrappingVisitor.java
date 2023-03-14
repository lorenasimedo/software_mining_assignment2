package ca.concordia.soen;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import java.util.ArrayList;
import java.util.List;

public class ThrowsDestructiveWrappingVisitor extends ASTVisitor {

	private final CompilationUnit compilationUnit;
	int AntiPatternOccurrencesCount = 0;
	List<AntiPatternOccurrence> ThrowsDestructiveWrappingOcurrencesList = new ArrayList<>();

	public ThrowsDestructiveWrappingVisitor(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	@Override
	public boolean visit(MethodDeclaration method) {
		if (method.getBody() != null) {

			method.getBody().accept(new ASTVisitor() {

				@Override
				public boolean visit(CatchClause clause) {

					if (clause.getBody().statements().isEmpty() == false) {

						SingleVariableDeclaration exception = clause.getException();
						
						String exceptionType = exception.getType().toString();

						System.out.println("The exception thrown in the Catch: " + exceptionType);
						
						clause.getBody().accept(new ASTVisitor() {

							public boolean visit(ThrowStatement node) {

								Expression expression = node.getExpression();
								if (expression instanceof ClassInstanceCreation) {
						            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
						            System.out.println("Thrown exception type: " + classInstanceCreation.getType().toString());
						            

						            if (exceptionType != classInstanceCreation.getType().toString()){
										AntiPatternOccurrencesCount += 1;
										int startLine = compilationUnit.getLineNumber(node.getStartPosition());
										String functionName = method.getName().toString();
										AntiPatternOccurrence ThrowsDestructiveWrappingOccurrence = new AntiPatternOccurrence(functionName, Integer.toString(startLine));
										ThrowsDestructiveWrappingOcurrencesList.add(ThrowsDestructiveWrappingOccurrence);
						            }
						        }
								System.out.println("Caught a throw statement: " + node.getExpression().toString());
						        
								
								return super.visit(node);
							}


						});

					}

					return true;

				}
			});

		}

		return true;
	}

}
