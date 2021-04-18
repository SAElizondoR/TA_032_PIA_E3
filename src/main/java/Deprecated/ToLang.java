package Deprecated;

import Analysis.LangAnalyzer;
import Analysis.LexicalAnalyzer;
import Analysis.Results.AnalysisOutput;
import Analysis.Results.OperationInfo;
import Analysis.Results.PrintInfo;
import Analysis.Results.ReadInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class ToLang {
    public enum Lang {JAVA, PYTHON, CPP, GO,}

    public static String toLang(Lang lang, String file) throws FileNotFoundException {
        return switch (lang) {
            case JAVA -> makeJava(file);
            default -> null;
        };
    }

    private static String makeJava(String filename) throws FileNotFoundException {
        LangAnalyzer analyzer = new LangAnalyzer(filename);
        AnalysisOutput output = analyzer.checkProgram();
        if (output.getStatus() == AnalysisOutput.Status.NO_ERROR) {
            StringBuilder builder = new StringBuilder();
            builder.append("import java.util.Scanner;\n");
            builder.append("public class Main\n");
            builder.append("{\n");
            builder.append("static public void main(String[] args){\n");

            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            boolean hasScanner = false;
            HashSet<String> identifiers = new HashSet<>();

            while (scanner.hasNextLine()) {
                LexicalAnalyzer lexical = new LexicalAnalyzer();
                String line = scanner.nextLine();
                if (line.isBlank())
                    continue;
                LexicalAnalyzer.SentenceType type = lexical.getSentenceType(line);
                switch (type) {
                    case Read:
                        if (!hasScanner) {
                            builder.append("Scanner scan = new Scanner(System.in);\n");
                            hasScanner = true;
                        }
                        ReadInfo readInfo = lexical.getLineReadInfo(line);
                        if (!identifiers.contains(readInfo.getIdentifier())) {
                            builder.append("double ");
                            identifiers.add(readInfo.getIdentifier());
                        }
                        builder.append(readInfo.getIdentifier());
                        builder.append(" = scan.nextDouble();\n");
                        break;
                    case Print:
                        PrintInfo printInfo = lexical.getLinePrintInfo(line);
                        builder.append("System.out.println(");
                        builder.append(printInfo.getIdentifier());
                        builder.append(");\n");
                        break;
                    case Operation:
                        OperationInfo operationInfo = lexical.getOperationInfo(line);
                        if (!identifiers.contains(operationInfo.getIdentifier())) {
                            builder.append("double ");
                            identifiers.add(operationInfo.getIdentifier());
                        }
                        builder.append(operationInfo.getIdentifier());
                        builder.append(" = ");
                        String expression = operationInfo.getExpression();


                        String[] split = expression.split("\\^");
                        int pendingP = 0;
                        for (int i = 0; i < split.length -1; i++) {
                            String[] result = fixString(split[i], builder);
                            split[i] = result[0];
                            if(result[1]!=null)
                                builder.append(result[1]);


                            if(result[2] != null) {
                                builder.append(result[0]);
                                builder.append(')');
                                if(result[2].length()>2) {
                                    builder.append(result[2].substring(0, 2));
                                    builder.append("Math.pow(").append(result[2].substring(2)).append(",");
                                }
                                else
                                    builder.append(result[2]);
                            }
                            else
                            {
                                builder.append("Math.pow(").append(result[0]).append(",");
                            }
                            pendingP++;
                        }
                        String[] result = fixString(split[split.length-1], builder);
                        split[split.length-1] = result[0];
                        builder.append(split[split.length - 1]);
                        if(result[2]!=null)
                            builder.append(result[2]);
                        for (int i = 0; i < pendingP-1; i++)
                            builder.append(')');


                        builder.append(";\n");
                        break;
                }


            }
            builder.append("}\n");
            builder.append("}\n");
            return builder.toString();

        } else {
            System.out.println("ERROR EN LINEA : " + output.getErrorLine());
            System.out.println(output.getCause());
            return null;
        }
    }

    private static String[] fixString(String str, StringBuilder builder) {
        String[] strs = new String[3];
        if (str.contains("(") || str.contains(")")) {
            int lp = 0;
            int rp = 0;
            int lastlp = 0;
            int lastrp = 0;
            for (int p = str.length() - 1; p >= 0; p--) {
                if (str.charAt(p) == '(') {
                    lastlp = p;
                    lp++;
                    if(lp>rp)
                        break;
                }
                if(str.charAt(p) == ')')
                    rp++;
            }

            if(lp>rp) {
                String left = str.substring(0, lastlp+1);
                str = str.substring(lastlp + 1);
                strs[1] = left;
            }

            lp = 0;
            rp = 0;
            for (int p = 0; p < str.length(); p++) {
                if (str.charAt(p) == ')') {
                    lastrp = p;
                    rp++;
                    if (lp < rp) {
                        break;
                    }
                }
                if(str.charAt(p) == '(')
                    lp++;

            }
            if(lp<rp) {
                String right = str.substring(lastrp, str.length());
                str = str.substring(0, lastrp);
                strs[2] = right;
            }

        }
        strs[0] = str;
        int lpCount = str.length() - str.replace(")", "").length();
        int rpCount = str.length() - str.replace("(", "").length();
        //if(lpCount!=rpCount)
        //    fixString(str, builder);
        return strs;
    }


    private static String makePython(String file) {
        return null;
    }
}
