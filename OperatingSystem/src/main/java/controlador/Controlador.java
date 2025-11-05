package controlador;

import static controlador.Utilidades.*;
import java.awt.HeadlessException;
import modelo.SistemaOperativo;
import static modelo.Memoria.obtenerKeyIndice;

import vista.View;

import vista.Estadistica;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.BCP;
import modelo.Estadisticas;

public class Controlador {
    private SistemaOperativo pc;
    private View view;
    private Estadistica estadistica;
    private Estadisticas est;
    private int indiceArch = 0;
    private int contEjecucion =0;
    //var glob para paso a paso
    private final Object lock = new Object();
    private boolean modoPasoPaso = false;
    private boolean hiloIniciado = false;

    public Controlador(SistemaOperativo pc, View view,Estadistica estadistica ) {
        this.pc = pc;
        this.view = view;
        this.est = new Estadisticas();
  
        this.estadistica = estadistica;
        this.view.btnBuscarListener(e -> buscarArchivo());
        this.view.btnEjecutar(e -> modoEjecucion());
        this.view.btnLimpiar(e -> cleanAll());
        this.view.btnReset(e -> clean());
        this.view.btnPasoListener(e -> {
            if (!hiloIniciado) {
                // Primer clic: inicia el modo paso a paso
                modoPasoPaso = true;
                hiloIniciado = true;
                modoEjecucion();
            } else {
                // Clics siguientes: avanzan una instrucción
                synchronized (lock) {
                    lock.notify();
                }
            }
        
        });
        try {
            this.view.setDiskSize(pc.getDisco().size());
        } catch (IOException ex) {
            System.getLogger(Controlador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        this.view.getSpnMemoria().addChangeListener(e -> showRam());
        this.view.btnVerEst(e -> mostrarEstadistica());
        this.estadistica.btnVolver(e -> volverEst());
        this.view.discoStageChange(e -> spinnerTamano());
        showDisk();
        showRam();
        inicializarSO();
        estadosIniciales();
        tablaEjecucion();
        
    }
    
    public void crearProcesos(){
        System.out.println("ar"+indiceArch+"+"+pc.getIntr().size());
        if (pc.getIntr() == null || pc.getIntr().isEmpty()) {
           return;
        }
    
        int contadorinstr =0;
        for(int i = indiceArch;i<pc.getIntr().size();i++){
            String instru = pc.getDisco().getDisco(i);
            
            if(instru.contains("|")){
                String[] partes = instru.split("\\|");
                String nombreArchivo = partes[0];
                BCP proceso = pc.getPlanificador().obeterProceso(nombreArchivo);
                
                view.addFilaProcesos(nombreArchivo,i,proceso.getAlcance());
                contadorinstr++;
            }
          
            
        } 
          System.out.println("bucle?");
        indiceArch = contadorinstr;
    }
        
    public void modoEjecucion(){
    
        String tipo = view.getCBoxCPU().toString();
        System.out.println(view.getProcesosTabla());
        HashMap<String, ArrayList<Integer>> ordenarProcesos; 
              
        switch(tipo){
            case "FCFS": 
                System.out.println("fcfs"+ordenarProcesosFCFS(view.getProcesosTabla()));
                ordenarProcesos = ordenarProcesosFCFS(view.getProcesosTabla());
                ejecutarAlgoritmo(ordenarProcesos);
          
                break;
            
            case "SJF": 
                System.out.println("spn"+ordenarProcesosSJF(view.getProcesosTabla()));
                ordenarProcesos = ordenarProcesosSJF(view.getProcesosTabla());
              
                ejecutarAlgoritmo(ordenarProcesos);
                break;
            case "SRT": 
        
                System.out.println("srt"+ajustarSalidaSRT(ordenarProcesosSRT(view.getProcesosTabla())));
                LinkedHashMap<String,ArrayList<Integer>> temp =ordenarProcesosSRT(view.getProcesosTabla());
                ordenarProcesos = ajustarSalidaSRT(temp);
        
                ejecutarAlgoritmo(ordenarProcesos);
                break;
            case "RR":
                String qstr=null;
                int q =-1;
                while(q<0){
                    qstr = JOptionPane.showInputDialog(null, "Introduce el quantum:");
                    if (qstr == null) {
                        JOptionPane.showMessageDialog(null, "Operación cancelada.");
                        return;
                    }
                    try{
                        q = Integer.parseInt(qstr);
                        if(q<=0){
                            JOptionPane.showMessageDialog(null, "El quantum debe ser un número entero positivo.");
                        }
                    }catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Error, Ingresa un número entero válido.");
                    }
                    
                }
                
                System.out.println("q="+q);
                System.out.println("rr"+ordenarProcesosRR(view.getProcesosTabla(),q));
                ordenarProcesos = ordenarProcesosRR(view.getProcesosTabla(),q);
                ejecutarAlgoritmo(ordenarProcesos);
                break;
            case "HRRN": 
                System.out.println("hrrn"+ordenarProcesosSJF(view.getProcesosTabla()));
                ordenarProcesos = ordenarProcesosHRRN(view.getProcesosTabla());

                ejecutarAlgoritmo(ordenarProcesos);
                break;
            case "CFS": 
                System.out.println("cfs"+ordenarProcesosSJF(view.getProcesosTabla()));
                ordenarProcesos = ordenarProcesosCFS(view.getProcesosTabla());
 
                ejecutarAlgoritmo(ordenarProcesos);
                break;
            default:
              JOptionPane.showMessageDialog(null, "El algoritmo "+tipo+ " aún no implementado");
              break;
        
        }
    }
        
    public void inicializarSO(){
        int sizeMemoria = (Integer) view.getSpnMemoria().getValue();
        int sizeDisco = (Integer) view.getSpnDisco().getValue();
        try {
            pc.tamannoDisco(sizeDisco);
            pc.tamannoMemoria(sizeMemoria);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al inicializar el disco: " + e.getMessage());
            return;
        }
        
        //inicializo
        pc.inicializarSO(sizeMemoria);
        pc.crearProcesos();
        crearProcesos();

 
    }
    public void actualizarMemoria(){
        int sizeMemoria = (Integer) view.getSpnMemoria().getValue();
         pc.tamannoMemoria(sizeMemoria);
 
        //inicializo
        pc.inicializarSO(sizeMemoria);
        pc.crearProcesos();

    }
    

    public void ejecutarAlgoritmo(HashMap<String, ArrayList<Integer>> mapa) {
        LinkedHashMap<String, ArrayList<Integer>> mapaRegistro = new LinkedHashMap<>(mapa);
        new Thread(() -> {   
            int next=0;
      
      
            while (mapa.size() > 0) {
                //memoria
                int indice = modoGuardado();
                System.out.println("indice:"+indice);
                //algortimos
                String nombreproceso = obtenerNombreProcesoU(mapa);
                String nombrekey = getNombreKey(nombreproceso);
                int rafaga = obtenerRafagaProcesoU(mapa);
                int getindice = getIndiceKey(mapaRegistro,nombreproceso);
                int nrafaga = getRafagaEjecutada(mapaRegistro,nombrekey,getindice);
                // tomar el proceso de la cola  
                BCP proceso = inicializarProcesos(nombrekey,indice,next,nrafaga);
                System.out.println("id"+proceso.getIdProceso());
                //ejecutar
                boolean stop = false;
                int i = proceso.getBase();
                while (i < proceso.getBase() + rafaga) {
                    String instr = pc.getDisco().getDisco(i);
                    if (instr != null) {
                        pc.getCPU(proceso.getCPU()).setIR(instr);
                        this.view.jTable3.changeSelection(i, i, false, false);
                        String res = pc.interprete(instr, proceso,pc.getCPU(proceso.getCPU()));
                        i = proceso.getPc();
                        view.marcarEjecucion("P"+proceso.getIdProceso(), contEjecucion);
                        contEjecucion++;
                        //entrada usuario
                        stop = procesarResultado(res,proceso.getCPU());
                        if (stop) break;
                    
                        if (modoPasoPaso) {
                            synchronized (lock) {
                                try {
                                    //espera otro click
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        } else {
                            esperar(pc.getTimer(instr)); // automático
                        }
                        // actualizar UI de memoria y BCP en el hilo de Swing
                        actualizarUI(proceso,indice,proceso.getIdProceso());
                    }
                }
                if (stop) break;
                // finalizar proceso
                finalizarProceso(proceso,indice,nrafaga);
                next++;
                eliminarProcesoU(mapa,nombreproceso);
                librarGuardado(indice);
            }
        }).start(); 
    }
    public int modoGuardado(){
        String tipo = view.getCBoxMemoria().toString();
              
        switch(tipo){
            case "Fija": 
                Particion particionLibre = pc.getMemoria().obtenerParticionLibre();
                if(particionLibre==null){
                    JOptionPane.showMessageDialog(null, "Error, no hay particiones libres");
                    return -1;
                }
                particionLibre.setOcupado(true);
                System.out.println("pp"+particionLibre);
               
                return particionLibre.getBase();
              
            
            case "Dinámica": 
                Particion bloque = pc.getMemoria().asignarBloqueDinamico();
                if(bloque==null){
                    JOptionPane.showMessageDialog(null, "Error en Dinamica, no hay particiones libres");
                    return -1;
                }
     
                return bloque.getBase();
            case "Segmentación": 
                Particion segmento = pc.getMemoria().asignarSegemento();
                if(segmento==null){
                    JOptionPane.showMessageDialog(null, "Error en Dinamica, no hay particiones libres");
                    return -1;
                }
     
                return segmento.getBase();
            case "Paginación":
                Particion marco = pc.getMemoria().asignarSegemento();
                if(marco==null){
                    JOptionPane.showMessageDialog(null, "Error en Dinamica, no hay particiones libres");
                    return -1;
                }
                return marco.getBase();
            default:
              JOptionPane.showMessageDialog(null, "El algoritmo "+tipo+ " aún no implementado");
              return -1;
        
        }
        
    }
    public void librarGuardado(int id){
        String tipo = view.getCBoxMemoria().toString();
              
        switch(tipo){
            case "Fija": 
                pc.getMemoria().liberarParticion(obtenerKeyIndice(id));
                break;
            
            case "Dinámica": 
                pc.getMemoria().liberarBloqueDinamico(id);
                break;
            case "Segmentación": 
                pc.getMemoria().liberarBloqueDinamico(id);
                break;
            case "Paginación":
                pc.getMemoria().liberarBloqueDinamico(id);
                break;
            default:
              JOptionPane.showMessageDialog(null, "El algoritmo "+tipo+ " aún no implementado");
              break;
        
        }
    }

    /*
    ===========================FUNCIONES ALGORITMOS AUX==========================
    */
    public BCP inicializarProcesos(String nombre,int indice, int next,int rafagaEjecutada){
        BCP proceso = pc.getPlanificador().obeterProceso(nombre);
       
        proceso.setSiguiente(getEnlace(next));

        if(proceso.getEstado().equals("nuevo") || proceso.getEstado().equals("espera")){
            updateBCP(proceso, indice);
            proceso.setEstado("preparado");
            updateBCP(proceso, indice);
         
        }
        // pasar a preparado: si esta preparado es por que ya fue ejecutado y esta esperando reanudar su ejecucion
        if(proceso.getEstado().equals("preparado")){
            proceso.setEstado("ejecucion");
            proceso.setTiempoInicio(System.currentTimeMillis());
            updateBCP(proceso, indice);
        }

        // ejecutar instrucciones
        int i = proceso.getBase();
        proceso.setPc(i);
        return proceso;
    }
    public void esperar(int tiempoInstr){
        try {
            Thread.sleep(tiempoInstr);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public boolean procesarResultado(String res,int CPU){
        switch(res){
            case "": return false;
            case "~Exit":
                return true;
                
            case "~Input": 
                manejarEntradaUsuario(CPU);
                return false;

            default:
                view.jTextArea1.append(res+"\n");
                return false;
        }
    }
    public void manejarEntradaUsuario(int CPU){
        this.view.jTextField1.selectAll();
        CountDownLatch latch = new CountDownLatch(1);
        final String[] dato = new String[1];
        this.view.jTextField1.addActionListener(e ->{
            dato[0] = this.view.jTextField1.getText();
            this.view.jTextField1.setText("");
            latch.countDown();
        });
        {
            try {
                latch.await();
                this.pc.movRegistro("DX", Integer.parseInt(dato[0]),pc.getCPU(CPU));
            } catch (InterruptedException ex) {
                System.getLogger(Controlador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }
    public void actualizarUI(BCP proceso,int indice,int id){
        pc.actualizarBCPDesdeCPU(proceso.getIdProceso(),proceso,pc.getCPU(proceso.getCPU()));
        pc.guardarBCPMemoria(proceso, indice);
        updateMemoria(proceso, indice);
        actualizarBCP(proceso,id);
        agregarFila(pc.getPila());
        System.out.println("pc"+ proceso.getPc());
    }
    public void finalizarProceso(BCP proceso,int indice,int rafagaEjecutada){
        if(rafagaEjecutada < proceso.getAlcance()){
            proceso.setEstado("espera");
        }else{
            proceso.setEstado("finalizado");
        }
    
        proceso.setTiempoFin(System.currentTimeMillis());
        proceso.setTiempoTotal(proceso.getTiempoFin() - proceso.getTiempoInicio());
        est.agregar(proceso.getIdProceso(), proceso.getTiempoTotal()); 
        updateBCP(proceso, indice);
        agregarEstadosTabla(proceso.getArchivos());
        System.out.println(pc.getBCP().toString());
     
        
    }
    /*
    ===========================OTRAS FUNCIONES==========================
    */
    public void estadosIniciales(){
        //System.out.println(pc.getPlanificador());
        for(int i=0;i<pc.getPlanificador().sizeProceso();i++){
               BCP proceso = pc.getPlanificador().obtenerProcesoIndice(i);
               String v1 = proceso.getEstado();
               String v2 = "P"+proceso.getIdProceso();
               String v3 = Integer.toString(proceso.getCPU());
               view.addFilaEstados(v2, v1, v3);  
              
        }
    }
    
    public void updateEstados(String valor1,String valor2){
        view.updateFilaEstados(valor1, valor2);
        
    }

    public void updateBCP(BCP proceso,int indice){
        pc.guardarBCPMemoria(proceso,indice);
        updateMemoria(proceso,indice);
        updateEstados("P"+proceso.getIdProceso(),proceso.getEstado());
            
    }
    public void tablaEjecucion(){
        for(int i=0;i<pc.getPlanificador().sizeProceso();i++){
               BCP proceso = pc.getPlanificador().obtenerProcesoIndice(i);
               String v2 = "P"+proceso.getIdProceso();
               view.addFilaProcesoD(v2);  
              
        }
        view.agregarColumnasHasta(getAlcanceTodo());
    }
     public int getAlcanceTodo(){
        int c =0;
        for(int i=0;i<pc.getPlanificador().sizeProceso();i++){
               BCP proceso = pc.getPlanificador().obtenerProcesoIndice(i);
               c+= proceso.getAlcance(); 
        }
        return c;
    }
    public String getEnlace(int n){
        String enlace = "";
        if(n < pc.getPlanificador().sizeProceso() ) {
            n++;
            enlace = "p"+n;
        }else{
            enlace = "-";
        }
        return enlace;
    }

    public void agregarEstadosTabla(List<String> archivos){
        int i=0;
        for(String arch:archivos){
            view.addFilaES(i,arch);
            i++;
        }
    }
 
    public void updateMemoria(String instr, int CPU){ 
        int star = pc.getCPU(CPU).getPC();
        view.addFilaMemoria(Integer.toString(star), instr);
        
    }

    public void updateMemoria(BCP bcp, int posicion){
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),"p"+bcp.getIdProceso());
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),bcp.getEstado());
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getPc()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getBase()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getAlcance()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getAc()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getAx()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getBx()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getCx()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Integer.toString(bcp.getDx()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),bcp.getIr());
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),bcp.getSiguiente());
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Long.toString(bcp.getTiempoInicio()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Long.toString(bcp.getTiempoFin()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++),Long.toString(bcp.getTiempoTotal()));
        view.updateFilaMemoria(posicion,Integer.toString(posicion++), bcp.getPila().toString());
        view.updateFilaMemoria(posicion,Integer.toString(posicion++), String.join(",", bcp.getArchivos()));
    }


    public void actualizarBCP(BCP bcp,int id){

        
        view.setlbIBX(Integer.toString(bcp.getRegistro(id,"BX")), bcp.getCPU());
        view.setlbIR(pc.binario(bcp.getIr()), bcp.getCPU());
        view.setlblAC(Integer.toString(bcp.getRegistro(id,"AC")), bcp.getCPU());
        view.setlblAX(Integer.toString(bcp.getRegistro(id,"AX")), bcp.getCPU());
        view.setlblCX(Integer.toString(bcp.getRegistro(id,"CX")), bcp.getCPU());
        view.setlblDX(Integer.toString(bcp.getRegistro(id,"DX")), bcp.getCPU());
        view.setlblPC(Integer.toString(bcp.getPc()), bcp.getCPU()); 
        
        view.setlbEnlace(bcp.getSiguiente(), bcp.getCPU());
        view.setlbCPU(bcp.getCpuAsig(), bcp.getCPU());
        view.setlbBase(Integer.toString(bcp.getBase()), bcp.getCPU());
        view.setlbAlcance(Integer.toString(bcp.getAlcance()), bcp.getCPU());
        view.setlblPrioridad(Integer.toString(bcp.getPrioridad()), bcp.getCPU());
    }
 
    public void buscarArchivo(){
        File[] archivos = seleccionarArchivos();
        if(archivos != null){
            for(File archivo: archivos){
                try{
                    List<String> lista = leerArchivo(archivo);
                    boolean valido =  true;
                    for (int i =0;valido && i<lista.size();i++){
                        valido &= validarLinea(lista.get(i));
                    }
                    if (valido){
                        pc.guardarInstrucciones(archivo.getName(),lista);
                        pc.getIntr();
                    }else{
                        JOptionPane.showMessageDialog(null, "Archivo con formato invalido");
                    }
                } catch(HeadlessException | IOException e){
                    JOptionPane.showMessageDialog(null, "No existe espacio suficiente en disco para cargar");
                }
            }
        }
        showDisk();
        inicializarSO();
        estadosIniciales();
        tablaEjecucion();
    }
    
    private void showDisk(){
        List<String> disco= pc.getDataDisk();
        DefaultTableModel modelo = (DefaultTableModel) this.view.jTable3.getModel();
        modelo.setRowCount(0);
        for(int i = 0;i<disco.size();i++){
            modelo.addRow(new Object[]{i, disco.get(i)});
        }
    }
    public void showRam(){
        view.getModelMemory().setRowCount(0);
        int sizeMemoria = (Integer) view.getSpnMemoria().getValue();

        guardarRam(0,pc.getEspacioSO(sizeMemoria),"<so>");
        int inicio = pc.getEspacioSO(sizeMemoria);
        System.out.println("i"+inicio+" "+sizeMemoria);
        guardarRam(inicio,sizeMemoria,"<user>");
        guardarRam(sizeMemoria,sizeMemoria+64,"<virtual>");
        actualizarMemoria();
    }
    public void guardarRam(int inicio,int rango, String val){
        
        for(int i =inicio;i<rango;i++){
            view.addFilaMemoria(Integer.toString(i), val);
        }
    }
    

    public void spinnerTamano(){
        try {
            this.pc.tamannoDisco((int) this.view.getSpnDisco().getValue());
        } catch (IOException ex) {
            try {
                this.view.getSpnDisco().setValue(this.pc.getDisco().size());
            } catch (IOException ex1) {
                System.getLogger(Controlador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex1);
            }
            JOptionPane.showMessageDialog(null, "No se puede reducir espacio utilizado ");
        }
        showDisk();
    }
    public void clean(){
        view.getModelProgram().setRowCount(0);
        view.getModelProcesos().setRowCount(0);
        view.getModelMemory().setRowCount(0);
        view.getModelPila().setRowCount(0);
        view.getModelArchivos().setRowCount(0);
        view.setlbIBX("---",0);
        view.setlbIR("---",0);
        view.setlblAC("---",0);
        view.setlblAX("---",0);
        view.setlblCX("---",0);
        view.setlblDX("---",0);
        view.setlblPC("---",0);
        
        view.setlbEnlace("---",0);
        view.setlbCPU("---",0);
        view.setlbBase("---",0);
        view.setlbAlcance("---",0);
        view.setlblPrioridad("---",0);
        
        view.setlbIBX("---",1);
        view.setlbIR("---",1);
        view.setlblAC("---",1);
        view.setlblAX("---",1);
        view.setlblCX("---",1);
        view.setlblDX("---",1);
        view.setlblPC("---",1);
        
        view.setlbEnlace("---",1);
        view.setlbCPU("---",1);
        view.setlbBase("---",1);
        view.setlbAlcance("---",1);
        view.setlblPrioridad("---",1);
        
        pc = new SistemaOperativo();
        estadistica = new Estadistica();
        est = new Estadisticas();
        pc.tamannoMemoria(512);
        
        showDisk();
        showRam();
        indiceArch=0;
        inicializarSO();
        contEjecucion=0;
        modoPasoPaso = false;
        hiloIniciado = false;
        estadosIniciales();
        view.getModelDiagram().setRowCount(0);
        tablaEjecucion();
    }
    
    public void cleanAll(){
        pc.ClearDisk();
        clean();
        JOptionPane.showMessageDialog(null, "Sistema limpiado correctamente");
    }    

    private void mostrarEstadistica(){
       // view.dispose(); cierro ventana principal
       
        estadistica.setVisible(true);
        estadistica.mostrarGraficoBarras(est.getRegistros());
    }

    private void volverEst(){
        estadistica.setVisible(false);
    }
       
    public void agregarFila(Stack<Integer>pila){
        view.cleanPila();
        int i=0;
        for(Integer val :pila){
            view.addFilaFila(i,Integer.toString(val));
            i++;
        }
    }
    

    

}
