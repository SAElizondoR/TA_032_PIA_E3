package Analysis.Results;

public class ResultInfo {
    private String identifier = null;
    private String errorCause = null;
    public AnalysisOutput.Status status;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getErrorCause() {
        return errorCause;
    }

    public void setErrorCause(String errorCause) {
        this.errorCause = errorCause;
    }

    public AnalysisOutput.Status getStatus() {
        return status;
    }

    public void setStatus(AnalysisOutput.Status status) {
        this.status = status;
    }

    public AnalysisOutput makeOutput(int errorLine)
    {
        AnalysisOutput error = new AnalysisOutput();
        error.setStatus(status);
        error.setErrorLine(errorLine);
        error.setCause(errorCause);
        return error;
    }
}
