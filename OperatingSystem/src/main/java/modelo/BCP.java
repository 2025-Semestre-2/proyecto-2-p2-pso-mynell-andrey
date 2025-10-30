/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class BCP {
    private int idProceso;
    private String estado; //nuevo, preparado, ejecución, en espera, finalizado  
    private int prioridad;
    
    private int pc; //contador del programa
    private int base; //direccion inicio en memoria
    private int alcance; // tamaño del proceso
    
    private int ac,ax,bx,cx,dx;
    private String ir;
    
    private Stack<Integer> pila; //tamaño de 5
    private String siguiente;
    private List<String> archivos;//lista de archivos que el proceso tiene abiertos
    
    private String cpuAsig;

   
    //estaditicas
    private long tiempoInicio;
    private long tiempoFin;
    private long tiempoTotal;

    public BCP(int idProceso, String estado, int prioridad, int base, int alcance) {
        this.idProceso = idProceso;
        this.estado = estado;
        this.prioridad = prioridad;
        this.base = base;
        this.alcance = alcance;
        this.siguiente = siguiente;
        this.pila = new Stack<>();
        this.archivos = new ArrayList<>();
        this.tiempoInicio = System.currentTimeMillis();
        this.tiempoTotal = 0;
    }
    public BCP() {
        this.estado = estado;
        this.prioridad = 1;
        this.base = 0;
        this.siguiente = siguiente;
        this.alcance = 0;
        this.pila = new Stack<>();
        this.archivos = new ArrayList<>();
        this.tiempoInicio = System.currentTimeMillis();
        this.tiempoTotal = 0;
    }
    //mas getter y setter

    
     public String getCpuAsig() {
        return cpuAsig;
    }

    public void setCpuAsig(String cpuAsig) {
        this.cpuAsig = cpuAsig;
    }
    public int getIdProceso() {
        return idProceso;
    }

    public String getEstado() {
        return estado;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public int getPc() {
        return pc;
    }

    public int getBase() {
        return base;
    }

    public int getAlcance() {
        return alcance;
    }

    public int getAc() {
        return ac;
    }

    public int getAx() {
        return ax;
    }

    public int getBx() {
        return bx;
    }

    public int getCx() {
        return cx;
    }

    public int getDx() {
        return dx;
    }

    public String getIr() {
        return ir;
    }

    public Stack<Integer> getPila() {
        return pila;
    }

    public String getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(String siguiente) {
        this.siguiente = siguiente;
    }
    

    public List<String> getArchivos() {
        return archivos;
    }

    public long getTiempoInicio() {
        return tiempoInicio;
    }

    public long getTiempoFin() {
        return tiempoFin;
    }

    public long getTiempoTotal() {
        return tiempoTotal;
    }

    public void setIdProceso(int idProceso) {
        this.idProceso = idProceso;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public void setAlcance(int alcance) {
        this.alcance = alcance;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public void setAx(int ax) {
        this.ax = ax;
    }

    public void setBx(int bx) {
        this.bx = bx;
    }

    public void setCx(int cx) {
        this.cx = cx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setIr(String ir) {
        this.ir = ir;
    }

    public void setPila(Stack<Integer> pila) {
        this.pila = pila;
    }



    public void setArchivos(List<String> archivos) {
        this.archivos = archivos;
    }

    public void setTiempoInicio(long tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    public void setTiempoFin(long tiempoFin) {
        this.tiempoFin = tiempoFin;
    }

    public void setTiempoTotal(long tiempoTotal) {
        this.tiempoTotal = tiempoTotal;
    }
    @Override
    public String toString() {
        return "BCP{" +
                "idProceso=" + idProceso +
                ", estado='" + estado + '\'' +
                ", prioridad=" + prioridad +
                ", base=" + base +
                ", alcance=" + alcance +
                ", hilo=" + cpuAsig +
                ", archivos=" + archivos +
                '}';
    }

    
}
