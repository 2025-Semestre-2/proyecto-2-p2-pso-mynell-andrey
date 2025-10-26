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
 * meter funciones auxiliares para no sobrecargar controlador
 */
public class Utilidades {
  

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
    public static LinkedHashMap<String, ArrayList<Integer>> ordenarProcesos(HashMap<String, ArrayList<Integer>> mapa) {
        List<Map.Entry<String, ArrayList<Integer>>> lista = new ArrayList<>(mapa.entrySet());
        lista.sort((e1, e2) -> {
            //primero ordene por arrivo
            int base1 = e1.getValue().get(0);
            int base2 = e2.getValue().get(0);
            int cmp = Integer.compare(base1, base2);
            //sino por ragaga
            if (cmp == 0) {
        
                int alcance1 = e1.getValue().get(1);
                int alcance2 = e2.getValue().get(1);
                return Integer.compare(alcance1, alcance2);
            }
            return cmp;
        });
        LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>();
        for (Map.Entry<String, ArrayList<Integer>> entry : lista) {
            mapaOrdenado.put(entry.getKey(), entry.getValue());
        }

        return mapaOrdenado;
    }
    

}
