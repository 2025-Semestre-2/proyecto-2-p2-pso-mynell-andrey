/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.util.LinkedList;
import java.util.Queue;
/**
 *
 * @author Andrey
 */
public class Planificador {
    private Queue<BCP> colaListos;
    
    public Planificador(){
        colaListos = new LinkedList<>();
    }
    public void agregarProceso(BCP bcp){
        colaListos.add(bcp);
    }
    public BCP obeterSiguienteProceso(){
        BCP proceso = colaListos.poll();
        return proceso;
    }
    public BCP verSiguiente(){
        return colaListos.peek();
    }
    public int sizeCola(){return colaListos.size();}
    public Queue<BCP> getColaListos(){
        return colaListos;
    }
}
