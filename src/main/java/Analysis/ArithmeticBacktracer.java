package Analysis;

import Analysis.Results.AnalysisOutput;

import java.util.ArrayList;
import java.util.HashSet;

public class ArithmeticBacktracer {
    private String expr = null;
    private ArrayList<String> subChains = new ArrayList<>();

    private HashSet<String> identifiers = new HashSet<>();
    private boolean badIdentifier = false;
    private String badIdentifierStr = null;

    public ArithmeticBacktracer(String expr) {
        this.expr = expr;
        makeDs();
        splitChain(this.expr);
    }

    private void splitChain(String chain)
    {
        char[] chars = chain.toCharArray();
        boolean beginExpr = false;
        int beginParPos = 0;
        int endParPos = 0;
        int openPar = 0;
        int index = 0;
        String closedExpr = null;
        String newExpr = null;
        for(char c : chars)
        {
            if(c == '(') {
                if(!beginExpr) {
                    beginExpr = true;
                    beginParPos = index;
                }
                openPar++;
            }
            if(c==')')
            {
                endParPos = index;
                openPar--;
                if(openPar==0)
                {
                    endParPos = index;
                    StringBuilder sb = new StringBuilder();
                    closedExpr = chain.substring(beginParPos+1, endParPos);
                    newExpr = sb.append(chain.substring(0, beginParPos)).append('D').append(chain.substring(endParPos+1, chain.length())).toString();
                    splitChain(closedExpr);
                    splitChain(newExpr);
                    break;
                }
            }

            index++;
        }
        if(!beginExpr) //No hay parentesis
            subChains.add(chain);
    }

    //[a-z0-9]
    private boolean validForLang(char c) {
        return String.valueOf(c).matches(RegexStrings.LANG);
    }

    private void makeDs() {
        char[] chars = expr.toCharArray();

        StringBuilder identifierBuilder = new StringBuilder();

        StringBuilder newExpr = new StringBuilder();


        boolean makingIdentifier = false;

        for (char c : chars) {

            if (validForLang(c)) {
                makingIdentifier = true;
                identifierBuilder.append(c);
            } else {
                String identifier = identifierBuilder.toString();
                if (makingIdentifier) {
                    newExpr.append('D');
                    if (identifier.length() > 0) {
                        if (!identifier.matches(RegexStrings.NUMBER)) {
                            if (!identifier.matches(RegexStrings.IDENTIFIER_NAME)) {
                                badIdentifierStr = identifier;
                                badIdentifier = true;
                                return;
                            }
                            identifiers.add(identifierBuilder.toString());
                        }
                    }
                }
                makingIdentifier = false;
                identifierBuilder = new StringBuilder();
                newExpr.append(c);
            }
        }


        //Por si se queda algo en el aire
        String identifier = identifierBuilder.toString();
        if (makingIdentifier) {
            newExpr.append('D');
            if (identifier.length() > 0) {
                if (!identifier.matches(RegexStrings.NUMBER)) {
                    if (!identifier.matches(RegexStrings.IDENTIFIER_NAME)) {
                        badIdentifierStr = identifier;
                        badIdentifier = true;
                        return;
                    }
                    identifiers.add(identifierBuilder.toString());
                }
            }
        }
        expr = newExpr.toString();
    }

    //Check if any of the identifiers of the expression is not declares
    public String checkIdentifiers(HashSet<String> declaredIdentifiers)
    {
        for(String id : identifiers)
        {
            if(!declaredIdentifiers.contains(id))
                return id;
        }
        return null;
    }

