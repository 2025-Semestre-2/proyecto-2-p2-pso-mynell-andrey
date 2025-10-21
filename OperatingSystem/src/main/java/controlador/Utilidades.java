/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.io.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
/**
 *
 * @author Andrey
 */
public class Utilidades {
    //abre un archivo y lo retorna

    public static File[] seleccionarArchivos() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new FileNameExtensionFilter("Archivos ASM (*.asm)", "asm"));

        int seleccionado = fc.showOpenDialog(null);
        if (seleccionado == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFiles();
        }
        return null;
    }

    public static List<String> leerArchivo(File archivo) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.trim());
            }
        }
        return lineas;
    }

}
