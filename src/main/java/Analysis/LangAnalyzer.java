package Analysis;

import Analysis.Results.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class LangAnalyzer {

    private int fileLine = 0;
    private HashSet<String> declaredIdentifiers = new HashSet<>();
    private HashSet<String> reservedWords = new HashSet<>();
    private String filename;

    public LangAnalyzer(String filename)
    {
        this.filename = filename;
        reservedWords.add("programa");
        reservedWords.add("iniciar");
        reservedWords.add("imprimir");
        reservedWords.add("leer");
        reservedWords.add("terminar");
    }

    public AnalysisOutput checkProgram() {
        try {
            return runTest(filename);
        } catch (FileNotFoundException e) {
            AnalysisOutput output = new AnalysisOutput();
            output.setStatus(AnalysisOutput.Status.COULD_NOT_OPEN_FILE);
            output.setCause("No se pudo abrir el archivo!");
            return output;
        }
    }
    public boolean isDefined(String identifier) {
        return declaredIdentifiers.contains(identifier);
    }

    //Retorna V si tod_o está bien
    public boolean registerIdentifier(String identifier) {
        if(!reservedWords.contains(identifier)) {
            declaredIdentifiers.add(identifier);
            return true;
        }
        return false;
    }

    //Método genérico para invalidar un programa cuya sentencia requiera estar dentro de un programa iniciada
    public AnalysisOutput makeInvalidProgram(int line, boolean hasHeader, boolean hasStart, boolean hasFooter, int footerLine) {
        AnalysisOutput error = new AnalysisOutput();
        if (!hasHeader) { //Si falta encabezado
            error.setStatus(AnalysisOutput.Status.NO_HEADER);
            error.setErrorLine(line);
            error.setCause("Sentencia inválida, programa no declarado!");
            return error;
        }

        if (!hasStart) { //Si falta inicio
            error.setStatus(AnalysisOutput.Status.PROGRAM_NOT_STARTED);
            error.setErrorLine(line);
            error.setCause("La sentencia requiere que se inicie el programa");
            return error;
        }

        if (footerLine != fileLine) { //Sentencias después de la terminación
            error.setStatus(AnalysisOutput.Status.BAD_END);
            error.setErrorLine(line);
            error.setCause("El hay más sentencias después del final del programa");
            return error;
        }

        error.setErrorLine(line);
        error.setCause("ERROR DESCONOCIDO");
        error.setStatus(AnalysisOutput.Status.UKNOWN);
        return error;
    }

    public HashSet<String> getDeclaredIdentifiers()
    {
        return  declaredIdentifiers;
    }

    public AnalysisOutput runTest(String filename) throws FileNotFoundException {
        //No cierro el scanner :p
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        LexicalAnalyzer analyzer = new LexicalAnalyzer();

        ProgramStatus programStatus = new ProgramStatus();
        AnalysisOutput error = new AnalysisOutput();


        while (scanner.hasNextLine())
        {
            fileLine++;
            String line = scanner.nextLine();
            if (line.isBlank()) //Si la linea es blanca la saltamos
            {
                error.setStatus(AnalysisOutput.Status.UKNOWN);
                error.setCause("Linea en blanco");
                error.setErrorLine(fileLine);
                return error;
            }

            LexicalAnalyzer.SentenceType type = analyzer.getSentenceType(line); //Sacamos el tipo

            switch (type) {
                case Read:
                    error = checkRead(line, programStatus);
                    if(error!=null)
                        return error;
                    break;
                case Print:
                    error = checkPrint(line, programStatus);
                    if(error!=null)
                        return error;
                    break;
                case Operation:
                    error = checkOperation(line, programStatus);
                    if(error!=null)
                        return error;
                    break;
                case Header:
                    error = checkHeader(line, programStatus);
                    if(error!=null)
                        return error;
                    break;

                case Start:
                    error = checkStart(line, programStatus);
                    if(error!=null)
                        return error;
                    break;

                case Footer:
                    error = checkFooter(line, programStatus);
                    if(error!=null)
                        return error;
                    break;
            }
        }


        if(!programStatus.hasOneInstruction)
        {
            error.setStatus(AnalysisOutput.Status.UKNOWN);
            error.setErrorLine(fileLine);
            error.setCause("El programa no tiene al menos una instrucción");
            return error;
        }

        //Si tiene footer quiere decir que tiene inicio y declaración de programa y si no ha tronado en algun otro lado
        //significa que tod_o está correcto
        if (programStatus.hasFooter) {
            AnalysisOutput noError = new AnalysisOutput();
            noError.setStatus(AnalysisOutput.Status.NO_ERROR);
            return noError;
        }
        else { //En caso de que tod_o el programa este correcto pero falte el footer
                error.setStatus(AnalysisOutput.Status.NO_FOOTER);
                error.setErrorLine(fileLine);
                error.setCause("El programa no tiene la sentencia terminar");
                return error;
        }
    }

    //REQUISITOS
    //La sentencia print equiere que se haya declarado un programa, se haya iniciado y que el programa
    //no se haya terminado
    private AnalysisOutput checkOperation(String line, ProgramStatus status) {
        AnalysisOutput error = new AnalysisOutput();
        if (status.hasStart && status.hasHeader && !status.hasFooter) {
            OperationInfo operationInfo = status.analyzer.getOperationInfo(line);

            if (operationInfo.getStatus() != AnalysisOutput.Status.NO_ERROR) //Si truena por un error lexico/gramático
                return operationInfo.makeOutput(fileLine);

            ArithmeticBacktracer backtracker = new ArithmeticBacktracer(operationInfo.getExpression());

            ArithmeticBacktracer.ArithmeticBacktrackerStatus expressionStatus =
                    backtracker.checkExpr();

            if(expressionStatus.status != AnalysisOutput.Status.NO_ERROR)
            {
                error.setCause(expressionStatus.errorCause);
                error.setErrorLine(fileLine);
                error.setStatus(expressionStatus.status);
                return error;
            }

            String notDeclaredIdentifier = backtracker.checkIdentifiers(declaredIdentifiers);
            if(notDeclaredIdentifier != null)
            {
                error.setCause("Identificador \"" + notDeclaredIdentifier + "\" no declarado");
                error.setErrorLine(fileLine);
                error.setStatus(AnalysisOutput.Status.NOT_DEFINED_VAR);
                return error;
            }

            boolean divisionBy0 = backtracker.checkDivisionBy0();
            if(divisionBy0)
            {
                error.setCause("Division entre 0");
                error.setErrorLine(fileLine);
                error.setStatus(AnalysisOutput.Status.UKNOWN);
                return error;
            }

            if(!registerIdentifier(operationInfo.getIdentifier()))
            {
                error.setStatus(AnalysisOutput.Status.BAD_IDENTIFIER);
                error.setCause("El identificador es una palabra reservada");
                error.setErrorLine(fileLine);
                return error;
            }
            if(!status.hasOneInstruction)
                status.hasOneInstruction=true;

        } else //Tiramos error
            return makeInvalidProgram(fileLine, status.hasHeader, status.hasStart, status.hasFooter, status.footerLine);
        return null;
    }

    //REQUISITOS
    //Que haya un start y no haya footers duplicados (El start se asegura que haya programa declarado)
    private AnalysisOutput checkFooter(String line, ProgramStatus status) {
        AnalysisOutput error = new AnalysisOutput();
        FooterInfo footerInfo = status.analyzer.getFooterInfo(line);

        //Guardias...
        if(!status.hasStart)
        {
            error.setStatus(AnalysisOutput.Status.PROGRAM_NOT_STARTED);
            error.setCause("Se ha llamado a la terminación de un programa no iniciado");
            error.setErrorLine(fileLine);
            return error;
        }

        if(status.hasFooter)
        {
            error.setStatus(AnalysisOutput.Status.DUPLICATED_FOOTER);
            error.setCause("Hay más de una sentencia terminar! [ERROR LEXICO???]");
            error.setErrorLine(fileLine);
            return error;
        }

        if(footerInfo.getStatus() != AnalysisOutput.Status.NO_ERROR)
        {
            return footerInfo.makeOutput(fileLine);
        }

        status.hasFooter = true;
        status.footerLine = fileLine;
        return null;
    }

    //REQUISITOS
    //Para que la sentencia sea correcta
    //necesita un encabezado,que el programa no ha terminado, no haya duplicados
    private AnalysisOutput checkStart(String line, ProgramStatus status) {
        AnalysisOutput error = new AnalysisOutput();
        StartInfo startInfo = status.analyzer.getStartInfo(line);

        //Guardias...
        if(!status.hasHeader) { //Si el programa no está declarado
            error.setStatus(AnalysisOutput.Status.BAD_START);
            error.setCause("El programa no se ha declarado");
            return error;
        }

        if(status.hasFooter) { //Si el programa ha terminado
            error.setStatus(AnalysisOutput.Status.BAD_START);
            error.setCause("Sentencia inicio en programa terminado");
            error.setErrorLine(fileLine);
            return error;
        }

        if (status.hasStart) {
            error.setStatus(AnalysisOutput.Status.DUPLICATED_START);
            error.setCause("Hay más de una sentencia inicio! [ERROR LEXICO???]");
            error.setErrorLine(fileLine);
            return error;
        }

        if(startInfo.getStatus() != AnalysisOutput.Status.NO_ERROR)
        {
            return startInfo.makeOutput(fileLine);
        }

        status.hasStart = true;
        status.startLine = fileLine;
        return null;
    }

    //RQUISITOS
    //Si no hay encabezado ninguna otra instruccion se ejecuta y tira error
    //Es el punto de partida
    //No encabezados duplicados
    private AnalysisOutput checkHeader(String line, ProgramStatus status) {
        AnalysisOutput error = new AnalysisOutput();
        HeaderInfo headerInfo = status.analyzer.getHeaderInfo(line);

        //Guardias...
        if(status.hasHeader)
        {
            error.setStatus(AnalysisOutput.Status.DUPLICATED_HEADER);
            error.setCause("Hay más de una sentencia programa!");
            error.setErrorLine(fileLine);
            return error;
        }

        if(headerInfo.getStatus() != AnalysisOutput.Status.NO_ERROR)
        {
            return headerInfo.makeOutput(fileLine);
        }

        status.hasHeader = true;
        status.headerLine = fileLine;
        status.programName = headerInfo.getIdentifier();
        return null;
    }




    //REQUISITOS
    //La sentencia print equiere que se haya declarado un programa, se haya iniciado y que el programa
    //no se haya terminado
    private AnalysisOutput checkPrint(String line, ProgramStatus status)
    {
        AnalysisOutput error = new AnalysisOutput();
        if (status.hasStart && status.hasHeader && !status.hasFooter) {
            PrintInfo printInfo = status.analyzer.getLinePrintInfo(line);

            if (printInfo.getStatus() != AnalysisOutput.Status.NO_ERROR) //Si truena por un error lexico/gramático
                return printInfo.makeOutput(fileLine);

            if (!isDefined(printInfo.getIdentifier())) //Si truena por que no está definida
            {
                error.setStatus(AnalysisOutput.Status.NOT_DEFINED_VAR);
                error.setCause("La variable " + printInfo.getIdentifier() + " no está definida! [ERROR LEXICO???]");
                error.setErrorLine(fileLine);
                return error;
            }
        } else //En caso de que haya alguna situacion con los requisitos de la sentencia tiramos error
            return makeInvalidProgram(fileLine, status.hasHeader, status.hasStart, status.hasFooter, status.footerLine);
        return null;
    }

    //REQUISITOS
    //La sentencia read equiere que se haya declarado un programa, se haya iniciado y que el programa
    //no se haya terminado
    private AnalysisOutput checkRead(String line, ProgramStatus status)
    {
        AnalysisOutput error = new AnalysisOutput();

        if(!status.hasHeader)
        {
            error.setStatus(AnalysisOutput.Status.UKNOWN);
            error.setCause("El programa no se ha declarado");
            error.setErrorLine(fileLine);
            return error;
        }

        if(!status.hasStart)
        {
            error.setStatus(AnalysisOutput.Status.UKNOWN);
            error.setCause("El programa no se ha iniciado");
            error.setErrorLine(fileLine);
            return error;
        }

        if(status.hasFooter)
        {
            error.setStatus(AnalysisOutput.Status.UKNOWN);
            error.setCause("El programa ya ha terminado");
            error.setErrorLine(fileLine);
            return error;
        }

            ReadInfo readInfo = status.analyzer.getLineReadInfo(line); //Leemos la info

            if (readInfo.getStatus() != AnalysisOutput.Status.NO_ERROR) //Si truena por un error lexico/gramático
                return readInfo.makeOutput(fileLine);

            if (isDefined(readInfo.getIdentifier())) //Si truena por que ya está definida
            {
                error.setStatus(AnalysisOutput.Status.ALREADY_DEFINED_VAR);
                error.setCause("La variable " + readInfo.getIdentifier() + " ya está definida! [ERROR LEXICO???]");
                error.setErrorLine(fileLine);
                return error;
            }

            if(!registerIdentifier(readInfo.getIdentifier()))
            {
                error.setStatus(AnalysisOutput.Status.BAD_IDENTIFIER);
                error.setCause("El identificador es una palabra reservada");
                error.setErrorLine(fileLine);
                return error;
            }
            if(!status.hasOneInstruction)
                status.hasOneInstruction=true;

        return null;
    }

    private static class ProgramStatus
    {
        LexicalAnalyzer analyzer = new LexicalAnalyzer();

        String programName = null;
        boolean hasHeader = false;
        int headerLine = 0;

        boolean hasStart = false;
        int startLine = 0;

        boolean hasFooter = false;
        int footerLine = 0;

        boolean hasOneInstruction = false;
    }

}
