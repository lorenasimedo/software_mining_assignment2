package ca.concordia.soen;

public class AntiPatternOccurrence {

    private final String functionName;
    private final String startingLine;

    public AntiPatternOccurrence(String functionName, String startingLine) {
        this.functionName = functionName;
        this.startingLine = startingLine;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getStartingLine() {
        return startingLine;
    }
}
