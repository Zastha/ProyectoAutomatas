import javax.swing.JOptionPane;
import ArbolSintactico.*;
import java.util.Vector;

public class Parser {
    // Declaración de variables----------------
    Programax p = null;
    String[] tipo = null;
    String[] variable;
    String byteString;
    private Vector tablaSimbolos = new Vector();
    private final Scanner s;
    final int ifx = 1, thenx = 2, elsex = 3, beginx = 4, endx = 5, printx = 6, semi = 7,
            sum = 8, igual = 9, igualdad = 10, intx = 11, floatx = 12, id = 13, longx = 14, doublex = 15, res = 16,
            multi = 17, div = 18, whilex = 19, dox = 20, repeatx = 21, untilx = 22;
    private int tknCode, tokenEsperado;
    private static int contador = 0;
    private String token,  log;
    
    // Sección de bytecode
    private int cntBC = 0; // Contador de lineas para el código bytecode
    private String pilaBC[] = new String[100];
    private int cntIns = 0;

    // ---------------------------------------------

    /*
     * public static void main(String[] args){
     * //var1 int ; var2 int; if var1 == var2 then print var1 + var2 else begin if
     * var1 + var2 then var1 := var2 + var1 else var2 := var1 + var2 end
     * new
     * Parser("var1 int ; var2 int ; var1 := var2 + var1 ; print var1 + var2 ;");
     * }
     */

    public Parser(String codigo) {
        s = new Scanner(codigo);
        token = s.getToken(true);
        tknCode = stringToCode(token);
        p = P();
        ipbc(cntBC +" : end");
        System.out.println(getBytecode());
    }

    // INICIO DE ANÁLISIS SINTÁCTICO
    public void advance() {
        contador++;
        token = s.getToken(true);
        tknCode = stringToCode(token);
    }

    public void eat(int t) {
        tokenEsperado = t;
        if (tknCode == tokenEsperado) {
            setLog("Token: " + token + "\n" + "Tipo:  " + s.getTipoToken());
            advance();
        } else {
            error(token, "token tipo:" + t);
        }
    }

    public Programax P() {
        Declarax d = D();
        createTable();
        Statx s = S();
        System.out.println();

        if (this.s.getLongitud() != contador) {
            System.out.println(
                    "Hay más tokens después de terminar los estatutos. Los siguientes tokens no fueron tomados en cuenta para el análisis");
            errorSobrante(this.s.getTokenQuedado(), "Hay tokens más allá de los estatutos evaluados");
        }

        return new Programax(tablaSimbolos, s);
    }

    public Declarax D() {
        if (tknCode == id && (s.checkNextToken().equals("int") || s.checkNextToken().equals("float") ||
                s.checkNextToken().equals("long") || s.checkNextToken().equals("double"))) {
            String s = token;
            eat(id);
            Typex t = T();
            eat(semi);
            Declarax decl = new Declarax(s, t);
            tablaSimbolos.addElement(decl);
       
            D();
            return decl;
        } else {
            return null;
        }
    }

    public Typex T() {
        if (tknCode == intx) {
            eat(intx);
            return new Typex("int");
        } else if (tknCode == floatx) {
            eat(floatx);
            return new Typex("float");
        } else if (tknCode == longx) {
            eat(longx);
            return new Typex("long");
        } else if (tknCode == doublex) {
            eat(doublex);
            return new Typex("double");
        } else {
            error(token, "(int / float / long / double)");
            return null;
        }
    }

