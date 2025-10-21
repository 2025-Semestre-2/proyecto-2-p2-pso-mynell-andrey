/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Andrey
 * Clase CPU
 * Objetivo: Almacena los registros basico de la CPU (pc,ac,ir,..etc)
 */
public class CPU {
    
    private int PC, AC, AX,BX,CX,DX;
    private String IR;

    /**
     * Reinicia el estado del CPU
     * Entrada: No recibe parametros
     * Salida: ninguna
     * Restricciones: No posee restricciones, se pierde todo lo almacenado
     * Objetivo: Establecer los valores iniciales
     */
    public void reset(){
        PC=0;
        IR="";
        AC=0;
        AX = BX = CX = DX = 0;
    }
    /**
     * GETTER Y SETTER
     */
    public void setPC(int PC) {
        this.PC = PC;
    }

    public void setAC(int AC) {
        this.AC = AC;
    }

    public void setAX(int AX) {
        this.AX = AX;
    }

    public void setBX(int BX) {
        this.BX = BX;
    }

    public void setCX(int CX) {
        this.CX = CX;
    }

    public void setDX(int DX) {
        this.DX = DX;
    }

    public void setIR(String IR) {
        this.IR = IR;
    }
    public int getPC() {
        return PC;
    }

    public int getAC() {
        return AC;
    }

    public int getAX() {
        return AX;
    }

    public int getBX() {
        return BX;
    }

    public int getCX() {
        return CX;
    }

    public int getDX() {
        return DX;
    }

    public String getIR() {
        return IR;
    }

}
