/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package modelo;

/**
 *
 * @author Andrey
 * Clase Memoria, que representa la memoria principal de la minipc
 * Objetivo: Almcenar las intrucciones en la posicion indicada
 */
public class Memoria {
    private String[] memoria;
    /**
     * Contructor que inicializa el arreglo de la memoria
     */
  
    public Memoria(String[] memoria) {
        this.memoria = memoria;
    }
    
    /**
     * Contructor que inicializa el tamaño de la memoria
     */
    public Memoria(int size){
        memoria = new String[size];
    }
    /**
     * Asigna un valor a una posicion en memoria
     * Entrada: 
     * @param pos posicion en memoria
     * @param valor instruccion a almecenar
     * Salida: No tiene
     * Restricciones No tiene
     * Objetivo: Guardar a la instruccion en una posicion en memoria
     */
    public void setMemoria(int pos, String valor) {
        memoria[pos] = valor;
    }
    /**
     * Retorna un valor a una posicion en memoria
     * Entrada: 
     * @param pos posicion en memoria
     * @param valor instruccion a almecenar
     * Salida: instruccion almacenada en la posicion x
     * Restricciones No tiene
     * Objetivo: Ejecutar a la instruccion en una posicion en memoria
     */
    public String getMemoria(int pos) {
        return memoria[pos];
    }
    
    /**
     * Salida:
     * @return tamaño de la memoria
     */
    public int size() {
        return memoria.length;
    }
    
    public void reset(){
        memoria = new String[size()];
    }
}