    public Statx S() { // return statement
        
        switch (tknCode) {
            
            case ifx:
                eat(ifx);
                Expx e1 = E();
                ipbc(cntIns + ": iflez goto ");
                int saltoElse = cntBC-1;
                eat(thenx);
                Statx s1 = S();
                ipbc(cntIns + ": goto ");
                int saltoIf = cntBC-1;
                pilaBC[saltoElse] += String.valueOf(cntBC);
                eat(elsex);
                Statx s2 = S();
                pilaBC[saltoIf] += String.valueOf(cntBC);
                
                return new Ifx(e1, s1, s2);

            case beginx:
                eat(beginx);
                s1 = S();
                L();
                return null;

            case id:
                String varName = token;
                eat(id);
                declarationCheck(varName);
                eat(igual); // :=
                Expx e = E();
                byteCode("igual", varName);
                return new Asignax(new Idx(varName), e);

            case printx:
                eat(printx);
                ipbc(cntIns + ": getstatic      #7");
                e = E();
                ipbc(cntIns + ": invokevirtual   #13");
                return new Printx(e);

            case whilex:
                eat(whilex);
                int inicioWhile = cntBC;
                Expx eWhile = E();
                ipbc(cntIns + ": iflez goto ");
                int saltoCondicional= cntBC - 1;
                eat(dox);
                Statx sWhile = S();
                ipbc(cntIns + ": goto " + inicioWhile);
                pilaBC[saltoCondicional] += String.valueOf(cntBC);
                return new Whilex(eWhile, sWhile);

            case repeatx:
                eat(repeatx);
                int inicioRepeat = cntBC;
                Statx sRepeat = S();
                eat(untilx);
                Expx eRepeat = E();
                ipbc(cntIns+": ifg goto "+inicioRepeat);
                return new Repeatx(sRepeat, eRepeat);

            default:
                error(token, "(if | begin | id | print | while | repeat)");
                return null;
        }
    }

    public void L() {
        if (tknCode == endx) {
            eat(endx);
        } else if (tknCode == semi) {
            eat(semi);
            S();
            L();
        } else {
            error(token, "(end | ;)");
        }
    }

    public Expx E() {
        if (tknCode == id) {
            String left = token;

            declarationCheck(left);
            eat(id);

            if (tknCode == 8 || tknCode == 16 || tknCode == 17 || tknCode == 18) {

                int operator = tknCode;
                eat(tknCode);

                if (tknCode == id) {
                    String right = token;

                    declarationCheck(right);
                    eat(id);
                    compatibilityCheck(left, right);

                    switch (operator) {
                        case 8:
                            byteCode("suma", left, right);
                            return new Sumax(new Idx(left), new Idx(right));
                        case 16:
                            byteCode("resta", left, right);
                            return new Restax(new Idx(left), new Idx(right));
                        case 17:
                            byteCode("multiplicacion", left, right);
                            return new Multix(new Idx(left), new Idx(right));
                        case 18:
                            byteCode("division", left, right);
                            return new Divix(new Idx(left), new Idx(right));
                        default:
                            throw new RuntimeException("Unknown operator");

                    }
                } else {

                    error(token, "(id)");
                    return null;

                }
            } else if (tknCode == igualdad) {

                eat(igualdad);
                if (tknCode == id) {

                    String right = token;

                    declarationCheck(right);
                    eat(id);
                    compatibilityCheck(left, right);
                    byteCode("igualdad", left, right);

                    return new Comparax(new Idx(left), new Idx(right));
                } else {

                    error(token, "(id)");
                    return null;
                }
            } else {
                error(token, "(== | + | - | * | /)");
                return null;
            }
        } else {
            error(token, "(id)");
            return null;
        }
    } // FIN DEL ANÁLISIS SINTÁCTICO

