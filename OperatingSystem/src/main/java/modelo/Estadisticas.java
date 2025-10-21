/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

public class Estadisticas {
    public static class Registro {
        private int idProceso;
        private long tiempo;
        
        public Registro(int idProceso, long tiempo){
            this.idProceso = idProceso;
            this.tiempo = tiempo;
        }

        public int getIdProceso() {
            return idProceso;
        }

        public long getTiempo() {
            return tiempo;
        }
        @Override
        public String toString() {
            return "["+idProceso + ", " + tiempo + " ms]";
        }
        
    }
    private List<Registro> registros;
    public Estadisticas(){
        this.registros = new ArrayList<>();
    }
    public void agregar(int idProceso,long tiempo){
        registros.add(new Registro(idProceso,tiempo));
    }
    public List<Registro> getRegistros() {
        return registros;
    }
    public String toString() {
        return registros.toString();
    }
    
    
    
}
