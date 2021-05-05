package Analysis;

import Analysis.Results.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class SentenceAnalyzer {
    public enum SentenceType {Operation, Print, Read, Header, Footer, Start}

    private static final String LEER = "leer ";
    private static final String IMPRIMIR = "imprimir ";
    private static final String PROGRAMA = "programa ";
    private static final String INICIAR = "iniciar";
    private static final String TERMINAR = "terminar.";
    private static final String ASIGNACION = ":=";

    public SentenceType getSentenceType(String sentence)
    {
        if(sentence.startsWith(LEER))
            return SentenceType.Read;
        if(sentence.startsWith(IMPRIMIR))
            return SentenceType.Print;
        if(sentence.startsWith(PROGRAMA))
            return SentenceType.Header;
        if(sentence.equals(INICIAR))
            return SentenceType.Start;
        if(sentence.equals(TERMINAR))
            return SentenceType.Footer;
        return  SentenceType.Operation;
    }

    public boolean endsWithSC(String sentence)
    {
        return sentence.endsWith(";");
    }

    public ReadInfo getLineReadInfo(String sentence)
    {
        ReadInfo readInfo = new ReadInfo();
        if(!endsWithSC(sentence))
        {
            readInfo.setStatus(AnalysisOutput.Status.SYNTAX_ERROR);
            readInfo.setErrorCause("No termina en ;");
            return readInfo;
        }

        String identifier = sentence.substring(LEER.length(), sentence.length()-1);
        if(!identifier.matches(RegexStrings.IDENTIFIER_NAME))
        {
            readInfo.setStatus(AnalysisOutput.Status.LEXICAL_ERROR);
            readInfo.setErrorCause("Nombre inválido");
            return readInfo;
        }

        readInfo.setStatus(AnalysisOutput.Status.NO_ERROR);
        readInfo.setIdentifier(identifier);
        return readInfo;
    }

    public PrintInfo getLinePrintInfo(String sentence)
    {
        PrintInfo printInfo = new PrintInfo();
        if(!endsWithSC(sentence))
        {
            printInfo.setStatus(AnalysisOutput.Status.SYNTAX_ERROR);
            printInfo.setErrorCause("No termina en ;");
            return printInfo;
        }

        String identifier = sentence.substring(IMPRIMIR.length(), sentence.length()-1);
        if(!identifier.matches(RegexStrings.IDENTIFIER_NAME))
        {
            printInfo.setStatus(AnalysisOutput.Status.LEXICAL_ERROR);
            printInfo.setErrorCause("Nombre inválido");
            return printInfo;
        }
        printInfo.setStatus(AnalysisOutput.Status.NO_ERROR);
        printInfo.setIdentifier(identifier);
        return printInfo;
    }

    public OperationInfo getOperationInfo(String sentence)
    {
        OperationInfo operationInfo = new OperationInfo();
        if(!endsWithSC(sentence))
        {
            operationInfo.setStatus(AnalysisOutput.Status.SYNTAX_ERROR);
            operationInfo.setErrorCause("No termina en ;");
            return operationInfo;
        }

        char[] chars = sentence.toCharArray();
        StringBuilder builder =new StringBuilder();

        int identifierEnd = 0;
        for(char c : chars)
        {
            if(Character.isWhitespace(c))
                break;
            builder.append(c);
            identifierEnd++;
        }

        String identifier = builder.toString();
        if(!identifier.matches(RegexStrings.IDENTIFIER_NAME)) //Si el identificador es inválido
        {
            operationInfo.setStatus(AnalysisOutput.Status.LEXICAL_ERROR);
            operationInfo.setErrorCause("Nombre inválido");
            return operationInfo;
        }

        String rest = sentence.substring(identifierEnd+1);
        if(!rest.startsWith(":="))
        {
            operationInfo.setStatus(AnalysisOutput.Status.SYNTAX_ERROR);
            operationInfo.setErrorCause("Se esperaba operador se asignación");
            return operationInfo;
        }


        rest = rest.substring(ASIGNACION.length()+1, rest.length()-1);

        operationInfo.setIdentifier(identifier);
        operationInfo.setExpression(rest);
        operationInfo.setStatus(AnalysisOutput.Status.NO_ERROR);
        return operationInfo;
    }

    public HeaderInfo getHeaderInfo(String sentence)
    {
        HeaderInfo headerInfo = new HeaderInfo();
        if(!endsWithSC(sentence))
        {
            headerInfo.setStatus(AnalysisOutput.Status.SYNTAX_ERROR);
            headerInfo.setErrorCause("No termina en ;");
            return headerInfo;
        }
        String identifier = sentence.substring(IMPRIMIR.length(), sentence.length()-1);
        if(!identifier.matches(RegexStrings.IDENTIFIER_NAME))
        {
            headerInfo.setStatus(AnalysisOutput.Status.LEXICAL_ERROR);
            headerInfo.setErrorCause("Nombre inválido");
            return headerInfo;
        }
        headerInfo.setStatus(AnalysisOutput.Status.NO_ERROR);
        headerInfo.setIdentifier(identifier);
        return headerInfo;
    }

    public FooterInfo getFooterInfo(String sentence)
    {
        FooterInfo footerInfo = new FooterInfo();
        footerInfo.setStatus(AnalysisOutput.Status.NO_ERROR);
        return footerInfo;
    }

    public StartInfo getStartInfo(String sentence)
    {
        StartInfo startInfo = new StartInfo();
        startInfo.setStatus(AnalysisOutput.Status.NO_ERROR);
        return startInfo;
    }
}
