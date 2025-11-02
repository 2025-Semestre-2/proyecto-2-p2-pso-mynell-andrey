
package modelo;

import controlador.Particion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Memoria {
    public static HashMap<Integer, Particion> particionesFija = new HashMap<>();
    private static int ultimaParticionAsignada = 0;
    private String[] memoria;
 
  
    public Memoria(String[] memoria) {
        this.memoria = memoria;
    }
    
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
