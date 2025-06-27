
//package Compiler;

//import GUI.MainFrame;
import javax.swing.JOptionPane;

public class Scanner {
    
//DECLARACIONES
    private int contar = 0;
    private int lineaNo, k, longitud;
    private final String[] tokens;
    private String tipoToken;
    private String token, tokenQuedado;
    private final String[] reservadas = {"if", "then", "else", "begin", "print", "end", "int", "float"};
    private final String[] operadores = {"==", ":=", "+"};
    private final String delimitador = ";";


//main 

/*public static void main(String[] args){
    System.out.println("hello");

    new Scanner("codigo texto de prueba");
}*/
    
//CONSTRUCTOR
    public Scanner(String codigo) {
        tokens = codigo.split("\\s+");
        lineaNo = 0; //Aun sin implementar...
        k=0;
        token = "";
        longitud = tokens.length;
    }
    
//MÉTODO que retorna tokens válidos al parser
    public String getToken(boolean b) {
        boolean tokenValido = false;
        token = tokens[k];
        if(b) {
            if(k < tokens.length-1) {
            k++;
            contar++;
            }
        }

        //VERIFICACIÓN LÉXICA
        //Palabras reservadas:
        for (String reservada : reservadas) {
            if (token.equalsIgnoreCase(reservada)) {
                tokenValido = true;
                setTipoToken("Palabra reservada", b);
                break;
            }
        }
            //Operadores:
        if(!tokenValido) {
            for(String operador : operadores) {
                if(token.equals(operador)) {
                    tokenValido = true;
                    setTipoToken("Operador", b);
                    break;
                }
            }
        }
            //Delimitador:
        if(!tokenValido) {
            if(token.equals(delimitador)) {
                tokenValido = true;
                setTipoToken("Delimitador", b);
            }
        }
        
            //Identificadores:
        if(!tokenValido) {
            if(validaIdentificador(token)) {
                tokenValido = true;
                setTipoToken("Identificador", b);
            }
        }
        
            //Error:
        if(!tokenValido) {
            error("el token \"" + token + "\" es inválido para el lenguaje.");
            return "TOKEN INVÁLIDO";
        }
        return token;
    }
    
    public boolean validaIdentificador(String t) {
        boolean tokenValido = false;
        char[] charArray;
        charArray = t.toCharArray();
        int i=0;
        
       //Validación del primer caracter:
        if((charArray[i]>='a' && charArray[i]<='z') || 
                (charArray[i] >= 'A' && charArray[i] <= 'Z') ||
                (charArray[i]=='_')){ //(charArray[i]=='_') || (charArray[i]=='-')){
            tokenValido = true;
        }
        //Validación del resto del token (si su longitud es mayor a 1):
        if(t.length() > 1 && tokenValido) {
            for(int j=1 ; j<charArray.length ; j++) {
                if((charArray[j]>='a' && charArray[j]<='z') || 
                (charArray[j] >= 'A' && charArray[j] <= 'Z') ||
                (charArray[j]=='_') || (charArray[j]=='-') || (charArray[j]>='0' && charArray[j]<='9')){
                    tokenValido = true;
                }
            }
        }
        else if(t.length() > 1 && tokenValido) {
            tokenValido = false;
        }
        return tokenValido;
    }
    
    public void setTipoToken(String tipo, boolean b) {
        if(b) {
            tipoToken = tipo;
        }
    }

    public String getTipoToken() {
        return tipoToken;
    }
    
    public String checkNextToken() {
        return tokens[k];
    }
    public int getLongitud(){
        return longitud;
    }
    public int getContar(){
        contar -= 1;
        return contar + 1;
    }
    public String getTokenQuedado(){
        tokenQuedado = tokens[contar];
        return tokenQuedado;
    }
    // public void restoTokens(){
    //     for(int i = contar; i < (longitud - 1); i++){
    //         System.out.println(tokens[i]);
    //     }
    // }
    
    public void error(String error) {
        switch(JOptionPane.showConfirmDialog(null,
                "Error léxico: " + error + ".\n"
                        + "¿Desea detener la ejecución?",
                "Ha ocurrido un error",
                JOptionPane.YES_NO_OPTION)) {
            case JOptionPane.NO_OPTION:
                double e = 1.1;
                break;
                    
            case JOptionPane.YES_OPTION:
                System.exit(0);
                break;
        }
    }
}
