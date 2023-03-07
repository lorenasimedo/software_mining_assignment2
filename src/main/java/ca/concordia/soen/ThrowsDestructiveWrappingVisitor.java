package ca.concordia.soen;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;

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


						clause.getBody().accept(new ASTVisitor() {

							public boolean visit(ThrowStatement node) {
								AntiPatternOccurrencesCount += 1;
								int startLine = compilationUnit.getLineNumber(method.getStartPosition());
								String functionName = method.getName().toString();
								AntiPatternOccurrence ThrowsDestructiveWrappingOccurrence = new AntiPatternOccurrence(functionName, Integer.toString(startLine));
								ThrowsDestructiveWrappingOcurrencesList.add(ThrowsDestructiveWrappingOccurrence);
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
