import Analysis.ArithmeticExpressionTester;
import Analysis.LangAnalyzer;
import Analysis.RegexStrings;
import Analysis.Results.AnalysisOutput;
import Analysis.Results.ArithmeticExpressionInfo;

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
        int p = 2;
        int k = 2;
        int j =2;
        int l = (p+6*(i)*k)^2*3/i*j*3/50;

        //Es un numero sólo válido?
        ArithmeticExpressionInfo info = ArithmeticExpressionTester.checkExpression("(p+6*(i)*k)^2*3/i*j*3/50", identifiers);

        if(info.getStatus() == AnalysisOutput.Status.NO_ERROR)
            System.out.println("EXPRESION VALIDA");
        else
            System.out.println("EXPRESION INVALIDA");

        LangAnalyzer analyzer = new LangAnalyzer();
        AnalysisOutput output = analyzer.checkProgram("C:\\Users\\garza\\OneDrive\\TA_032_PIA_E3\\Ejemplo.txt");
        if(output.getStatus() != AnalysisOutput.Status.NO_ERROR)
        {
            System.out.println("ERROR EN LINEA : " + output.getErrorLine());
            System.out.println(output.getCause());
            return;
        }
        System.out.println("PROGRAMA SIN ERRORES");
    }
}
