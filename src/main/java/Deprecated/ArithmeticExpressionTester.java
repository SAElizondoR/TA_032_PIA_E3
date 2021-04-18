package Deprecated;

import Analysis.RegexStrings;
import Analysis.Results.AnalysisOutput;
import Analysis.Results.ArithmeticExpressionInfo;

import java.util.HashSet;

public class ArithmeticExpressionTester {

    private static class SideStatus
    {
        public boolean success = false;
        public String identifier = null;
        SideStatus(boolean success, String identifier)
        {
            this.success = success;
            this.identifier = identifier;
        }
    }

    //Se incluye como operador a () (aunque no son operadores reales) por simplicidad
    private static boolean isOperator(char c) {
        return switch (c) {
            case '+', '-', '*', '/', '^', '(', ')' -> true;
            default -> false;
        };
    }

    //Side -1 ->left
    //Side 1 ->right
    private static SideStatus isSideValid(String expr, int pos, HashSet<String> identifiers, int side) {
        if (pos <= 0 && side == -1)
            return new SideStatus(false, null); //Si la posición analizada es el límite izquierdo y se analiza por la izquierda no es válida
        if (pos == expr.length() - 1 && side == 1)
            return new SideStatus(false, null);; //Si la posición analizada es el limite derecho y se analiza por la derecha no es válida

        StringBuilder identifierOrNumberBuilder = new StringBuilder(); //Builder para el numero o identificador

        int currentPos = pos + side; //Depende del lado la posición que se va a empezar a manejar
        char[] chars = expr.toCharArray();

        while (!isOperator(chars[currentPos])) //Mientras la posición actual no es un operador
        {
            identifierOrNumberBuilder.append(chars[currentPos]); //Añade número o letra

            currentPos += side; //Nos desplazamos en la dirección adecuada

            if (currentPos < 0 || currentPos >= expr.length()) //Si alcanzamos uno de los límites, termina el ciclo
                break;
        }

        if (side == -1) //Si se analiza la izquierda se invierte la cadena
            identifierOrNumberBuilder.reverse();

        String identifierOrNumber = identifierOrNumberBuilder.toString();


        if (identifierOrNumber.length() == 0) { //Si la izquierda o derecha inmediata fue un operador
            if (side == -1 && chars[currentPos] == ')') //Si se analiza la izquierda de un operador solo es valido tener )
                return new SideStatus(true, null);
            if (side == 1 && chars[currentPos] == '(') //Si se analiza la derecha de un operador solo es valido tener (
                return new SideStatus(true, null);
            return new SideStatus(false, null); //El lado inmediato fue un operador no válido
        }

        if (identifierOrNumber.matches(RegexStrings.NUMBER)) //Si la cadena es un número
            return new SideStatus(true, null);
        if (identifiers.contains(identifierOrNumber)) //O si es identificador registrado
            return new SideStatus(true, null);

        return new SideStatus(false, identifierOrNumber); //Si llegamos aqui es que el identificador es incorrecto

    }

    //Reemplaza el parentesis actual por un $ si encuentra uno de cierre lo reemplaza por %
    //para no dentectarlo de nuevo
    //Un paréntesis de cierre es el ) más cercano
    private static boolean lookForCloseParenthesis(char[] chars, int startParPos) {
        chars[startParPos] = '$';

        if (startParPos + 1 == ')') return false; //Si no hay nada dentro

        for (int i = startParPos + 2; i < chars.length; i++) {
            if (chars[i] == ')') {
                chars[i] = '%';
                return true;
            }
        }

        return false;
    }

