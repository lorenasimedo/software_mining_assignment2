package ca.concordia.soen;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class AntiPatternVisitor extends ASTVisitor {

    protected final CompilationUnit compilationUnit;
    protected int antiPatternOccurrencesCount = 0;
    List<AntiPatternOccurrence> antiPatternOcurrencesList = new ArrayList<>();

    public AntiPatternVisitor(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

}
