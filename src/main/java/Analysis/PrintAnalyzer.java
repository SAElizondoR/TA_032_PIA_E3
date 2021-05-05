package Analysis;

import java.util.HashSet;

public class PrintAnalyzer {

    private String sentence = null;
    private HashSet<String> identifiers = new HashSet<>();

    public PrintAnalyzer(String sentence)
    {
        this.sentence = sentence;
    }

    public HashSet<String> getIdentifiers()
    {
        return identifiers;
    }

    public boolean isValid()
    {
        char[] chars = sentence.toCharArray();
        boolean onRawPrint = false;
        StringBuilder identifiersStr = new StringBuilder();
        for(int i = 0; i<chars.length; i++)
        {
            if(chars[i]=='"') {
                if(i>0)
                    if(chars[i-1]=='\\')
                        continue;

                if(onRawPrint) { //Se cierra comillas checamos que el siguiente sea un +
                    if (i < chars.length - 1)
                        if (chars[i + 1] != '+')
                            return false;
                }
                else
                {
                    if (i > 0) //Se abre comillas checamos que el anterior sea un +
                        if (chars[i - 1] != '+')
                            return false;
                }


                onRawPrint = !onRawPrint;
                continue;
            }

            if(!onRawPrint)
            {
                if(chars[i]=='+')
                {
                    if(identifiersStr.length()>0) {
                        String identifierName = identifiersStr.toString();
                        if(identifierName.matches(RegexStrings.IDENTIFIER_NAME));
                        identifiers.add(identifiersStr.toString());
                        identifiersStr = new StringBuilder();
                    }
                    continue;
                }
                identifiersStr.append(chars[i]);

            }
        }
        if(identifiersStr.length()>0) {
            String identifierName = identifiersStr.toString();
            if(identifierName.matches(RegexStrings.IDENTIFIER_NAME));
            identifiers.add(identifiersStr.toString());
        }
        if(onRawPrint)
            return false;
        return true;
    }
}