    //REGLAS:
    //Se analiza por jerarquia sumas y restas, multiplicaciones y divisiones, potencias y al final paréntesis
    //Una operaodor binario es válido si la izquierda y derecha es un número, un identificador válido o
    //si la izquierda inmediata es ) o si la derecha inmediata es )
    //En gerneral todos los análisis son identicos excepto los parentesis
    //Se respeta el orden de derivación al verificar primero sumas y restas y luego mult y div y .......
    public static ArithmeticExpressionInfo checkExpression(String expr, HashSet<String> identifiers) {
        char[] chars = expr.toCharArray();

        ArithmeticExpressionInfo infoBadExpression = new ArithmeticExpressionInfo();
        infoBadExpression.setStatus(AnalysisOutput.Status.BAD_EXPRESSION);
        infoBadExpression.setErrorCause("Expresion inválida");

        //sumas y restas
        int pos = 0;
        for (char c : chars) {
            if (c == '+') {
                SideStatus isLeft = isSideValid(expr, pos, identifiers, -1); // Checa si la izquierda es válida
                SideStatus isRight = isSideValid(expr, pos, identifiers, 1); // Checa si la derecha es válida
                if(!isLeft.success)
                {
                    if(isLeft.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isLeft.identifier + "\" no declarado");
                    return infoBadExpression;
                }
                if(!isRight.success)
                {
                    if(isRight.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isRight.identifier + "\" no declarado");
                    return infoBadExpression;
                }
            }
            if (c == '-') {
                SideStatus isLeft = isSideValid(expr, pos, identifiers, -1); // Checa si la izquierda es válida
                SideStatus isRight = isSideValid(expr, pos, identifiers, 1); // Checa si la derecha es válida
                if(!isLeft.success)
                {
                    if(isLeft.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isLeft.identifier + "\" no declarado");
                    return infoBadExpression;
                }
                if(!isRight.success)
                {
                    if(isRight.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isRight.identifier + "\" no declarado");
                    return infoBadExpression;
                }
            }
            pos++;
        }

        //mult y div
        pos = 0;
        for (char c : chars) {
            if (c == '*') {
                SideStatus isLeft = isSideValid(expr, pos, identifiers, -1); // Checa si la izquierda es válida
                SideStatus isRight = isSideValid(expr, pos, identifiers, 1); // Checa si la derecha es válida
                if(!isLeft.success )
                {
                    if(isLeft.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isLeft.identifier + "\" no declarado");
                    return infoBadExpression;
                }
                if(!isRight.success)
                {
                    if(isRight.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isRight.identifier + "\" no declarado");
                    return infoBadExpression;
                }
            }
            if (c == '/') {
                SideStatus isLeft = isSideValid(expr, pos, identifiers, -1); // Checa si la izquierda es válida
                SideStatus isRight = isSideValid(expr, pos, identifiers, 1); // Checa si la derecha es válida
                if(!isLeft.success)
                {
                    if(isLeft.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isLeft.identifier + "\" no declarado");
                    return infoBadExpression;
                }
                if(!isRight.success)
                {
                    if(isRight.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isRight.identifier + "\" no declarado");
                    return infoBadExpression;
                }
            }
            pos++;
        }

        //potencia
        pos = 0;
        for (char c : chars) {
            if (c == '^') {
                SideStatus isLeft = isSideValid(expr, pos, identifiers, -1); // Checa si la izquierda es válida
                SideStatus isRight = isSideValid(expr, pos, identifiers, 1); // Checa si la derecha es válida
                if(!isLeft.success)
                {
                    if(isLeft.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isLeft.identifier + "\" no declarado");
                    return infoBadExpression;
                }
                if(!isRight.success)
                {
                    if(isRight.identifier != null)
                        infoBadExpression.setErrorCause("Identificador \""+ isRight.identifier + "\" no declarado");
                    return infoBadExpression;
                }
            }
            pos++;
        }

        //parentesis
        //Primero checamos que todos tengan parejas
        pos = 0;
        for (char c : chars) {
            if (c == '(') {
                boolean hasPair = lookForCloseParenthesis(chars, pos); //Checa si tiene pareja
                //Si tiene pareja reemplaza ( por $ y ) por % para no repetir coincidencias
                //Ejemplo -> (()), los primeros dos ( tendrían como pareja al primer ) lo que es un error
                //por ello se cambia el símbolo
                if (!hasPair) //Si no tene pareja no es válida
                    return infoBadExpression;
            }
            pos++;
        }

        pos = 0; //Checamos que no haya situaciones 1231(expr) o (expr)123123
        for (char c : chars) {
            //Como el símbolo ha cambiado usamos el nuevo símbolo
            //Checamos si la izquierda de ( es válida, si lo es es que nos encontramos en una situacion
            //123123()
            if (c == '$') {
                SideStatus isLeft = isSideValid(expr, pos, identifiers, -1); // Checa si la izquierda es válida
                if (isLeft.success) //Si la izquierda es un identificador no es valida
                    return infoBadExpression;
            }

            //Como el símbolo ha cambiado usamos el nuevo símbolo
            //Checamos si la izquierda de ) es válida, si lo es es que nos encontramos en una situacion
            //()12313123
            if (c == '%') {
                SideStatus isRight = isSideValid(expr, pos, identifiers, 1); //Checa si la derecha es un identicador/num
                if (isRight.success) //Si la izquierda es un identificador
                    return infoBadExpression;
            }
            pos++;
        }

        //Si quedó un ( o ) suelto es que le faltó su pareja
        //Esto es para situaciones )*123*(i), donde ) no se analiza
        for (char c : chars)
            if (c == '(' || c == ')')
                return infoBadExpression;

        ArithmeticExpressionInfo success = new ArithmeticExpressionInfo();
        success.setStatus(AnalysisOutput.Status.NO_ERROR);
        return success;
    }

}
