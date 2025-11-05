/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;



public class Planificador {
    private Map<String,BCP> procesos;
    
    public Planificador(){
        this.procesos = new LinkedHashMap<>();
    }
    public void agregarProceso(String nombre, BCP bcp){
        procesos.put(nombre, bcp);
    }
    public BCP obeterProceso(String nombre){
        return procesos.get(nombre);
    }
    public BCP obtenerSiguienteProceso() {
        if (procesos.isEmpty()) return null;
        return procesos.values().iterator().next(); 
    }
    public void eliminarProceso(String nombre){
        procesos.remove(nombre);
    }
    public BCP obtenerProcesoIndice(int indice){
        return new ArrayList<>(procesos.values()).get(indice);
    }
    public void eliminarSiguienteProceso() {
        if (!procesos.isEmpty()) {
            String primeraClave = procesos.keySet().iterator().next();
            procesos.remove(primeraClave);
        }
    }

    
    public boolean existeProceso(String nombre) {
        return procesos.containsKey(nombre);
    }
    
    public int sizeProceso(){return procesos.size();}
    public Map<String, BCP> getProcesos() {
        return procesos;
    }
    public void setProcesos(HashMap<String, BCP> procesos) {
        this.procesos = procesos;
    }

    @Override
    public String toString() {
        return "Planificador{" + "procesos=" + procesos + '}';
    }
    
}
