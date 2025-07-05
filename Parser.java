//package Compiler;

import javax.swing.JOptionPane;
import ArbolSintactico.*;
import java.util.Vector;
import org.apache.commons.lang3.ArrayUtils;

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
            multi = 17, div = 18;
    private int tknCode, tokenEsperado;
    private String token, tokenActual, log;

    // Sección de bytecode
    private int cntBC = 0; // Contador de lineas para el código bytecode
    private String bc; // String temporal de bytecode
    private int jmp1, jmp2, jmp3;
    private int aux1, aux2, aux3;
    private String pilaBC[] = new String[100];
    private String memoriaBC[] = new String[10];
    private String pilaIns[] = new String[50];
    private int retornos[] = new int[10];
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
    }

    // INICIO DE ANÁLISIS SINTÁCTICO
    public void advance() {
        token = s.getToken(true);
        tokenActual = s.getToken(false);
        tknCode = stringToCode(token);
    }

    public void eat(int t) {
        tokenEsperado = t;
        if (tknCode == t) {
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

        if (this.s.getLongitud() - 1 != this.s.getContar()) {
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
                eat(thenx);
                Statx s1 = S();
                eat(elsex);
                Statx s2 = S();
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
                return new Asignax(new Idx(varName), e);

            case printx:
                eat(printx);
                e = E();
                return new Printx(e);

            default:
                error(token, "(if | begin | id | print)");
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
            if (tknCode == igualdad) { // ==
                eat(igualdad);
                if (tknCode == id) {
                    String right = token;
                    declarationCheck(right);
                    eat(id);
                    compatibilityCheck(left, right);
                    return new Comparax(new Idx(left), new Idx(right));
                } else {
                    error(token, "(id)");
                    return null;
                }
            } else if (tknCode == sum) { // +
                eat(sum);
                if (tknCode == id) {
                    String right = token;
                    declarationCheck(right);
                    eat(id);
                    compatibilityCheck(left, right);
                    return new Sumax(new Idx(left), new Idx(right));
                } else {
                    error(token, "(id)");
                    return null;
                }
            } else if (tknCode == res) { // -
                eat(res);
                if (tknCode == id) {
                    String right = token;
                    declarationCheck(right);
                    eat(id);
                    compatibilityCheck(left, right);
                    return new Restax(new Idx(left), new Idx(right));
                } else {
                    error(token, "(id)");
                    return null;
                }

            } else if (tknCode == multi) { // *
                eat(multi);
                if (tknCode == id) {
                    String right = token;
                    declarationCheck(right);
                    eat(id);
                    compatibilityCheck(left, right);
                    return new Multix(new Idx(left), new Idx(right));
                } else {
                    error(token, "(id)");
                    return null;
                }
            } else if (tknCode == div) { // /
                eat(div);
                if (tknCode == id) {
                    String right = token;
                    declarationCheck(right);
                    eat(id);
                    compatibilityCheck(left, right);
                    return new Divix(new Idx(left), new Idx(right));
                } else {
                    error(token, "(id)");
                    return null;
                }

            } else {
                error(token, "(== | +)");
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
                        + "se espera: " + t + ".\n"
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
            Typex tx;
            dx = (Declarax) tablaSimbolos.get(i);
            variable[i] = dx.s1;
            tipo[i] = dx.s2.getTypex();
            System.out.println(variable[i] + ": " + tipo[i]); // Imprime tabla de símbolos por consola.
        }

        ArrayUtils.reverse(variable);
        ArrayUtils.reverse(tipo);

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
    public void compatibilityCheck(String s1, String s2) {
        Declarax elementoCompara1;
        Declarax elementoCompara2;
        System.out.println("CHECANDO COMPATIBILIDAD ENTRE TIPOS (" + s1 + ", " + s2 + "). ");
        boolean termino = false;
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            elementoCompara1 = (Declarax) tablaSimbolos.elementAt(i);
            if (s1.equals(elementoCompara1.s1)) {
                System.out.println("Se encontró el primer elemento en la tabla de símbolos...");
                for (int j = 0; j < tablaSimbolos.size(); j++) {
                    elementoCompara2 = (Declarax) tablaSimbolos.elementAt(j);
                    if (s2.equals(elementoCompara2.s1)) {
                        System.out.println("Se encontró el segundo elemento en la tabla de símbolos...");
                        if (tipo[i].equals(tipo[j])) {
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
                                    "Incompatibilidad de tipos: " + elementoCompara1.s1 + " ("
                                            + elementoCompara1.s2.getTypex() + "), " + elementoCompara2.s1 + " ("
                                            + elementoCompara2.s2.getTypex()
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

    public void byteCode(String tipo, String s1, String s2) {
        int pos1 = -1, pos2 = -1;

        for (int i = 0; i < variable.length; i++) {
            if (s1.equals(variable[i])) {
                pos1 = i;
            }
            if (s2.equals(variable[i])) {
                pos2 = i;
            }
        }

        String prefijo1 = preTipo(s1);
        String prefijo2 = preTipo(s2);
        String prefijoSigma = preTipo(tipoSigma(s1, s2));
        switch (tipo) {
            case "igualdad":
                ipbc(cntIns + ": " + prefijo1 + "load_" + pos1);
                ipbc(cntIns + ": " + prefijo2 + "load_" + pos2);
                ipbc(cntIns + ": ifne " + (cntIns + 4));
                jmp1 = cntBC;
                break;

            case "suma":
                ipbc(cntIns + ": " + prefijo1 + "load_" + pos1);
                ipbc(cntIns + ": " + prefijo2 + "load_" + pos2);
                ipbc(cntIns + ": " + prefijoSigma + "add");
                jmp2 = cntBC;
                break;
        }
    }

    public void byteCode(String tipo, String s1) {
        int pos1 = -1;
        for (int i = 0; i < variable.length; i++) {
            if (s1.equals(variable[i])) {
                pos1 = i;
            }
        }
        String prefijo = preTipo(s1);
        switch (tipo) {
            case "igual":
                pilaBC[cntBC + 3] = cntIns + 4 + ": " + prefijo + "store_" + pos1;
                cntIns++;
                jmp2 = cntBC;
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

    private String tipoVariable(String nombreVar) {
        for (int i = 0; i < variable.length; i++) {
            if (nombreVar.equals(variable[i])) {
                return tipo[i];
            }
        }
        return "int";
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
