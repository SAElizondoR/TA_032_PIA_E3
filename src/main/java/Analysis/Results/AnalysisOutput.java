package Analysis.Results;

public class AnalysisOutput {
   public enum Status{NO_ERROR, LEXICAL_ERROR, SYNTAX_ERROR, COULD_NOT_OPEN_FILE, BAD_EXPRESSION}

    private Status status = Status.COULD_NOT_OPEN_FILE;
    private int errorLine = 0;
    private String cause = null;

    public Status getStatus() {
        return status;
    }

    public int getErrorLine() {
        return errorLine;
    }

    public String getCause() {
        return cause;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setErrorLine(int errorLine) {
        this.errorLine = errorLine;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