    //La cadena es válida si todas las subcadenas son válidas
    public ArithmeticBacktrackerStatus checkExpr() {
        ArithmeticBacktrackerStatus status = new ArithmeticBacktrackerStatus();
        status.errorCause = "Expresión inválida";
        status.status = AnalysisOutput.Status.BAD_IDENTIFIER;
        if(badIdentifier)
        {
            status.errorCause = "Nombre de identificador \"" + badIdentifierStr +"\" inválido";
            status.badIdentifier = badIdentifierStr;
            status.status = AnalysisOutput.Status.BAD_IDENTIFIER;
            return status;
        }

        if(subChains.size()==0) return status;

        for(String chain : subChains) {
            GoUpResult result = null;
            do {
                chain = constant(chain);
                chain = parenthesis(chain);
                chain = sign(chain);
                chain = pow(chain);
                chain = MD(chain);
                chain = SR(chain);
                chain = constant(chain);
                result = goUP(chain);
                chain = result.str;

            } while (result.didGoUp);

            if (chain.equals("S"))
                continue;
            return status;
        }
        status.errorCause = "";
        status.status = AnalysisOutput.Status.NO_ERROR;
        return status;
    }

    //A la operación de mayor jerarquía de izquierda a derecha, se le intenta subir de rango los símbolos hasta
    //alcanzar un match
    //P.E. B+A->A+A->S+A->S Válido
    //Si en el movimiento no se alcanzó a subir ningún símbolo, es por que la cadena es inválida
    //Es decir se llego a una stuación donde hay un símbolo extraño de algun lado del operador
    private GoUpResult goUP(String str) {

        char[] chars = str.toCharArray();
        for(int i = 1; i<chars.length; i++)
        {
            if(chars[i] > 'C')
            {
                if(chars[i-1] == '+' || chars[i-1] == '-')
                {
                    if(i>=2) {
                        if (!matchesSymbols(chars[i - 2])) {
                            chars[i] = 'C';
                            return new GoUpResult(String.valueOf(chars), true);
                        }
                    }
                    else
                    {
                        chars[i] = 'C';
                        return new GoUpResult(String.valueOf(chars), true);
                    }
                }
            }
        }

        int pow = str.indexOf('^');
        if (pow > 0 && pow < str.length() - 1) { //Si el símbolo existe y no está en ningún borde
            if(matchesSymbols(str.charAt(pow - 1)) && matchesSymbols(str.charAt(pow + 1))) { //Y no hay símbolos extraños
                if (str.charAt(pow - 1) > 'B') {                                             //a la derecha e izqueirda
                    char[] newStr = str.toCharArray();
                    newStr[pow - 1] = --newStr[pow - 1];
                    str = String.valueOf(newStr);
                }

                if (str.charAt(pow + 1) > 'C') {
                    char[] newStr = str.toCharArray();
                    newStr[pow + 1] = --newStr[pow + 1];
                    str = String.valueOf(newStr);
                }
                return new GoUpResult(str, true);
            }
        }

        int mult = str.indexOf('*');
        int div = str.indexOf('/');

        //Exactamente el mismo de arriba pero aqui si la multiplicación está primero que la division
        //se realiza la multiplicacion, o si no hay división
        if (mult > 0 && mult < str.length() - 1 && (mult<div | div==-1)) {
            if(matchesSymbols(str.charAt(mult - 1)) && matchesSymbols(str.charAt(mult + 1))) {
                if (str.charAt(mult - 1) > 'A') {
                    char[] newStr = str.toCharArray();
                    newStr[mult - 1] = --newStr[mult - 1];
                    str = String.valueOf(newStr);
                }
                if (str.charAt(mult + 1) > 'B') {
                    char[] newStr = str.toCharArray();
                    newStr[mult + 1] = --newStr[mult + 1];
                    str = String.valueOf(newStr);
                }
                return new GoUpResult(str, true);
            }
        }

        if (div > 0 && div < str.length() - 1) {
            if(matchesSymbols(str.charAt(div - 1)) && matchesSymbols(str.charAt(div + 1))) {
                if (str.charAt(div - 1) > 'A') {
                    char[] newStr = str.toCharArray();
                    newStr[div - 1] = --newStr[div - 1];
                    str = String.valueOf(newStr);
                }
                if (str.charAt(div + 1) > 'B') {
                    char[] newStr = str.toCharArray();
                    newStr[div + 1] = --newStr[div + 1];
                    str = String.valueOf(newStr);
                }
                return new GoUpResult(str, true);
            }
        }

        //Lo mismo de las * y /
        int sum = str.indexOf('+');
        int res = str.indexOf('-');

        if (sum > 0 && sum < str.length() - 1 && (sum<res | res==-1)) {
            if(matchesSymbols(str.charAt(sum - 1)) && matchesSymbols(str.charAt(sum + 1))) {
                if (str.charAt(sum - 1) != 'S') {
                    char[] newStr = str.toCharArray();
                    if (str.charAt(sum - 1) != 'A')
                        newStr[sum - 1] = --newStr[sum - 1];
                    else
                        newStr[sum - 1] = 'S';
                    str = String.valueOf(newStr);
                }

                if (str.charAt(sum + 1) > 'A') {
                    char[] newStr = str.toCharArray();
                    newStr[sum + 1] = --newStr[sum + 1];
                    str = String.valueOf(newStr);
                }
                return new GoUpResult(str, true);
            }
        }

        if (res > 0 && res < str.length() - 1) {
            if(matchesSymbols(str.charAt(res - 1)) && matchesSymbols(str.charAt(res + 1)))
            {
                if (str.charAt(res - 1) != 'S') {
                    char[] newStr = str.toCharArray();
                    if (str.charAt(res - 1) != 'A')
                        newStr[res - 1] = --newStr[res - 1];
                    else
                        newStr[res - 1] = 'S';
                    str = String.valueOf(newStr);
                }

                if (str.charAt(res + 1) > 'A') {
                    char[] newStr = str.toCharArray();
                    newStr[res + 1] = --newStr[res + 1];
                    str = String.valueOf(newStr);
                }
                return new GoUpResult(str, true);
            }
        }
        return new GoUpResult(str, false);
    }

