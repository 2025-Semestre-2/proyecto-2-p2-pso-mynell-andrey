/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mvc;

import modelo.SistemaOperativo;
import vista.View;
import vista.Estadistica;
import controlador.Controlador;

public class Main {

    public static void main(String[] args) {
        SistemaOperativo modelo = new SistemaOperativo();
        View view = new View();
     
        Estadistica estadistica = new Estadistica();
        Controlador controlador = new Controlador(modelo, view,estadistica);
        view.setVisible(true);
    }
}
