import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SelectorArchivo {

    public SelectorArchivo(){
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setDialogTitle("Selecciona Archivo a compilar");
        jfc.setCurrentDirectory(new File("ArchivosPrueba"));
        FileNameExtensionFilter fnef = new FileNameExtensionFilter("Archivos a Compilar","txt");
        jfc.setFileFilter(fnef);

        if(jfc.showOpenDialog(null) != JFileChooser.CANCEL_OPTION){

            File archivo = jfc.getSelectedFile();
            try{
                System.out.println(fileToString(archivo));
                new Parser(fileToString(archivo));
                
            }catch(IOException e){
                System.out.println("Error al convertir archivo a String");
            }
            
        }

    }

    public String fileToString(File archivo) throws IOException{
        String codigo ="";

        BufferedReader reader = new BufferedReader(new FileReader (archivo));
        StringBuilder sb = new StringBuilder();
        String line;
        String ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(ls);
            }

            codigo = sb.toString();

        } finally{
            reader.close();
        }
        return codigo;
    }

    
}