    //Checa que no haya simbolos extraños
    private boolean matchesSymbols(char c) {
        return switch (c) {
            case 'S', 'A', 'B', 'C', 'D' -> true;
            default -> false;
        };
    }

    private String constant(String str){
        if(str.matches("([\\-\\+]?)([A-D])|([\\-\\+]?)([S])")) return "S";
        return str;
    }

    private String parenthesis(String str) {
        return str.replaceAll(RegexStrings.PARENTHESIS, "D");
    }

    private String sign(String str) {
        char[] chars = str.toCharArray();
        StringBuilder newExpr = new StringBuilder();
        newExpr.append(chars[0]);
        for(int i = 1; i<chars.length; i++)
        {
            if(chars[i] == 'C')
            {
                if(chars[i-1] == '+' || chars[i-1] == '-')
                {
                    if(i>=2) {
                        if (!matchesSymbols(chars[i - 2])) {
                            newExpr.setCharAt(newExpr.length()-1, chars[i]);
                        }
                        else
                            newExpr.append(chars[i]);
                    }
                    else
                    {
                        newExpr.setCharAt(newExpr.length()-1, chars[i]);
                    }
                }
                else
                    newExpr.append(chars[i]);
            }
            else
            {
                newExpr.append(chars[i]);
            }
        }
        return newExpr.toString();
    }

    private String pow(String str) {
        return str.replaceAll(RegexStrings.POW, "B");
    }

    private String MD(String str) {
        return str.replaceAll(RegexStrings.MD, "A");
    }

    private String SR(String str) {
        return str.replaceAll(RegexStrings.SR, "S");
    }

    private static class GoUpResult {
        public String str;
        public boolean didGoUp = false;

        GoUpResult(String str, boolean didGoUp) {
            this.str = str;
            this.didGoUp = didGoUp;
        }
    }

    public static class ArithmeticBacktrackerStatus
    {
        public AnalysisOutput.Status status;
        public String errorCause = null;
        public String badIdentifier = null;

        public ArithmeticBacktrackerStatus(){};
        public ArithmeticBacktrackerStatus(AnalysisOutput.Status status, String errorCause, String badIdentifier)
        {
            this.status = status;
            this.errorCause = errorCause;
            this.badIdentifier = badIdentifier;
        }
    }
}
