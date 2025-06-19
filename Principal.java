import java.io.IOException;

public class Principal {
    public static void main(String[] args) throws IOException {
        System.out.println("Hola");
        String codigo = "var1 int ; var2 int ; var1 := var2 + var1 ; print var1 + var2 ;  ";
        new Parser(codigo);
    }

}