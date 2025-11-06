
package modelo;

import controlador.Particion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Memoria {
    public static HashMap<Integer, Particion> particionesFija = new HashMap<>();
    public static List<Particion> bloquesDinamicos = new ArrayList<>();
    private static int ultimaParticionAsignada = 0;
    private static final int TAM_PAGINA = 16;
    private String[] memoria;
 
  
    public Memoria(String[] memoria) {
        this.memoria = memoria;
    }
    /*
    =========================================
    FIJO
    =========================================
    */
    public static Particion obtenerParticionLibre() {
        List<Integer> keys = new ArrayList<>(particionesFija.keySet());
        int n = keys.size();
        for (int i = 0; i < n; i++) {
            int idx = (ultimaParticionAsignada + i) % n;
            Particion p = particionesFija.get(keys.get(idx));
            if (!p.isOcupado()) {
                ultimaParticionAsignada = idx + 1; 
                return p;
            }
        }
        return null;
    }

    public static void liberarParticion(int nombre) {
        if (particionesFija.containsKey(nombre)) {
            particionesFija.get(nombre).setOcupado(false);
        }
    }
    
    public static Integer obtenerKeyIndice(int indice){
        for (Map.Entry<Integer, Particion> entry : particionesFija.entrySet()) {
            Particion p = entry.getValue();
            if (p.getBase() == indice) {
                return entry.getKey();
            }
        }
        return null;
        
    }
    /*
    =========================================
    DINAMICO
    =========================================
    */
    public void inicializarBloqueDinamico(int tam){
        if (bloquesDinamicos.isEmpty()) {
            int inicio = Math.max(20, tam/5);
            bloquesDinamicos.add(new Particion(inicio,tam-inicio,false));
            ultimaParticionAsignada = 0;
        }
        
    }
    public  Particion asignarBloqueDinamico() {
        int n = bloquesDinamicos.size();
        System.out.println("tam"+ n);
        if (n == 0) return null;
        boolean encontrado = false;
    
        for (int i = ultimaParticionAsignada; i < n; i++) {
            System.out.println("ulyima:"+ultimaParticionAsignada);
        
            Particion p = bloquesDinamicos.get(i);
            System.out.println(p);

            if (!p.isOcupado() && p.getTamaño() >= 16) {
                int base = p.getBase();
                p.setOcupado(true);
                encontrado = true;
                int nuevaBase = base + 16;
                int nuevoTam = p.getTamaño() - 16;

                Particion nuevo = new Particion(nuevaBase, nuevoTam, false);
                bloquesDinamicos.add(nuevo);
                p.setTamaño(16);
                
                ultimaParticionAsignada++;
                System.out.println("ultimo"+ultimaParticionAsignada);
                System.out.println("Bloques actuales: " + bloquesDinamicos);
                return p;
               
            }
        }
  
        if (!encontrado) {
            ultimaParticionAsignada = 0;
            return asignarBloqueDinamico();
        }

        return null;
    }

    public  void liberarBloqueDinamico(int base) {
        System.out.println("tam"+bloquesDinamicos.size());
        for (int i = 0; i < bloquesDinamicos.size(); i++) {
            Particion p = bloquesDinamicos.get(i);
            if (p.getBase() == base) {
                p.setOcupado(false);
                break;
            }
        }
    }
    /*
    =========================================
    SEGMENTACION
    =========================================
    */
    public void inicializarAsegmento(int tam){
        if (bloquesDinamicos.isEmpty()) {
            int inicio = Math.max(20, tam/5);
            bloquesDinamicos.add(new Particion(inicio,16,false));
            ultimaParticionAsignada = 0;
        }
        
    }
    public  Particion asignarSegemento() {
        int n = bloquesDinamicos.size();
        if (n == 0) return null;
        boolean encontrado = false;
    
        for (int i = ultimaParticionAsignada; i < n; i++) {
       
        
            Particion p = bloquesDinamicos.get(i);
            System.out.println(p);

            if (!p.isOcupado() && p.getTamaño() >= 16) {
                int base = p.getBase();
                p.setOcupado(true);
                encontrado = true;
                int nuevaBase = base + 16;

                Particion nuevo = new Particion(nuevaBase, 16, false);
                bloquesDinamicos.add(nuevo);

                ultimaParticionAsignada++;
                return p;
               
            }
        }
  
        if (!encontrado) {
            ultimaParticionAsignada = 0;
            return asignarBloqueDinamico();
        }

        return null;
    }
        /*
    =========================================
    PAGINADO
    =========================================
    */


    public void inicializarPaginacion(int tamMemoria) {
        bloquesDinamicos.clear();
        int base = Math.max(20, size()/5);
        int cantidadMarcos = (tamMemoria-base) / TAM_PAGINA;
        for (int i = 0; i < cantidadMarcos; i++) {
            bloquesDinamicos.add(new Particion(base, TAM_PAGINA, false));
            base += TAM_PAGINA;
        }
        ultimaParticionAsignada = 0;
        System.out.println("Memoria inicializada con " + cantidadMarcos + " marcos de tamaño " + TAM_PAGINA);
    }
    
    public Particion asignarPagina() {
        int n = bloquesDinamicos.size();
        if (n == 0) {
            System.out.println("No hay marcos inicializados.");
            return null;
        }

        for (int i = ultimaParticionAsignada; i < n; i++) {
            Particion marco = bloquesDinamicos.get(i);
            if (!marco.isOcupado()) {
                marco.setOcupado(true);
                ultimaParticionAsignada = (i + 1) % n;
                System.out.println("Página asignada en marco base " + marco.getBase());
                return marco;
            }
        }

        for (int i = 0; i < ultimaParticionAsignada; i++) {
            Particion marco = bloquesDinamicos.get(i);
            if (!marco.isOcupado()) {
                marco.setOcupado(true);
                ultimaParticionAsignada = (i + 1) % n;
                System.out.println("Página asignada en marco base " + marco.getBase());
                return marco;
            }
        }

        System.out.println("⚠️ No hay marcos disponibles (memoria llena)");
        return null;
    }
    public void limpiarMemoriaPP() {
        particionesFija.clear();
        bloquesDinamicos.clear();
    }
    public Memoria(int size){
        memoria = new String[size];
    }

    public void setMemoria(int pos, String valor) {
        memoria[pos] = valor;
    }

    public String getMemoria(int pos) {
        return memoria[pos];
    }
    
    public int size() {
        return memoria.length;
    }
    
    public void reset(){
        memoria = new String[size()];
    }
}
