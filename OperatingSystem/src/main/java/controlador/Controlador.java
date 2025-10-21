package controlador;

import static controlador.Utilidades.leerArchivo;
import static controlador.Utilidades.seleccionarArchivos;
import java.awt.HeadlessException;
import modelo.SistemaOperativo;

import vista.View;

import vista.Estadistica;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
    private int contador =0;
    private boolean inicializado = false;
    private BCP procesoActual = null;
    private int pos = 0;
    

    public Controlador(SistemaOperativo pc, View view,Estadistica estadistica ) {
        this.pc = pc;
        this.view = view;
        this.est = new Estadisticas();
  
        this.estadistica = estadistica;
        this.view.btnBuscarListener(e -> buscarArchivo());
        this.view.btnEjecutar(e -> ejecutarSO());
        this.view.btnLimpiar(e -> cleanAll());
        this.view.btnReset(e -> clean());
        this.view.btnPasoListener(e -> ejecutarPasoPaso());
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
    }
    public void ejecutarSO(){
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
        
        //guardarEnDisco();
        pc.crearProcesos();
        
        planificadorTrabajos();
        
    }
    
    public void showRam(){
        view.getModelMemory().setRowCount(0);
        int sizeMemoria = (Integer) view.getSpnMemoria().getValue();

        guardarRam(0,pc.getEspacioSO(sizeMemoria),"<so>");
        int inicio = pc.getEspacioSO(sizeMemoria);
        System.out.println("i"+inicio+" "+sizeMemoria);
        guardarRam(inicio,sizeMemoria,"<user>");
        guardarRam(sizeMemoria,sizeMemoria+64,"<virtual>");
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
        
    public void planificadorTrabajos() {
        new Thread(() -> {   
            int cpu = 0;
            int indice = pc.getBCP().getPc();
            int next=0;
            while (pc.getPlanificador().sizeCola() > 0) {

                // tomar el proceso de la cola
                BCP proceso = pc.getPlanificador().obeterSiguienteProceso();
                if(cpu>5){
                    proceso.setEstado("nuevo");
                    proceso.setCpuAsig("hilo"+cpu);
                    cpu =1;
                }
                proceso.setCpuAsig("hilo"+cpu);
                String enlace = getEnlace(next);
                proceso.setSiguiente(enlace);
                preparadoBCP(proceso, indice);//este para añadir el nuevo a los estados
                // pasar a preparado
                proceso.setEstado("preparado");
                updateBCP(proceso, indice);

                // pasar a ejecucion
                proceso.setEstado("ejecucion");
                proceso.setTiempoInicio(System.currentTimeMillis());
                updateBCP(proceso, indice);
                boolean stop = false;
                // ejecutar instrucciones
                int i = proceso.getBase();
                proceso.setPc(i);
                while (i < proceso.getBase() + proceso.getAlcance()) {
                    String instr = pc.getDisco().getDisco(i);
                    if (instr != null) {
                        pc.getCPU().setIR(instr);
                        this.view.jTable3.changeSelection(i, i, false, false);
                        String res = pc.interprete(instr, proceso);
                        i = proceso.getPc();
                        switch(res){
                            case "": break;
                            case "~Exit":
                                stop = true;
                                break;
                            case "~Input": 
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
                                        this.pc.movRegistro("DX", Integer.parseInt(dato[0]));
                                    } catch (InterruptedException ex) {
                                        System.getLogger(Controlador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                                    }
                                }
                                break;

                            default:
                                view.jTextArea1.append(res+"\n");
                                break;
                        }
                        if (stop) break;
                    

                        int tiempoInstr = pc.getTimer(instr);
                        try {
                            Thread.sleep(tiempoInstr);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        // actualizar UI de memoria y BCP en el hilo de Swing
                        pc.actualizarBCPDesdeCPU(proceso);
                        pc.guardarBCPMemoria(proceso, indice);
                        updateMemoria(proceso, indice);
                        actualizarBCP(proceso);
                        agregarFila(pc.getPila());
                        System.out.println("pc"+ proceso.getPc());
                   
                    }
                }
                if (stop) break;
                // finalizar proceso
                proceso.setEstado("finalizado");
                proceso.setTiempoFin(System.currentTimeMillis());
                proceso.setTiempoTotal(proceso.getTiempoFin() - proceso.getTiempoInicio());
                est.agregar(proceso.getIdProceso(), proceso.getTiempoTotal()); 

                updateBCP(proceso, indice);
                agregarEstadosTabla(proceso.getArchivos());

                indice += 16;
                cpu++;
                next++;

            }
        }).start(); 

    }
    public void ejecutarPasoPaso(){
        if(!inicializado){
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

            //guardarEnDisco();
            pc.crearProcesos();
          
            inicializado=true;
            pos =pc.getBCP().getPc();
            
        }
        
        planificadorTrabajosPasoPaso();
        
    }
    public void planificadorTrabajosPasoPaso() {
        int cpu=0;
        int next=0;
        if(procesoActual==null){
            if(pc.getPlanificador().sizeCola() == 0){
                JOptionPane.showMessageDialog(null, "Error: No hay procesos en cola");
                return;
            }
            
            // tomar el proceso de la cola
            procesoActual = pc.getPlanificador().obeterSiguienteProceso();
            if(cpu>5){
                    procesoActual.setEstado("nuevo");
                    procesoActual.setCpuAsig("hilo"+cpu);
                    cpu =1;
                }
            procesoActual.setCpuAsig("hilo"+cpu);
            String enlace = getEnlace(next);
            procesoActual.setSiguiente(enlace);
            preparadoBCP(procesoActual, pos);//este para añadir el nuevo a los estados
            // pasar a preparado
            procesoActual.setEstado("preparado");
            
            preparadoBCP(procesoActual, pos);

            // pasar a ejecucion
            procesoActual.setEstado("ejecucion");
            procesoActual.setTiempoInicio(System.currentTimeMillis());
            updateBCP(procesoActual, pos);
        }
        

        // ejecutar instrucciones}
        int posIntr = procesoActual.getBase()+contador;
        if(contador < procesoActual.getAlcance()) {
            String instr = pc.getDisco().getDisco(posIntr);
            if (instr != null) {
                pc.getCPU().setIR(instr);
                this.view.jTable3.changeSelection(posIntr, posIntr, false, false);
                String res = pc.interprete(instr, procesoActual);
                switch(res){
                    case "": break;
                    case "~Exit": 
                        
                        break;
                    case "~Input": 
                        this.view.jTextField1.selectAll();
                        new Thread(() -> {
                            CountDownLatch latch = new CountDownLatch(1);
                            final String[] dato = new String[1];
                            this.view.jTextField1.addActionListener(e ->{
                                dato[0] = this.view.jTextField1.getText();
                                this.view.jTextField1.setText("");
                                latch.countDown();
                            });
                            {
                                try {
                                    this.view.btnPaso.setEnabled(false);
                                    latch.await();
                                    this.pc.movRegistro("DX", Integer.parseInt(dato[0]));
                                    this.view.btnPaso.setEnabled(true);
                                } catch (InterruptedException ex) {
                                    System.getLogger(Controlador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                                }
                            }
                        }).start();
                        break;

                    default:
                        view.jTextArea1.append(res+"\n");
                        break;
                }
                procesoActual.setPc(posIntr + 1);

                // actualizar UI de memoria y BCP en el hilo de Swing
                pc.actualizarBCPDesdeCPU(procesoActual);
                pc.guardarBCPMemoria(procesoActual, pos);
                updateMemoria(procesoActual, pos);
                actualizarBCP(procesoActual);
                agregarFila(pc.getPila());

            }
            contador++;
            next++;
            
        }
        else{
            // finalizar proceso
          
            procesoActual.setEstado("finalizado");
            procesoActual.setTiempoFin(System.currentTimeMillis());
            procesoActual.setTiempoTotal(procesoActual.getTiempoFin() - procesoActual.getTiempoInicio());
            est.agregar(procesoActual.getIdProceso(), procesoActual.getTiempoTotal()); 

            updateBCP(procesoActual, pos);
            agregarEstadosTabla(procesoActual.getArchivos());
         
            pos += 16; //indice
            contador =0; // contador
            procesoActual=null;
            cpu++;


        }
    }

    public void updateBCP(BCP proceso,int indice){
        pc.guardarBCPMemoria(proceso,indice);
        updateMemoria(proceso,indice);
        updateEstados(Integer.toString(proceso.getIdProceso()),proceso.getEstado());
            
    }
    public String getEnlace(int n){
        String enlace = "";
        if(n < pc.getPlanificador().sizeCola() ) {
            n++;
            enlace = "p"+n;
        }else{
            enlace = "-";
        }
        return enlace;
    }
    public void preparadoBCP(BCP proceso,int indice){
        pc.guardarBCPMemoria(proceso,indice);
        addMemoria(proceso,indice);
        updateEstados(Integer.toString(proceso.getIdProceso()),proceso.getEstado());
            
    }
    public void agregarEstadosTabla(List<String> archivos){
        int i=0;
        for(String arch:archivos){
            view.addFilaES(i,arch);
            i++;
        }
    }

    public void updateEstados(String valor1,String valor2){
        view.addFilaEstados(valor1, valor2);
        
    }
 
    public void updateMemoria(String instr){ 
        int star = pc.getCPU().getPC();
        view.addFilaMemoria(Integer.toString(star), instr);
        
    }
    public void addMemoria(BCP bcp, int posicion){
        view.addFilaMemoria(Integer.toString(posicion++),"p"+bcp.getIdProceso());
        view.addFilaMemoria(Integer.toString(posicion++),bcp.getEstado());
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getPc()));
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getBase()));
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getAlcance()));
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getAc()));
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getAx()));
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getBx()));
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getCx()));
        view.addFilaMemoria(Integer.toString(posicion++),Integer.toString(bcp.getDx()));
        view.addFilaMemoria(Integer.toString(posicion++),bcp.getIr());
        view.addFilaMemoria(Integer.toString(posicion++),bcp.getSiguiente());
        view.addFilaMemoria(Integer.toString(posicion++),Long.toString(bcp.getTiempoInicio()));
        view.addFilaMemoria(Integer.toString(posicion++),Long.toString(bcp.getTiempoFin()));
        view.addFilaMemoria(Integer.toString(posicion++),Long.toString(bcp.getTiempoTotal()));
        view.addFilaMemoria(Integer.toString(posicion++), bcp.getPila().toString());
        view.addFilaMemoria(Integer.toString(posicion++), String.join(",", bcp.getArchivos()));
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


    public void actualizarBCP(BCP bcp){
        view.setlbIBX(Integer.toString(bcp.getBx()));
        view.setlbIR(pc.binario(bcp.getIr()));
        view.setlblAC(Integer.toString(bcp.getAc()));
        view.setlblAX(Integer.toString(bcp.getAx()));
        view.setlblCX(Integer.toString(bcp.getCx()));
        view.setlblDX(Integer.toString(bcp.getDx()));
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
                    pc.guardarInstrucciones(archivo.getName(),lista);
                    pc.getIntr();
                } catch(HeadlessException | IOException e){
                    JOptionPane.showMessageDialog(null, "No existe espacio suficiente en disco para cargar");
                }
            }
        }
        showDisk();
    }
    
    private void showDisk(){
        List<String> disco= pc.getDataDisk();
        DefaultTableModel modelo = (DefaultTableModel) this.view.jTable3.getModel();
        modelo.setRowCount(0);
        for(int i = 0;i<disco.size();i++){
            modelo.addRow(new Object[]{i, disco.get(i)});
        }
    }

    public void clean(){
        view.getModelProgram().setRowCount(0);
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
        contador =0;
        inicializado = false;
        procesoActual = null;
        pos = 0;
        pc.tamannoMemoria(512);
        
        showDisk();
        showRam();
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
