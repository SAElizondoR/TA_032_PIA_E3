package Analysis;

import java.util.HashSet;

public class ArithmeticExpressionTester {


    private static boolean isOperator(char c)
    {
        return switch (c)
        {
            case '+', '-', '*', '/', '^', '(', ')'-> true;
            default -> false;
        };
    }

    //Side -1 ->left
    //Side 1 ->right
    private static boolean isIdentifierOrNumber(String expr, int pos, HashSet<String> identifiers, int side)
    {
        if(pos<=0) return false;
        if(pos==expr.length()-1) return false;

        StringBuilder identifierOrNumberBuilder = new StringBuilder();
        int currentPos = pos+side;
        char[] chars = expr.toCharArray();



        while (!isOperator(chars[currentPos]))
        {
            identifierOrNumberBuilder.append(chars[currentPos]);

            currentPos+=side;
            if(currentPos<0||currentPos>=expr.length())
                break;
        }

        if(side==-1)
            identifierOrNumberBuilder.reverse();
        String identifierOrNumber= identifierOrNumberBuilder.toString();

        if(identifierOrNumber.length() == 0) {
            if (side == -1 && chars[currentPos] == ')') //Si se analiza la izquierda de un operador solo es valido tener )
                return true;
            if (side == 1 && chars[currentPos] == '(') //Si se analiza la derecha de un operador solo es valido tener (
                return true;
            return false;
        }


        if(identifierOrNumber.length()==0)
                return false;

        if(identifierOrNumber.matches(RegexStrings.NUMBER))
            return true;
        if(identifiers.contains(identifierOrNumber))
            return true;
        return false;

    }

    //Reemplaza el parentesis actual por un $ si encuentra uno de cierre también lo reemplaza
    //para no dentectarlo de nuevo
    //Un parentesis de cierre es aquel que se encuentre más al final
    public static boolean lookForCloseParenthesis(char[] chars, int startParPos)
    {
        chars[startParPos] = '$';

        if(startParPos+1==')') return false; //Si no hay nada dentro

        for(int i = startParPos+2; i<chars.length; i++)
        {
            if(chars[i] == ')') {
                chars[i]= '%';
                return true;
            }
        }

        return false;
    }

    //Usar el + y el - de esta forma no es válido --5, ++2
   public static boolean checkExpression(String expr, HashSet<String> identifiers) {
        char[] chars = expr.toCharArray();

       //sumas y restas
       int pos = 0;
        for (char c : chars) {
            if (c == '+') {
                boolean isLeft = isIdentifierOrNumber(expr, pos, identifiers, -1);
                boolean isRight = isIdentifierOrNumber(expr, pos, identifiers, 1);
                if(!(isLeft&&isRight))
                    return false;
            }
            if (c == '-') {
                boolean isLeft = isIdentifierOrNumber(expr, pos, identifiers, -1);
                boolean isRight = isIdentifierOrNumber(expr, pos, identifiers, 1);
                if(!(isLeft&&isRight))
                    return false;
            }
            pos++;
        }

        //Esta hecho asi por pura jerarquia de operadores

       //mult y div
       pos = 0;
       for (char c : chars) {
           if (c == '*') {
               boolean isLeft = isIdentifierOrNumber(expr, pos, identifiers, -1);
               boolean isRight = isIdentifierOrNumber(expr, pos, identifiers, 1);
               if(!(isLeft&&isRight))
                   return false;
           }
           if (c == '/') {
               boolean isLeft = isIdentifierOrNumber(expr, pos, identifiers, -1);
               boolean isRight = isIdentifierOrNumber(expr, pos, identifiers, 1);
               if(!(isLeft&&isRight))
                   return false;
           }
           pos++;
       }

       //potencia
       pos = 0;
       for (char c : chars) {
           if (c == '^') {
               boolean isLeft = isIdentifierOrNumber(expr, pos, identifiers, -1);
               boolean isRight = isIdentifierOrNumber(expr, pos, identifiers, 1);
               if(!(isLeft&&isRight))
                   return false;
           }
           pos++;
       }

       //parentesis
       pos = 0;

       //45+232+123+2)+2(
       //(())
       //(asda)
       //(12+23)
       for (char c : chars) {
            if(c == '(')
            {
                boolean hasPair = lookForCloseParenthesis(chars, pos); //Checa si tiene pareja
                if(!hasPair) //Si no tene pareja no es válida
                    return false;
            }

            if(c == '$' || c== '(')
            {
                boolean isLeft = isIdentifierOrNumber(expr, pos, identifiers, -1); // Checa si la izquierda es un identificador/num
                if(isLeft) //Si la izquierda es un identificador no es valida
                    return false;
            }

           if(c == '%' || c == ')')
           {
               boolean isRight = isIdentifierOrNumber(expr, pos, identifiers, 1); //Checa si la derecha es un identicador/num
               if(isRight) //Si la izquierda es un identificador
                   return false;
           }

            pos++;
       }

        return true;
    }
}
