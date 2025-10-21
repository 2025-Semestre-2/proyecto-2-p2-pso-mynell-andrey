/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mynell
Clase Disco, que representa la disco principal de la minipc
Objetivo: Almcenar las intrucciones en la posicion indicada
 */
public class Disco {
    private final File discoFile;
    private final int indices= 50;
    
    public Disco(String rutaDisco)throws IOException{
        this.discoFile = new File(rutaDisco);
        if (!discoFile.exists()) inicializarArchivo();
    }
    private void inicializarArchivo() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(discoFile))) {
            for (int i = 0; i < 512; i++) {
                bw.newLine();
            }
        }
    }
    
    public List<String> leerTodo() throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(discoFile))) {
            String l;
            while ((l = br.readLine()) != null) {
                lineas.add(l);
            }
        }
        return lineas;
    }
    
    public List<String> getDatos() throws IOException{
        List<String> lista = leerTodo();
        int limite = lista.size(); 
        for (int i = indices; i < lista.size(); i++) {
            if (lista.get(i).trim().isEmpty()) { 
                limite = i; 
                break;
            }
        }
        return lista.subList(indices, limite);
    }
    
    private void escribirTodo(List<String> lineas) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(discoFile))) {
            for (String l : lineas) {
                bw.write(l);
                bw.newLine();
            }
        }
    }
    
    public List<String> listarArchivos() throws IOException {
        List<String> lineas = leerTodo();
        List<String> archivos = new ArrayList<>();
        for (int i = 0; i < indices && i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (!linea.isEmpty()) {
                archivos.add(linea.split("\\|")[0]);
            }
        }
        return archivos;
    }
    
    public void crearArchivo(String nombre, List<String> contenido) throws IOException {
        List<String> lineas = leerTodo();
        
        int longitud = contenido.size();
        int posicionInicio = -1;
        int contador = 0;
        for (int i = indices; i < lineas.size(); i++) {
            if (lineas.get(i).trim().isEmpty()) {
                contador++;
                if (contador == longitud) {
                    posicionInicio = i - longitud + 1;
                    break;
                }
            } else {
                contador = 0;
            }
        }
        if (posicionInicio != -1) {
            for (int i = 0; i < longitud; i++) {
                lineas.set(posicionInicio + i, contenido.get(i));
            }
            boolean agregado = false;
            for (int i = 0; i < indices; i++) {
                if (lineas.get(i).trim().isEmpty()) {
                    lineas.set(i, nombre + "|" + posicionInicio + "|" + longitud);
                    agregado = true;
                    break;
                }
            }
            if (!agregado) throw new IOException("Índice lleno");
            escribirTodo(lineas);
        }else{
            throw new IOException("Espacio en Disco Insuficiente");
        }
    }
    
    public List<String> leerArchivo(String nombre) throws IOException {
        List<String> lineas = leerTodo();
        for (int i = 0; i < indices && i < lineas.size(); i++) {
            String l = lineas.get(i).trim();
            if (l.startsWith(nombre + "|")) {
                String[] parts = l.split("\\|");
                int inicio = Integer.parseInt(parts[1]);
                int longitud = Integer.parseInt(parts[2]);
                List<String> contenido = new ArrayList<>();
                for (int j = inicio; j < inicio+ longitud && j < lineas.size(); j++) {
                    contenido.add(lineas.get(j));
                }
                return contenido;
            }
        }
        throw new IOException("Archivo no encontrado");
    }
    
    public void eliminarArchivo(String nombre) throws IOException {
        List<String> lineas = leerTodo();
        for (int i = 0; i < indices && i < lineas.size(); i++) {
            if (lineas.get(i).startsWith(nombre + "|")) {
                String[] partes = lineas.get(i).split("\\|");
                if (partes.length == 3) {
                    int inicio = Integer.parseInt(partes[1]);
                    int cantidad = Integer.parseInt(partes[2]);
                    for (int j = inicio; j < inicio + cantidad && j < lineas.size(); j++) {
                        lineas.set(j, "");
                    }
                }
                lineas.set(i, "");
                break;
            }
        }
        escribirTodo(lineas);
    }

    
    public void setDisco (int pos, String contenido){
        try {
            List<String> disco = leerTodo();
            disco.set(pos, contenido);
            escribirTodo(disco);
        } catch (IOException ex) {
            System.getLogger(Disco.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }
    
    public String getDisco(int pos){
        try {
            return leerTodo().get(pos);
        } catch (IOException ex) {
            System.getLogger(Disco.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
    
    public void ClearAll(){
        try {
            escribirTodo(List.of());
            inicializarArchivo();
        } catch (IOException ex) {
            System.getLogger(Disco.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public void cambiarTamano(int nuevoTamano) throws IOException {
        List<String> lineas = leerTodo();
        if(nuevoTamano>indices){
            if (nuevoTamano >= lineas.size()) {
                while(lineas.size()<nuevoTamano){
                    lineas.add("");
                }
                escribirTodo(lineas);
            }else{
                if (!lineas.get(nuevoTamano).trim().isEmpty()) {
                    throw new IOException("No se puede reducir: existen datos en el rango a eliminar.");
                }else escribirTodo(lineas.subList(0, nuevoTamano));
            }
        }else throw new IOException("No se puede reducir: existen datos en el rango a eliminar.");
    }

    
    public int size() throws IOException {
        return leerTodo().size();
    }
}
