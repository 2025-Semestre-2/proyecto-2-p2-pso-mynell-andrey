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
    //var glob para paso a paso
    private final Object lock = new Object();
    private boolean modoPasoPaso = false;
    private boolean hiloIniciado = false;
    private int cpuCounter = 0;



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
                GanttSimpleNonPreemptive(ordenarProcesos);
                break;
            
            case "SJF": 
                System.out.println("spn"+ordenarProcesosSJF(view.getProcesosTabla()));
                ordenarProcesos = ordenarProcesosSJF(view.getProcesosTabla());
                GanttSimpleNonPreemptive(ordenarProcesos);
                ejecutarAlgoritmo(ordenarProcesos);
                break;
            case "SRT": 
        
                System.out.println("srt"+ajustarSalidaSRT(ordenarProcesosSRT(view.getProcesosTabla())));
                LinkedHashMap<String,ArrayList<Integer>> temp =ordenarProcesosSRT(view.getProcesosTabla());
                ordenarProcesos = ajustarSalidaSRT(temp);
                GanttSimple(temp);
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
                GanttSimple(ordenarProcesos);
                ejecutarAlgoritmo(ordenarProcesos);
                break;
            case "HRRN": 
                System.out.println("hrrn"+ordenarProcesosSJF(view.getProcesosTabla()));
                ordenarProcesos = ordenarProcesosHRRN(view.getProcesosTabla());
                GanttSimple(ordenarProcesos);
                ejecutarAlgoritmo(ordenarProcesos);
                break;
            case "CFS": 
                System.out.println("cfs"+ordenarProcesosSJF(view.getProcesosTabla()));
                ordenarProcesos = ordenarProcesosCFS(view.getProcesosTabla());
                GanttSimple(ordenarProcesos);
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
                        pc.getCPU(cpuCounter%2).setIR(instr);
                        this.view.jTable3.changeSelection(i, i, false, false);
                        String res = pc.interprete(instr, proceso,pc.getCPU(cpuCounter%2));
                        i = proceso.getPc();
                        //entrada usuario
                        stop = procesarResultado(res);
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
                cpuCounter++;
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
    public boolean procesarResultado(String res){
        switch(res){
            case "": return false;
            case "~Exit":
                return true;
                
            case "~Input": 
                manejarEntradaUsuario();
                return false;

            default:
                view.jTextArea1.append(res+"\n");
                return false;
        }
    }
    public void manejarEntradaUsuario(){
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
                this.pc.movRegistro("DX", Integer.parseInt(dato[0]),pc.getCPU(cpuCounter%2));
            } catch (InterruptedException ex) {
                System.getLogger(Controlador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }
    public void actualizarUI(BCP proceso,int indice,int id){
        pc.actualizarBCPDesdeCPU(proceso.getIdProceso(),proceso,pc.getCPU(cpuCounter%2));
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
    public void updateEstados(String valor1,String valor2){
        view.addFilaEstados(valor1, valor2);
        
    }

    public void updateBCP(BCP proceso,int indice){
        pc.guardarBCPMemoria(proceso,indice);
        updateMemoria(proceso,indice);
        updateEstados(Integer.toString(proceso.getIdProceso()),proceso.getEstado());
            
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
 
    public void updateMemoria(String instr){ 
        int star = pc.getCPU(cpuCounter%2).getPC();
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

        view.setlbIBX(Integer.toString(bcp.getRegistro(id,"BX")));
        view.setlbIR(pc.binario(bcp.getIr()));
        view.setlblAC(Integer.toString(bcp.getRegistro(id,"AC")));
        view.setlblAX(Integer.toString(bcp.getRegistro(id,"AX")));
        view.setlblCX(Integer.toString(bcp.getRegistro(id,"CX")));
        view.setlblDX(Integer.toString(bcp.getRegistro(id,"DX")));
        view.setlblPC(Integer.toString(bcp.getPc())); 
        
        view.setlbEnlace(bcp.getSiguiente());
        view.setlbCPU(bcp.getCpuAsig());
        view.setlbBase(Integer.toString(bcp.getBase()));
        view.setlbAlcance(Integer.toString(bcp.getAlcance()));
        view.setlblPrioridad(Integer.toString(bcp.getPrioridad()));
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
        view.setlbIBX("---");
        view.setlbIR("---");
        view.setlblAC("---");
        view.setlblAX("---");
        view.setlblCX("---");
        view.setlblDX("---");
        view.setlblPC("---");
        
        view.setlbEnlace("---");
        view.setlbCPU("---");
        view.setlbBase("---");
        view.setlbAlcance("---");
        view.setlblPrioridad("---");
        
        pc = new SistemaOperativo();
        estadistica = new Estadistica();
        est = new Estadisticas();
        pc.tamannoMemoria(512);
        
        showDisk();
        showRam();
        indiceArch=0;
        inicializarSO();
        modoPasoPaso = false;
        hiloIniciado = false;
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
    
    /*
    ===========================DIAGRAMADO==========================
    */
    
    public void GanttSimple(HashMap<String, ArrayList<Integer>> mapaOrdenado) {
        // Calcular tiempo total según la mayor columna de tiempo
        int totalTiempo = 0;
        for (Map.Entry<String, ArrayList<Integer>> entry : mapaOrdenado.entrySet()) {
            int inicio = Integer.parseInt(entry.getKey().split("_")[1]);
            int duracion = entry.getValue().get(1);
            totalTiempo = Math.max(totalTiempo, inicio + duracion);
        }

        // Crear encabezados de columnas
        String[] columnas = new String[totalTiempo + 1];
        columnas[0] = "Proceso";
        for (int i = 1; i <= totalTiempo; i++) {
            columnas[i] = String.valueOf(i - 1);
        }

        // Obtener modelo y limpiarlo
        DefaultTableModel model = this.view.getModelDiagram();
        model.setRowCount(0);
        model.setColumnCount(0);

        for (String col : columnas) {
            model.addColumn(col);
        }

        // Agrupar ejecuciones por proceso
        Map<String, List<int[]>> ejecucionesPorProceso = new LinkedHashMap<>();
        for (Map.Entry<String, ArrayList<Integer>> entry : mapaOrdenado.entrySet()) {
            String nombre = entry.getKey().split("_")[0];
            int inicio = Integer.parseInt(entry.getKey().split("_")[1]);
            int duracion = entry.getValue().get(1);

            ejecucionesPorProceso.putIfAbsent(nombre, new ArrayList<>());
            ejecucionesPorProceso.get(nombre).add(new int[]{inicio, duracion});
        }

        // Llenar filas
        for (Map.Entry<String, List<int[]>> entry : ejecucionesPorProceso.entrySet()) {
            String nombre = entry.getKey();
            Object[] fila = new Object[totalTiempo + 1];
            fila[0] = nombre;

            // Pintar bloques según sus ejecuciones
            for (int[] ejec : entry.getValue()) {
                int inicio = ejec[0];
                int duracion = ejec[1];
                for (int t = inicio; t < inicio + duracion; t++) {
                    fila[t + 1] = "██";
                }
            }

            model.addRow(fila);
        }

        // Configurar JTable
        JTable tabla = this.view.jTable26;
        tabla.setModel(model);
        tabla.setRowHeight(20);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(100);
        for (int i = 1; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(30);
        }
    }
    
    public void GanttSimpleNonPreemptive(HashMap<String, ArrayList<Integer>> mapaOrdenado) {
        // Calcular tiempo total considerando llegadas y ráfagas
        int tiempoActual = 0;
        int totalTiempo = 0;
        for (Map.Entry<String, ArrayList<Integer>> entry : mapaOrdenado.entrySet()) {
            int llegada = entry.getValue().get(0);
            int rafaga = entry.getValue().get(1);
            tiempoActual = Math.max(tiempoActual, llegada) + rafaga;
        }
        totalTiempo = tiempoActual;

        // Crear encabezados de columnas
        String[] columnas = new String[totalTiempo + 1];
        columnas[0] = "Proceso";
        for (int i = 1; i <= totalTiempo; i++) {
            columnas[i] = String.valueOf(i - 1);
        }

        // Limpiar y configurar modelo
        DefaultTableModel model = this.view.getModelDiagram();
        model.setRowCount(0);
        model.setColumnCount(0);
        for (String col : columnas) {
            model.addColumn(col);
        }

        // Dibujar cada proceso
        tiempoActual = 0;
        for (Map.Entry<String, ArrayList<Integer>> entry : mapaOrdenado.entrySet()) {
            String nombre = entry.getKey();
            int llegada = entry.getValue().get(0);
            int rafaga = entry.getValue().get(1);

            // El proceso empieza cuando llega o cuando termina el anterior
            int inicio = Math.max(tiempoActual, llegada);

            Object[] fila = new Object[totalTiempo + 1];
            fila[0] = nombre;

            for (int t = inicio; t < inicio + rafaga; t++) {
                fila[t + 1] = "██";
            }

            model.addRow(fila);
            tiempoActual = inicio + rafaga;
        }

        // Configurar JTable
        JTable tabla = this.view.jTable26;
        tabla.setModel(model);
        tabla.setRowHeight(20);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(100);
        for (int i = 1; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(30);
        }
    }

}
