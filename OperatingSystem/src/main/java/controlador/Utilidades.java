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
    public static String getNombreKey(String pkey) { 
        String[] partes = pkey.split("_");
        return partes[0]; 
    }
    public static Integer getIndiceKey(HashMap<String, ArrayList<Integer>> mapa,String nombre) {
        List<Map.Entry<String, ArrayList<Integer>>> lista = new ArrayList<>(mapa.entrySet());
   
        int indice =-1;
        lista.size();
        for (int i = 0; i<lista.size();i++) {
             String comp = lista.get(i).getKey();
         
             if (comp.equals(nombre)) {
                 indice =i;  
                 break;
             }
        }
     
        return indice;
    }
    
    public static Integer getRafagaEjecutada(HashMap<String, ArrayList<Integer>> mapa,String nombre,int pos) {
        List<Map.Entry<String, ArrayList<Integer>>> lista = new ArrayList<>(mapa.entrySet());
        
        int rafaga =0;
        for (int i = 0; i<=pos;i++) {
             String comp = lista.get(i).getKey();
             comp = getNombreKey(comp);
     
             if (comp.equals(nombre)) {
                 int r = lista.get(i).getValue().get(1);
                 rafaga+=r;  
                 
             }
        }
        return rafaga;
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
    /**
     * 4= test3.asm=[3, 4]
     * 3=test4.asm=[2, 2]
     * 1= test6.asm=[1, 5]
     * 2=test5.asm=[2, 3]
     */
        public static LinkedHashMap<String, ArrayList<Integer>> ordenarProcesosRR(HashMap<String, ArrayList<Integer>> mapa,
                int quantum) { 
            List<Map.Entry<String, ArrayList<Integer>>> procesos = new ArrayList<>(mapa.entrySet()); 
            LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>(); 
            
            Queue<Map.Entry<String, ArrayList<Integer>>> cola = new LinkedList<>();
            int tiempo = 0; 
            
            while (!procesos.isEmpty() ||!cola.isEmpty()) {

                Iterator<Map.Entry<String, ArrayList<Integer>>> it = procesos.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, ArrayList<Integer>> p = it.next();
                    if (p.getValue().get(0) <= tiempo) {
                        cola.add(p);
                        it.remove();
                    }
                }

                if (cola.isEmpty()) {
                    tiempo++; 
                    continue;
                }
                Map.Entry<String, ArrayList<Integer>> actual = cola.poll();
                int llegada = actual.getValue().get(0);
                int rafaga = actual.getValue().get(1);

                int ejecucion = Math.min(quantum, rafaga);
                mapaOrdenado.put(actual.getKey() + "_" + tiempo, new ArrayList<>(List.of(llegada, ejecucion)));

                tiempo += ejecucion;

                if (rafaga - ejecucion > 0) {
                    ArrayList<Integer> nuevo = new ArrayList<>(Arrays.asList(llegada, rafaga - ejecucion));
                    cola.add(new AbstractMap.SimpleEntry<>(actual.getKey(), nuevo));
                } 
            }
            return mapaOrdenado; 
        }
        
        public static LinkedHashMap<String, ArrayList<Integer>> ordenarProcesosHRRN(
            HashMap<String, ArrayList<Integer>> mapa) {

            class Proceso {
                String nombre;
                int llegada;
                int rafaga;

                Proceso(String nombre, int llegada, int rafaga) {
                    this.nombre = nombre;
                    this.llegada = llegada;
                    this.rafaga = rafaga;
                }
            }

            List<Proceso> procesos = new ArrayList<>();
            for (Map.Entry<String, ArrayList<Integer>> entry : mapa.entrySet()) {
                procesos.add(new Proceso(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1)));
            }

            LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>();
            int tiempo = 0;

            while (!procesos.isEmpty()) {
                // Obtener procesos que ya han llegado
                List<Proceso> disponibles = new ArrayList<>();
                for (Proceso p : procesos) {
                    if (p.llegada <= tiempo) {
                        disponibles.add(p);
                    }
                }

                if (disponibles.isEmpty()) {
                    tiempo++;
                    continue;
                }
                final int t = tiempo;
                // Calcular Response Ratio y elegir el mayor
                Proceso siguiente = Collections.max(disponibles, (p1, p2) -> {
                    double rr1 = (double)(t - p1.llegada + p1.rafaga) / p1.rafaga;
                    double rr2 = (double)(t - p2.llegada + p2.rafaga) / p2.rafaga;
                    return Double.compare(rr1, rr2);
                });

                // Ejecutar completamente
                mapaOrdenado.put(siguiente.nombre + "_" + tiempo, 
                                 new ArrayList<>(List.of(siguiente.llegada, siguiente.rafaga)));

                tiempo += siguiente.rafaga;
                procesos.remove(siguiente);
            }

            return mapaOrdenado;
        }

        
        public static LinkedHashMap<String, ArrayList<Integer>> ordenarProcesosCFS(
            HashMap<String, ArrayList<Integer>> mapa) {

            class Proceso {
                String nombre;
                int llegada;
                int rafagaRestante;
                double vruntime;

                Proceso(String nombre, int llegada, int rafaga) {
                    this.nombre = nombre;
                    this.llegada = llegada;
                    this.rafagaRestante = rafaga;
                    this.vruntime = 0;
                }
            }

            List<Proceso> procesos = new ArrayList<>();
            for (Map.Entry<String, ArrayList<Integer>> entry : mapa.entrySet()) {
                procesos.add(new Proceso(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1)));
            }

            LinkedHashMap<String, ArrayList<Integer>> mapaOrdenado = new LinkedHashMap<>();
            List<Proceso> activos = new ArrayList<>();

            int tiempo = 0;

            while (!procesos.isEmpty() || !activos.isEmpty()) {
                // Mover procesos que ya llegaron a la lista de activos
                Iterator<Proceso> it = procesos.iterator();
                while (it.hasNext()) {
                    Proceso p = it.next();
                    if (p.llegada <= tiempo) {
                        activos.add(p);
                        it.remove();
                    }
                }

                if (activos.isEmpty()) {
                    tiempo++;
                    continue;
                }

                // Seleccionar el proceso con menor vruntime (más justo)
                activos.sort(Comparator.comparingDouble(p -> p.vruntime));
                Proceso actual = activos.get(0);

                // Ejecutar 1 unidad de tiempo
                int ejecucion = 1;
                mapaOrdenado.put(actual.nombre + "_" + tiempo, new ArrayList<>(List.of(actual.llegada, ejecucion)));

                tiempo += ejecucion;
                actual.rafagaRestante -= ejecucion;
                actual.vruntime += ejecucion; // En versión simplificada, igual a tiempo real

                // Si terminó, quitarlo de activos
                if (actual.rafagaRestante <= 0) {
                    activos.remove(actual);
                }
            }

            return mapaOrdenado;
        }


        
        private static final String REGISTRO = "(AX|BX|CX|DX)";
        private static final String ENTERO = "(-?\\d+)";
        private static final String ETIQUETA = "[A-Za-z_][A-Za-z0-9_]*";
        private static final String VALORES = "([A-Za-z0-9_,\\s]*)";

        public static boolean validarLinea(String linea) {
            linea = linea.trim().toUpperCase();

            // Instrucciones básicas
            String regexLOAD = "LOAD\\s+" + REGISTRO;
            String regexSTORE = "STORE\\s+" + REGISTRO;
            String regexMOV = "MOV\\s+" + REGISTRO + "\\s*,\\s*(" + REGISTRO + "|" + ENTERO + ")";
            String regexADD = "ADD\\s+" + REGISTRO;
            String regexSUB = "SUB\\s+" + REGISTRO;
            String regexINC = "INC\\s+" + REGISTRO;
            String regexDEC = "DEC\\s+" + REGISTRO;

            // Variantes especiales
            String regexMOVS = "MOV\\s+" + REGISTRO + "\\s*,\\s*S";
            String regexDECAX = "DEC\\s+AX";
            String regexDECReg = "DEC\\s+" + REGISTRO;
            String regexINCReg = "INC\\s+" + REGISTRO;
            String regexSWAP = "SWAP\\s+" + REGISTRO + "\\s*,\\s*" + REGISTRO;

            // Interrupciones
            String regexINT20H = "INT\\s+20H";
            String regexINT09H = "INT\\s+09H";
            String regexINT10H = "INT\\s+10H";
            String regexINT21H = "INT\\s+21H";

            // Saltos y comparaciones
            String regexJMP = "JMP\\s+" + ETIQUETA;
            String regexCMP = "CMP\\s+" + REGISTRO + "\\s*,\\s*" + REGISTRO;
            String regexJE = "JE\\s+" + ETIQUETA;
            String regexJNE = "JNE\\s+" + ETIQUETA;

            // Parámetros y pila
            String regexPARAM = "PARAM\\s+" + VALORES;
            String regexPUSH = "PUSH\\s+" + REGISTRO;
            String regexPOP = "POP\\s+" + REGISTRO;

            // Unir todas las posibles instrucciones
            return linea.matches(regexLOAD) ||
                   linea.matches(regexSTORE) ||
                   linea.matches(regexMOV) ||
                   linea.matches(regexMOVS) ||
                   linea.matches(regexADD) ||
                   linea.matches(regexSUB) ||
                   linea.matches(regexINC) ||
                   linea.matches(regexDEC) ||
                   linea.matches(regexSWAP) ||
                   linea.matches(regexINT20H) ||
                   linea.matches(regexINT09H) ||
                   linea.matches(regexINT10H) ||
                   linea.matches(regexINT21H) ||
                   linea.matches(regexJMP) ||
                   linea.matches(regexCMP) ||
                   linea.matches(regexJE) ||
                   linea.matches(regexJNE) ||
                   linea.matches(regexPARAM) ||
                   linea.matches(regexPUSH) ||
                   linea.matches(regexPOP);
        }

}
