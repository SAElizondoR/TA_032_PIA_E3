import Analysis.LangAnalyzer;
import Analysis.RegexStrings;
import Analysis.Results.AnalysisOutput;

import java.util.Scanner;

public class run {
    static public void main(String[] args) {
        //Scanner scanner = new Scanner(System.in);
        //String file = scanner.nextLine();
        LangAnalyzer analyzer = new LangAnalyzer();
        AnalysisOutput output = analyzer.checkProgram("C:\\Users\\garza\\OneDrive\\TA_032_PIA_E3\\Ejemplo.txt");
        if(output.getStatus() != AnalysisOutput.Status.NO_ERROR)
        {
            System.out.println("ERROR EN LINEA : " + output.getErrorLine());
            System.out.println(output.getCause());
            return;
        }
        System.out.println("NO ERROR");
    }
}
