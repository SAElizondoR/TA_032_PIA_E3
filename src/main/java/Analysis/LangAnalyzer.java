package Analysis;

import Analysis.Results.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class LangAnalyzer {

    private int fileLine = 0;
    private HashSet<String> declaredIdentifiers = new HashSet<>();

    public AnalysisOutput checkProgram(String filename) {
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

    public void registerIdentifier(String identifier) {
        declaredIdentifiers.add(identifier);
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

    public AnalysisOutput runTest(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        LexicalAnalyzer analyzer = new LexicalAnalyzer();

        String programName = null;
        boolean hasHeader = false;
        int headerLine = 0;

        boolean hasStart = false;
        int startLine = 0;

        boolean hasFooter = false;
        int footerLine = 0;


        while (scanner.hasNextLine())
        {
            fileLine++;
            String line = scanner.nextLine();
            if (line.isBlank()) //Si la linea es blanca la saltamos
                continue;

            LexicalAnalyzer.SentenceType type = analyzer.getSentenceType(line); //Sacamos el tipo

            switch (type) {
                case Read:
                    //REQUISITOS
                    //La sentencia read equiere que se haya declarado un programa, se haya iniciado y que el programa
                    //no se haya terminado
                    if (hasStart && hasHeader && !hasFooter) {
                        ReadInfo readInfo = analyzer.getLineReadInfo(line); //Leemos la info
                        if (readInfo.getStatus() != AnalysisOutput.Status.NO_ERROR) //Si truena por un error lexico/gramático
                            return readInfo.makeOutput(fileLine);

                        if (isDefined(readInfo.getIdentifier())) //Si truena por que ya está definida
                        {
                            AnalysisOutput error = new AnalysisOutput();
                            error.setStatus(AnalysisOutput.Status.ALREADY_DEFINED_VAR);
                            error.setCause("La variable " + readInfo.getIdentifier() + " ya está definida! [ERROR LEXICO???]");
                            error.setErrorLine(fileLine);
                            scanner.close();
                            return error;
                        }
                        registerIdentifier(readInfo.getIdentifier()); //Si tod_o está bien registramos el identificador
                    } else //En caso de que haya alguna situacion con los requisitos de la sentencia tiramos error
                        return makeInvalidProgram(fileLine, hasHeader, hasStart, hasFooter, footerLine);

                    break;
                case Print:
                    //REQUISITOS
                    //La sentencia print equiere que se haya declarado un programa, se haya iniciado y que el programa
                    //no se haya terminado
                    if (hasStart && hasHeader && !hasFooter) {
                        PrintInfo printInfo = analyzer.getLinePrintInfo(line);

                        if (printInfo.getStatus() != AnalysisOutput.Status.NO_ERROR) //Si truena por un error lexico/gramático
                            return printInfo.makeOutput(fileLine);

                        if (!isDefined(printInfo.getIdentifier())) //Si truena por que no está definida
                        {
                            AnalysisOutput error = new AnalysisOutput();
                            error.setStatus(AnalysisOutput.Status.NOT_DEFINED_VAR);
                            error.setCause("La variable " + printInfo.getIdentifier() + " no está definida! [ERROR LEXICO???]");
                            error.setErrorLine(fileLine);
                            scanner.close();
                            return error;
                        }
                    } else //En caso de que haya alguna situacion con los requisitos de la sentencia tiramos error
                        return makeInvalidProgram(fileLine, hasHeader, hasStart, hasFooter, footerLine);
                    break;
                case Operation:
                    //REQUISITOS
                    //La sentencia print equiere que se haya declarado un programa, se haya iniciado y que el programa
                    //no se haya terminado
                    if (hasStart && hasHeader && !hasFooter) {
                        OperationInfo operationInfo = analyzer.getOperationInfo(line);

                        if (operationInfo.getStatus() != AnalysisOutput.Status.NO_ERROR) //Si truena por un error lexico/gramático
                            return operationInfo.makeOutput(fileLine);

                        ArithmeticExpressionInfo expressionStatus =
                                ArithmeticExpressionTester.checkExpression(operationInfo.getExpression(), declaredIdentifiers);

                        if(expressionStatus.getStatus() != AnalysisOutput.Status.NO_ERROR)
                            return expressionStatus.makeOutput(fileLine);

                        registerIdentifier(operationInfo.getIdentifier());


                        if (!isDefined(operationInfo.getIdentifier())) //Si truena por que no está definida
                        {
                            AnalysisOutput error = new AnalysisOutput();
                            error.setStatus(AnalysisOutput.Status.NOT_DEFINED_VAR);
                            error.setCause("La variable " + operationInfo.getIdentifier() + " no está definida! [ERROR LEXICO???]");
                            error.setErrorLine(fileLine);
                            scanner.close();
                            return error;
                        }
                    } else //Tiramos error
                        return makeInvalidProgram(fileLine, hasHeader, hasStart, hasFooter, footerLine);
                    break;
                case Header:
                    HeaderInfo headerInfo = analyzer.getHeaderInfo(line);
                    //RQUISITOS
                    //Si no hay encabezado ninguna otra instruccion se ejecuta y tira error
                    //Es el punto de partida
                    //No encabezados duplicados
                    if (!hasHeader) //Manejamos encabezaods duplicados
                    {
                        if (headerInfo.getStatus() == AnalysisOutput.Status.NO_ERROR) { //Si el encabezado esta correcto
                            hasHeader = true;
                            headerLine = fileLine;
                            programName = headerInfo.getIdentifier();
                        } else
                            return headerInfo.makeOutput(fileLine);
                    } else //Tiramos error si hay encabezado duplicado
                    {
                        AnalysisOutput error = new AnalysisOutput();
                        error.setStatus(AnalysisOutput.Status.DUPLICATED_HEADER);
                        error.setCause("Hay más de una sentencia programa! [ERROR LEXICO???]");
                        error.setErrorLine(fileLine);
                        scanner.close();
                        return error;
                    }
                    break;

                case Start:
                    //REQUISITOS
                    //Para que la sentencia sea correcta
                    //mecesita un encabezado,que el programa no ha terminado, no haya duplicados
                    StartInfo startInfo = analyzer.getStartInfo(line);

                    if (hasHeader) { //Si el programa está declarado
                        if (!hasFooter) { //Si el programa no ha terminado
                            if (!hasStart) { //Manejamos duplicados
                                if (startInfo.getStatus() == AnalysisOutput.Status.NO_ERROR) {
                                    hasStart = true;
                                    startLine = fileLine;
                                } else
                                    return startInfo.makeOutput(fileLine);
                            } else { //Tiramos error por duplicados
                                AnalysisOutput error = new AnalysisOutput();
                                error.setStatus(AnalysisOutput.Status.DUPLICATED_START);
                                error.setCause("Hay más de una sentencia inicio! [ERROR LEXICO???]");
                                error.setErrorLine(fileLine);
                                scanner.close();
                                return error;
                            }
                        } else //Tiramos error por programa terminado
                        {
                            AnalysisOutput error = new AnalysisOutput();
                            error.setStatus(AnalysisOutput.Status.BAD_START);
                            error.setCause("Sentencia inicio en programa terminado");
                            error.setErrorLine(fileLine);
                            scanner.close();
                            return error;
                        }
                    } else //Tiramos errro por programa no declarado
                    {
                        AnalysisOutput error = new AnalysisOutput();
                        error.setStatus(AnalysisOutput.Status.BAD_START);
                        error.setCause("El programa no se ha declarado");
                        error.setErrorLine(fileLine);
                        scanner.close();
                        return error;
                    }
                    break;

                case Footer:

                    //REQUISITOS
                    //Que haya un start y no haya footers duplicados (El start se asegura que haya programa declarado)
                    FooterInfo footerInfo = analyzer.getFooterInfo(line);
                    if (hasStart) { //Verificamos que haya iniciado
                        if (!hasFooter) //Si es la primera sentencia de terminar
                        {
                            if (footerInfo.getStatus() == AnalysisOutput.Status.NO_ERROR) {
                                hasFooter = true;
                                footerLine = fileLine;
                            } else
                                return footerInfo.makeOutput(fileLine);
                        } else //Terminar duplicados
                        {
                            AnalysisOutput error = new AnalysisOutput();
                            error.setStatus(AnalysisOutput.Status.DUPLICATED_FOOTER);
                            error.setCause("Hay más de una sentencia terminar! [ERROR LEXICO???]");
                            error.setErrorLine(fileLine);
                            scanner.close();
                            return error;
                        }

                    } else //Manejar terminacion de programa no iniciado
                    {
                        AnalysisOutput error = new AnalysisOutput();
                        error.setStatus(AnalysisOutput.Status.PROGRAM_NOT_STARTED);
                        error.setCause("Se ha llamado a la terminación de un programa no iniciado");
                        error.setErrorLine(fileLine);
                        scanner.close();
                        return error;
                    }
                    break;
            }
        }

        //Si tiene footer quiere decir que tiene inicio y declaración de programa y si no ha tronado en algun otro lado
        //significa que tod_o está correcto
        if (hasFooter) {
            AnalysisOutput noError = new AnalysisOutput();
            noError.setStatus(AnalysisOutput.Status.NO_ERROR);
            return noError;
        }
        else { //En caso de que tod_o el programa este correcto pero falte el footer
            if (!hasFooter) { //Si el programa no tiene final
                AnalysisOutput error = new AnalysisOutput();
                error.setStatus(AnalysisOutput.Status.NO_FOOTER);
                error.setErrorLine(fileLine);
                error.setCause("El programa no tiene la sentencia terminar");
                return error;
            }
        }

        //Error desconocido, si llegamos aquí es hora de llorar
        return makeInvalidProgram(fileLine, hasHeader, hasStart, hasFooter, footerLine);
    }

}
