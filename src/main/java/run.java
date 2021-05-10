import Analysis.*;
import Analysis.Results.AnalysisOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class run {
    private static ArrayList<String> subChains = new ArrayList<>();
    static public void main(String[] args) {

        boolean continuar = true;
        Scanner  reader = new Scanner (System.in);
        while(continuar) {
            System.out.println("Ingrese la ruta local del archivo con extensión");
            String name = "";
            name = reader.nextLine();

            System.out.println("Leyendo archivo : " + name);

            LangAnalyzer analyzer = new LangAnalyzer(name);
            AnalysisOutput output = analyzer.checkProgram();
            PrintAnalyzer printAnalyzer = new PrintAnalyzer("j2");
            boolean isValid = printAnalyzer.isValid();
            if (output.getStatus() != AnalysisOutput.Status.NO_ERROR) {
                String errorType = "";
                switch (output.getStatus()) {
                    case SYNTAX_ERROR:
                        errorType = "[ERROR DE SINTAXIS] ";
                        break;
                    case LEXICAL_ERROR:
                        errorType = "[ERROR LÉXICO] ";
                        break;
                    case COULD_NOT_OPEN_FILE:
                        errorType = "[NO SE PUDO ABRIR ARCHIVO] ";
                        break;
                }
                System.out.println("ERROR EN LINEA : " + output.getErrorLine());
                System.out.println(errorType + output.getCause());
            }
            else {
                System.out.println("PROGRAMA SIN ERRORES");
            }
            System.out.println("Desea ingresar otro archivo? y/n");
            char s = reader.next().charAt(0);
            if(s=='n'||s=='N')
              continuar = false;
            reader = new Scanner (System.in);

            for (int i = 0; i < 50; ++i) System.out.println();
        }
        reader.close();
    }

}
