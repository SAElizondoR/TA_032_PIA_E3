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
    static public void main(String[] args) {
        //Scanner scanner = new Scanner(System.in);
        //scanner.nextDouble();
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
        int l = ((2^3)*2^4/5)^(2*3);


        //ArithmeticBacktracer backtracer = new ArithmeticBacktracer("((2)+2+4^4)+(p+6*(i)*k)^2*3/i*j*3/50");

        //ArithmeticBacktracer.ArithmeticBacktrackerStatus valid = backtracer.checkExpr();
        //backtracer.checkExpr();


        //Es un numero sólo válido?
        /*
        ArithmeticExpressionInfo info = ArithmeticExpressionTester.checkExpression("(p+6*(i)*k)^2*3/i*j*3/50", identifiers);

        if(info.getStatus() == AnalysisOutput.Status.NO_ERROR)
            System.out.println("EXPRESION VALIDA");
        else
            System.out.println("EXPRESION INVALIDA");
        */
        LangAnalyzer analyzer = new LangAnalyzer("C:\\Users\\garza\\OneDrive\\TA_032_PIA_E3\\Ejemplo.txt");
        AnalysisOutput output = analyzer.checkProgram();
        if(output.getStatus() != AnalysisOutput.Status.NO_ERROR)
        {
            System.out.println("ERROR EN LINEA : " + output.getErrorLine());
            System.out.println(output.getCause());
            return;
        }
        System.out.println("PROGRAMA SIN ERRORES");
    }
}
