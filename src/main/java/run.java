import Analysis.ArithmeticExpressionTester;
import Analysis.LangAnalyzer;
import Analysis.RegexStrings;
import Analysis.Results.AnalysisOutput;

import java.util.HashSet;


public class run {
    static public void main(String[] args) {
        //Scanner scanner = new Scanner(System.in);
        //String file = scanner.nextLine();
        HashSet<String> identifiers = new HashSet<>();
        identifiers.add("i");
        identifiers.add("j");
        identifiers.add("p");
        identifiers.add("k");

        //(p+6(i)*k)^2*3/i*j*3/50
        //(p+(i)6*k)^2*3/i*j*3/50
        //(p+6*(i)*k)^2*3/i*j*3/50

        //Son validas estas operaciones? en la grmática del problema?
        //Creo que no y funciona tomandolas como no válidas
        // 6(i)
        // (i)2

        // (6)2
        // 2(6)

        int i = 2;
        int p= (((i)/2*2+2*6/(2)));

        boolean valid = ArithmeticExpressionTester.checkExpression("p+6*(i)*k)^2*3/i*j*3/50", identifiers);
        System.out.println(valid);

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
