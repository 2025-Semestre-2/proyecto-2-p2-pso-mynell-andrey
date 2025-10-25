/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.util.HashMap;



public class Planificador {
    private HashMap<String,BCP> procesos;
    
    public Planificador(){
        this.procesos = new HashMap<>();
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
    public HashMap<String, BCP> getProcesos() {
        return procesos;
    }
    public void setProcesos(HashMap<String, BCP> procesos) {
        this.procesos = procesos;
    }
}
