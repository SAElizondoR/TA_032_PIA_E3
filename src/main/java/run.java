import Analysis.*;
import Analysis.Results.AnalysisOutput;
import Analysis.Results.ArithmeticExpressionInfo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class run {
    private static ArrayList<String> subChains = new ArrayList<>();
    static public void main(String[] args) {
        LangAnalyzer analyzer = new LangAnalyzer("C:\\Users\\garza\\OneDrive\\TA_032_PIA_E3\\Ejemplo.txt");
        AnalysisOutput output = analyzer.checkProgram();
        PrintAnalyzer printAnalyzer = new PrintAnalyzer("j2");
        boolean isValid = printAnalyzer.isValid();
        if(output.getStatus() != AnalysisOutput.Status.NO_ERROR)
        {
            String errorType = "";
            switch (output.getStatus())
            {
                case SYNTAX_ERROR -> errorType="[ERROR DE SINTAXIS] ";
                case LEXICAL_ERROR -> errorType="[ERROR LÉXICO] ";
                case COULD_NOT_OPEN_FILE -> errorType="[NO SE PUDO ABRIR ARCHIVO] ";
            }
            System.out.println("ERROR EN LINEA : " + output.getErrorLine());
            System.out.println(errorType + output.getCause());
            return;
        }
        System.out.println("PROGRAMA SIN ERRORES");
    }

}
