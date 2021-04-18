package Analysis.Results;

public class AnalysisOutput {
   public enum Status{NO_ERROR, LEXICAL_ERROR, SYNTAX_ERROR, ALREADY_DEFINED_VAR, NOT_DEFINED_VAR,
       COULD_NOT_OPEN_FILE, DUPLICATED_HEADER, DUPLICATED_FOOTER, DUPLICATED_START, NO_HEADER, NO_FOOTER,
       NO_START, BAD_START, BAD_END, UKNOWN, NO_SC, BAD_IDENTIFIER, PROGRAM_NOT_STARTED, EXPECTED_OPERATOR, BAD_EXPRESSION}

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
