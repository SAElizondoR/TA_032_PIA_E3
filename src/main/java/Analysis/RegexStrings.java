package Analysis;

public class RegexStrings {

    public static final String PROGRAM_NAME = "([a-z])([0-9a-z]*)";
    public static final String IDENTIFIER_NAME = "([a-z])([0-9a-z]*)";
    public static final String NUMBER = "([0-9]*)";
    public static final String LANG = "([a-z0-9])";

    public static final String PARENTHESIS = "\\(([A-D]|[S])\\)";
    public static final String SIGN = "([\\+\\-]{2})([C])";
    public static final String POW = "B([|^])C";
    public static final String MD = "A[*]B|A[/]B";
    public static final String SR = "S[+]A|S[-]A";
}