    public void error(String token, String t) {
        switch (JOptionPane.showConfirmDialog(null,
                "Error sintáctico:\n"
                        + "El token:(" + token + ") no concuerda con la gramática del lenguaje,\n"
                        + "se espera: " + codeToString(tokenEsperado) + ".\n"
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

    public void errorSobrante(String token, String t) {
        JOptionPane.showMessageDialog(
                null,
                "Error sintáctico:\n"
                        + "No se evaluarán los tokens a partir de: (" + token + ").\n"
                        + t + ".",
                "Ha ocurrido un error",
                JOptionPane.ERROR_MESSAGE);
    }

    public int stringToCode(String t) {
        int codigo = 0;
        switch (t) {
            case "if":
                codigo = 1;
                break;
            case "then":
                codigo = 2;
                break;
            case "else":
                codigo = 3;
                break;
            case "begin":
                codigo = 4;
                break;
            case "end":
                codigo = 5;
                break;
            case "print":
                codigo = 6;
                break;
            case "while":
                codigo = 19;
                break;
            case "do":
                codigo = 20;
                break;
            case "repeat":
                codigo = 21;
                break;
            case "until":
                codigo = 22;
                break;
            case ";":
                codigo = 7;
                break;
            case "+":
                codigo = 8;
                break;
            case ":=":
                codigo = 9;
                break;
            case "==":
                codigo = 10;
                break;
            case "int":
                codigo = 11;
                break;
            case "float":
                codigo = 12;
                break;
            case "long":
                codigo = 14;
                break;
            case "double":
                codigo = 15;
                break;
            case "-":
                codigo = 16;
                break;
            case "*":
                codigo = 17;
                break;
            case "/":
                codigo = 18;
                break;
            default:
                codigo = 13;
                break;
        }
        return codigo;
    }

    public String codeToString(int codigo) {

        String cadena = "";
        switch (codigo) {
            case 1:
                cadena = "if";
                break;
            case 2:
                cadena = "then";
                break;
            case 3:
                cadena = "else";
                break;
            case 4:
                cadena = "begin";
                break;
            case 5:
                cadena = "end";
                break;
            case 6:
                cadena = "print";
                break;
            case 19:
                cadena = "while";
                break;
            case 20:
                cadena = "do";
                break;
            case 21:
                cadena = "repeat";
                break;
            case 22:
                cadena = "until";
                break;
            case 7:
                cadena = ";";
                break;
            case 8:
                cadena = "+";
                break;
            case 9:
                cadena = ":=";
                break;
            case 10:
                cadena = "==";
                break;
            case 11:
                cadena = "int";
                break;
            case 12:
                cadena = "float";
                break;
            case 14:
                cadena = "long";
                break;
            case 15:
                cadena = "double";
                break;
            case 16:
                cadena = "-";
                break;
            case 17:
                cadena = "*";
                break;
            case 18:
                cadena = "/";
                break;
            default:
                cadena = "id";
                break;
        }
        return cadena;
    }

    // Métodos para recoger la información de los tokens para luego mostrarla
    public void setLog(String l) {
        if (log == null) {
            log = l + "\n \n";
        } else {
            log = log + l + "\n \n";
        }
    }

    public String getLog() {
        return log;
    }
    // -----------------------------------------------

    // Recorrido de la parte izquierda del árbol y creación de la tabla de símbolos
    public void createTable() {
        // String[] aux1 = new String[tablaSimbolos.size()];
        // String[] aux2 = new String[tablaSimbolos.size()];
        variable = new String[tablaSimbolos.size()];
        tipo = new String[tablaSimbolos.size()];

        // Imprime tabla de símbolos
        System.out.println("-----------------");
        System.out.println("TABLA DE SÍMBOLOS");
        System.out.println("-----------------");
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            Declarax dx;
            // Typex tx;
            dx = (Declarax) tablaSimbolos.get(i);
            variable[i] = dx.s1;
            tipo[i] = dx.s2.getTypex();
            System.out.println(variable[i] + ": " + tipo[i]); // Imprime tabla de símbolos por consola.
        }


        System.out.println("-----------------\n");
    }

    // Verifica las declaraciones de las variables consultando la tabla de símbolos
    public void declarationCheck(String s) {
        boolean valido = false;
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            if (s.equals(variable[i])) {
                valido = true;
                break;
            }
        }
        if (!valido) {
            System.out.println("La variable " + s + " no está declarada.\nSe detuvo la ejecución.");
            javax.swing.JOptionPane.showMessageDialog(null, "La variable [" + s + "] no está declarada", "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Chequeo de tipos consultando la tabla de símbolos
    // Error por reverse
    public void compatibilityCheck(String s1, String s2) {
        String elementoCompara1;
        String elementoCompara2;
        String tipoCompara1;
        String tipoCompara2;
        System.out.println("CHECANDO COMPATIBILIDAD ENTRE TIPOS (" + s1 + ", " + s2 + "). ");
        boolean termino = false;
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            // elementoCompara1 = (Declarax) tablaSimbolos.elementAt(i);
            elementoCompara1 = variable[i];
            tipoCompara1 = tipo[i];
            // if (s1.equals(elementoCompara1.s1)) {
            if (s1.equals(elementoCompara1)) {
                System.out.println("Se encontró el primer elemento en la tabla de símbolos...");
                for (int j = 0; j < tablaSimbolos.size(); j++) {
                    // elementoCompara2 = (Declarax) tablaSimbolos.elementAt(j);
                    elementoCompara2 = variable[j];
                    tipoCompara2 = tipo[j];
                    if (s2.equals(elementoCompara2)) {
                        System.out.println("Se encontró el segundo elemento en la tabla de símbolos...");
                        if (tipoCompara1.equals(tipoCompara2)) {
                            termino = true;
                            break;
                        } else if ((tipo[i].equals("int") && tipo[j].equals("long"))
                                || (tipo[i].equals("long") && tipo[j].equals("int")) ||
                                (tipo[i].equals("double") && tipo[j].equals("float"))
                                || (tipo[i].equals("float") && tipo[j].equals("double"))) {
                            termino = true;
                            break;
                        } else {
                            termino = true;
                            javax.swing.JOptionPane.showMessageDialog(null,
                                    "Incompatibilidad de tipos: " + elementoCompara1 + " ("
                                            + tipoCompara1 + "), " + elementoCompara2 + " ("
                                            + tipoCompara2
                                            + ").",
                                    "Error",
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    }
                }
            }
            if (termino) {
                break;
            }
        }
    }

    public void byteCode(String simbolo, String s1, String s2) {
        int pos1 = -1, pos2 = -1;
        

        for (int i = 0; i < variable.length; i++) {
            if (s1.equals(variable[i])) {
                pos1 = i;

            }
            if (s2.equals(variable[i])) {
                pos2 = i;
            }
        }
        
        String tipo1 = tipo[pos1];
        String tipo2 = tipo[pos2];
     

        String prefijo1 = preTipo(tipo1);
        String prefijo2 = preTipo(tipo2);
        String prefijoSigma = preTipo(tipoSigma(tipo1, tipo2));
        switch (simbolo) {
            case "igualdad":
                ipbc(cntIns + ": " + prefijo1 + "load_" + pos1);
                ipbc(cntIns + ": " + prefijo2 + "load_" + pos2);
                ipbc(cntIns + ": if_"+prefijoSigma+"cmpeq");
                break;
            case "suma":
                ipbc(cntIns + ": " + prefijo1 + "load_" + pos1);
                ipbc(cntIns + ": " + prefijo2 + "load_" + pos2);
                ipbc(cntIns + ": " + prefijoSigma + "add");
                break;
            case "resta":
                ipbc(cntIns + ": " + prefijo1 + "load_" + pos1);
                ipbc(cntIns + ": " + prefijo2 + "load_" + pos2);
                ipbc(cntIns + ": " + prefijoSigma + "sub");
                break;
            case "multiplicacion":
                ipbc(cntIns + ": " + prefijo1 + "load_" + pos1);
                ipbc(cntIns + ": " + prefijo2 + "load_" + pos2);
                ipbc(cntIns + ": " + prefijoSigma + "mul");
                break;
            case "division":
                ipbc(cntIns + ": " + prefijo1 + "load_" + pos1);
                ipbc(cntIns + ": " + prefijo2 + "load_" + pos2);
                ipbc(cntIns + ": " + prefijoSigma + "div");
                break;
        }
    }

    public void byteCode(String simbolo, String s1) {
        int pos1 = -1;
        for (int i = 0; i < variable.length; i++) {
            if (s1.equals(variable[i])) {
                pos1 = i;
            }
        }
        String prefijo = preTipo(s1);
        switch (simbolo) {
            case "igual":
                ipbc(cntIns + ": " + prefijo + "store_" + pos1);
            break;

        }
    }



    public void ipbc(String ins) {
        
        while (pilaBC[cntBC] != null) {
            cntBC++;
        }
        cntIns++;
        pilaBC[cntBC] = ins;
        cntBC++;
    }

    public String getBytecode() {
        String JBC = "";
        for (int i = 0; i < pilaBC.length; i++) {
            if (pilaBC[i] != null) {
                JBC = JBC + pilaBC[i] + "\n";
            }
        }
        return JBC;
    }

    private String tipoSigma(String tipo1, String tipo2) {
        if (tipo1.equals(tipo2)) {
            return tipo1;
        } else if (tipo1.equals("int") && tipo2.equals("long") ||
                tipo1.equals("long") && tipo2.equals("int")) {
            return "long";
        } else if (tipo1.equals("double") && tipo2.equals("float") ||
                tipo1.equals("float") && tipo2.equals("double")) {
            return "double";
        } else {
            return null;
        }
    }

    private String preTipo(String tipo) {
        String prefijo = "";
        if (tipo == "int") {
            prefijo = "i";
        } else if (tipo == "float") {
            prefijo = "f";
        } else if (tipo == "long") {
            prefijo = "l";
        } else if (tipo == "double") {
            prefijo = "d";
        }
        return prefijo;
    }
}
