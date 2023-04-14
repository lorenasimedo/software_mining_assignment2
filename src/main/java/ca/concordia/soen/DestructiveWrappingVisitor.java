package ca.concordia.soen;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

public class DestructiveWrappingVisitor extends AntiPatternVisitor {


	public DestructiveWrappingVisitor(CompilationUnit compilationUnit) {
		super(compilationUnit);
	}

	@Override
	public boolean visit(MethodDeclaration method) {
		if (method.getBody() != null) {

			method.getBody().accept(new ASTVisitor() {

				@Override
				public boolean visit(CatchClause clause) {

					if (!clause.getBody().statements().isEmpty()) {

						SingleVariableDeclaration exception = clause.getException();
						
						String exceptionType = exception.getType().toString();
						
						clause.getBody().accept(new ASTVisitor() {

							public boolean visit(ThrowStatement node) {

								Expression expression = node.getExpression();
								if (expression instanceof ClassInstanceCreation) {
						            ClassInstanceCreation classInstanceCreation;
									classInstanceCreation = (ClassInstanceCreation) expression;
						            

						            if (!exceptionType.equals(classInstanceCreation.getType().toString())){
										antiPatternOccurrencesCount += 1;
										int startLine = compilationUnit.getLineNumber(node.getStartPosition());
										String functionName = method.getName().toString();
										AntiPatternOccurrence DestructiveWrappingOccurrence = new AntiPatternOccurrence(functionName, Integer.toString(startLine));
										antiPatternOcurrencesList.add(DestructiveWrappingOccurrence);
						            }
						        }
						        
								
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
