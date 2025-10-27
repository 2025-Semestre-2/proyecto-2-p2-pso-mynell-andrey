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
    public static ArrayList<Integer> obtenerProcesoU(HashMap<String, ArrayList<Integer>> mapa, String nombre) {
        return mapa.get(nombre);
    }

    public static ArrayList<Integer>  obtenerSiguienteProcesoU(HashMap<String, ArrayList<Integer>> mapa) {
        if (mapa.isEmpty()) return null;
        return mapa.values().iterator().next(); 
    }
    public static String obtenerNombreProcesoU(HashMap<String, ArrayList<Integer>> mapa) {
        if (mapa.isEmpty()) return null;
        return mapa.keySet().iterator().next(); 
    }
    public static Integer obtenerRafagaProcesoU(HashMap<String, ArrayList<Integer>> mapa) {
        if (mapa.isEmpty()) return null;
        Map.Entry<String, ArrayList<Integer>> entry = mapa.entrySet().iterator().next();
    
        return entry.getValue().get(1); 
    }
    public static void eliminarProcesoU(HashMap<String, ArrayList<Integer>> mapa, String nombre){
        mapa.remove(nombre);
    }
    public static void eliminarSiguienteProcesoU(HashMap<String, ArrayList<Integer>> mapa, String nombre) {
        if (!mapa.isEmpty()) {
            String primeraClave = mapa.keySet().iterator().next();
            mapa.remove(primeraClave);
        }
    }

    public static LinkedHashMap<String, ArrayList<Integer>> ordenarProcesosFCFS(HashMap<String, ArrayList<Integer>> mapa) {
        List<Map.Entry<String, ArrayList<Integer>>> lista = new ArrayList<>(mapa.entrySet());
        lista.sort((e1, e2) -> {
            //primero ordene por arrivo
            int llegada1 = e1.getValue().get(0);
            int llegada2 = e2.getValue().get(0);
            return Integer.compare(llegada1, llegada2);
          
        });
        LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>();
        for (Map.Entry<String, ArrayList<Integer>> entry : lista) {
            mapaOrdenado.put(entry.getKey(), entry.getValue());
        }

        return mapaOrdenado;
    }

    public static LinkedHashMap<String, ArrayList<Integer>> ordenarProcesosSJF(HashMap<String, ArrayList<Integer>> mapa) {
        List<Map.Entry<String, ArrayList<Integer>>> procesos = new ArrayList<>(mapa.entrySet());
        LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>();

        int tiempo = 0;
        while (!procesos.isEmpty()) {
            //ver quienes llegaron en ese tiempo
            List<Map.Entry<String, ArrayList<Integer>>> disponibles = new ArrayList<>();
            for (Map.Entry<String, ArrayList<Integer>> p : procesos) {
                if (p.getValue().get(0) <= tiempo) {
                    disponibles.add(p);
                }
            }

            if (disponibles.isEmpty()) {
                tiempo++;
                continue;
            }

            //elegir menor rafaga
            Map.Entry<String, ArrayList<Integer>> siguiente = disponibles.get(0);
            for (Map.Entry<String, ArrayList<Integer>> p : disponibles) {
                if (p.getValue().get(1) < siguiente.getValue().get(1)) {
                    siguiente = p;
                }
            }

            mapaOrdenado.put(siguiente.getKey(), siguiente.getValue());
            tiempo += siguiente.getValue().get(1);
            procesos.remove(siguiente);
        }
        return mapaOrdenado;
    }
    /**
     * 
     * entrada: {test3.asm=[3, 4], test4.asm=[2, 2], test6.asm=[1, 5], test5.asm=[2, 3]}
     * salida:  
     * {test6.asm=[1, 1], test4.asm=[2, 1],test4.asm=[2, 1], 
     * test5.asm=[2, 1],test5.asm=[2, 1],test5.asm=[2, 1]
     * test6.asm=[1, 1],test6.asm=[1, 1],test6.asm=[1, 1],test6.asm=[1, 1]
     * ,test3.asm=[3, 1],test3.asm=[3, 1],test3.asm=[3, 1],test3.asm=[3, 1]}
     */
    
    public static List<Map.Entry<String, ArrayList<Integer>>> modificarHash(List<Map.Entry<String, ArrayList<Integer>>> procesos,String nombre,ArrayList<Integer> proceso ) {
        List<Map.Entry<String, ArrayList<Integer>>> resultado = new ArrayList<>();

        for (Map.Entry<String, ArrayList<Integer>> p : procesos) {
             String comp = p.getKey();
             if (comp.equals(nombre)) {
                 resultado.add(new AbstractMap.SimpleEntry<>(comp, proceso));
                    
             }else{
                 resultado.add(p);
             }
        }
     
        return resultado;
    }

  
    public static LinkedHashMap<String, ArrayList<Integer>> ordenarProcesosSRT(HashMap<String, ArrayList<Integer>> mapa) { 
        List<Map.Entry<String, ArrayList<Integer>>> procesos = new ArrayList<>(mapa.entrySet()); 
        LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>(); 
        int tiempo = 0; while (!procesos.isEmpty()) { 
            List<Map.Entry<String, ArrayList<Integer>>> disponibles = new ArrayList<>(); 
            for (Map.Entry<String, ArrayList<Integer>> p : procesos) { 
                if (p.getValue().get(0) <= tiempo) { 
                    disponibles.add(p); 
                    } 
            } 
            if (disponibles.isEmpty()) { 
                tiempo++; 
                continue;
            }
            Map.Entry<String, ArrayList<Integer>> siguiente = disponibles.get(0);
            int minRafaga = siguiente.getValue().get(1);
            int minllegada = siguiente.getValue().get(0);

            for (Map.Entry<String, ArrayList<Integer>> p : disponibles) {
                int rafaga = p.getValue().get(1);
                int llegada = p.getValue().get(0) ;
                if (rafaga < minRafaga || 
                    (rafaga == minRafaga && llegada <minllegada )) {
                    siguiente = p;
                    minRafaga = rafaga;
                  
                }
            }

           mapaOrdenado.put(siguiente.getKey()+"_"+tiempo, new ArrayList<>(List.of(siguiente.getValue().get(0), 1)));

           int newarrivo = siguiente.getValue().get(0); 
           int newrafaga = siguiente.getValue().get(1); 
            if(siguiente.getValue().get(1)-1<=0){ 
                procesos.remove(siguiente); 
            }
            else{ 
                
                procesos = modificarHash(procesos,siguiente.getKey(),new ArrayList<>(Arrays.asList(newarrivo, newrafaga-1))); 
            } 
            tiempo += 1; 
        } 
        
        return mapaOrdenado; 


    }
    public static String getNombreKey(String pkey) { 
        String[] partes = pkey.toString().split("_");
        return partes[0]; 
    }
    public static LinkedHashMap<String, ArrayList<Integer>> ajustarSalidaSRT(HashMap<String, ArrayList<Integer>> mapa) { 
        List<Map.Entry<String, ArrayList<Integer>>> procesos = new ArrayList<>(mapa.entrySet()); 
        LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>(); 
        if (procesos.isEmpty()) return mapaOrdenado;
        int tiempo=0;
        
        Map.Entry<String, ArrayList<Integer>> anterior = procesos.get(0);
        String nombreAnterior = getNombreKey(anterior.getKey());
        int llegada = anterior.getValue().get(0);
        int rafaga = anterior.getValue().get(1);
        
        for (int i =1;i<procesos.size();i++) { 
            Map.Entry<String, ArrayList<Integer>> actual = procesos.get(i);
            String nombreActual = getNombreKey(actual.getKey());
            int llegadaActual = actual.getValue().get(0);
            int rafagaActual = actual.getValue().get(1);
            
            if(nombreActual.equals(nombreAnterior)){
                rafaga+=rafagaActual;
            }else{
                mapaOrdenado.put(nombreAnterior+"_"+tiempo, new ArrayList<>(List.of(llegada, rafaga)));
                nombreAnterior = nombreActual;
                llegada = llegadaActual;
                rafaga = rafagaActual;
                
            }
            tiempo++;
           
           
        } 
        mapaOrdenado.put(nombreAnterior+"_"+tiempo, new ArrayList<>(List.of(llegada, rafaga)));

        return mapaOrdenado; 


    }

    


}
