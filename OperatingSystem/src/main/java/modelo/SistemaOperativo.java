/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.IOException;
import java.util.*;
import javax.swing.JOptionPane;


public class SistemaOperativo {
    private BCP bcp;
    private CPU cpu;
    private Disco disco;
    private Memoria memoria;
    private Planificador plan;
    private List<String> instrucciones;
    private Queue<BCP> colaProcesos; //FIFO
    private boolean cmpBandera = false;
    private Stack<Integer> pila = new Stack<>();
    
  
    public SistemaOperativo(){
        bcp = new BCP();
        cpu = new CPU();
        instrucciones = new ArrayList<>();
        colaProcesos = new LinkedList<>(); 
        plan = new Planificador();
        try {
            this.disco = new Disco("Disco.txt");
            instrucciones = disco.getDatos();
        } catch (IOException ex) {
            System.getLogger(SistemaOperativo.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    public void tamannoMemoria(int sizeMemoria) {
        this.memoria = new Memoria(sizeMemoria);
}

    public void tamannoDisco(int sizeDisco) throws IOException {
        this.disco.cambiarTamano(sizeDisco);
    }
    public void guardarInstrucciones(String nombreArchivo,List<String> lista) throws IOException{
        disco.crearArchivo(nombreArchivo, lista);
        instrucciones = disco.getDatos();
    }
    
    public void ClearDisk(){
        disco.ClearAll();
    }
    
    public List<String> getDataDisk(){
        try {
            return disco.leerTodo();
        } catch (IOException ex) {
            System.getLogger(SistemaOperativo.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }

    public List<String> getIntr(){
        System.out.println(instrucciones);
        return instrucciones;
    }   

    public void inicializarSO(int tamanno){
        cpu.reset();
        cpu.setPC(0);
        int base = getEspacioSO(tamanno);
        bcp.setPc(base);
    }

    public void cargarSO(String instr){
        int pos = cpu.getPC();
        try {
            if(pos <= disco.size()){
                disco.setDisco(pos,instr);
                pos++;
            } else{
                JOptionPane.showConfirmDialog(null, "Error al leer el archivo" );   
            }
        } catch (IOException ex) {
            JOptionPane.showConfirmDialog(null, "Error al leer el archivo" );
        }
    }
    
    public void movRegistro(String registro,int valor){
        switch(registro.replace(",", "").toLowerCase()){
            case "ax":cpu.setAX(valor);break;
            case "bx":cpu.setBX(valor);break;
            case "cx":cpu.setCX(valor);break;
            case "dx":cpu.setDX(valor);break;
            
        }
    }

    public int getRegistro(String registro){
        switch(registro.replace(",", "").toLowerCase()){
            case "ax": return cpu.getAX();
            case "bx": return cpu.getBX();
            case "cx": return cpu.getCX();
            case "dx": return cpu.getDX();
            
        }
        return 0;
    }

    public String interprete(String instr, BCP proceso){
        String[] partes = instr.split(" ");
        String op = partes[0].toLowerCase();
        switch(op){
            case "load":
                cpu.setAC(getRegistro(partes[1]));
                break;
            case "store":
                movRegistro(partes[1],cpu.getAC());
                break;
            case "mov":
                switch (partes[2].toLowerCase()){
                    case "ax":
                    case "bx":
                    case "cx":
                    case "dx":
                        movRegistro(partes[1],getRegistro(partes[2]));
                        break;
                    default:
                        movRegistro(partes[1],Integer.parseInt(partes[2]));
                }
                break;
            case "sub":
                cpu.setAC(cpu.getAC()-getRegistro(partes[1]));
                break;
            case "add":
                cpu.setAC(cpu.getAC()+getRegistro(partes[1]));
                break;
            case "inc":
                if(partes.length == 1){
                    cpu.setAC(cpu.getAC()+1);
                }else{
                    switch(partes[1].toLowerCase()){
                    case "ax": cpu.setAX(cpu.getAX()+1); break;
                    case "bx": cpu.setBX(cpu.getBX()+1); break; 
                    case "cx": cpu.setCX(cpu.getCX()+1); break;
                    case "dx": cpu.setDX(cpu.getDX()+1);break; 
                    }
                }
                break;
            case "dec":
                if(partes.length == 1){
                    cpu.setAC(cpu.getAC()-1);
                }else{
                    switch(partes[1].toLowerCase()){
                    case "ax": cpu.setAX(cpu.getAX()-1); break;
                    case "bx": cpu.setBX(cpu.getBX()-1); break; 
                    case "cx": cpu.setCX(cpu.getCX()-1); break;
                    case "dx": cpu.setDX(cpu.getDX()-1);break; 
                    }
                }
                break;
            case "swap":
                int temp = getRegistro(partes[1]);
                movRegistro(partes[1],getRegistro(partes[2]));
                movRegistro(partes[2], temp);
                break;
            case "int":
                switch(partes[1].toLowerCase()){
                    case "20h":
                        return "~Exit";
                    case "10h":
                        return Integer.toString(cpu.getDX());
                    case "09h":
                        return "~Input";
                    case "21h":
                        break;
                }
                break;
            case "jmp":
                int jmp = proceso.getPc()+ Integer.parseInt(partes[1]);
                cpu.setPC((jmp >= 0)? jmp:0);
                break;
            case "cmp":
                cmpBandera = getRegistro(partes[1]) == getRegistro(partes[2]);
                break;
            case "je":
                if (cmpBandera){
                    int je = proceso.getPc()+ Integer.parseInt(partes[1]);
                    cpu.setPC((je >= 0)? je:0);
                }
                break;
            case "jne":
                if (!cmpBandera){
                    int jne = proceso.getPc()+ Integer.parseInt(partes[1]);
                    cpu.setPC((jne >= 0)? jne:0);
                }
                break;
            case "param":
                for (int i= 1; i<partes.length;i++){
                    pila.push(Integer.parseInt(partes[i].replace(",","")));
                }
                break;
            case "push":
                Stack u = pila;
                u.push((Integer)getRegistro(partes[1]));
                break;
            case "pop":
                int pop = pila.pop();
                switch(partes[1].toLowerCase()){
                case "ax": cpu.setAX(pop); break;
                case "bx": cpu.setBX(pop); break; 
                case "cx": cpu.setCX(pop); break;
                case "dx": cpu.setDX(pop); break; 
                }
                break;
        }
        return "";
    }
    public int getValue(String val){
        int time =0;
        switch(val){
            case "20h":
                time= 2000;
                break;
            case "10h":
                time= 2000;
                break;
            case "9h":
                time= 500; //tiempo int - entrada de usuario
                break;
            case "21h":
                time= 5000;
                break;
        }
        return time;
    }
    public int getTimer(String instr){
        String[] partes = instr.split(" ");
        String op = partes[0].toLowerCase();
        String val = "0";
        if (partes.length >1) {val = partes[1].toLowerCase();}
        int time =0;
        switch(op){
            case "load":
                time= 2000;
                break;
            case "store":
                time= 2000;
                break;
            case "mov":
                time= 1000;
                break;
            case "sub":
                time= 3000;
                break;
            case "add":
                time= 3000;
                break;
            case "inc":
                time= 1000;
                break;
            case "dec":
                time= 1000;
                break;    
            case "swap":
                time= 2000;
                break;  
            case "int":
                time= getValue(val);
                break;
            case "jmp":
                time = 2000;
                break;
            case "cmp":
                time = 1000;
                break;
            case "jne":
                time = 2000;
                break;
            case "je":
                time = 2000;
                break;
            case "param":
                time = 3000;
                break;
            case "push":
                time = 1000;
                break;
            case "pop":
                time = 1000;
                break;
        }
        return time;
    }

    public String binario (String instr){

        String[] partes = instr.split(" ");
        String op = partes[0].toLowerCase();
        String str = "";
        switch(op){
            case "load": str = "0001"; break;
            case "store": str = "0010";  break; //0110?
            case "mov": str = "0011"; break;
            case "sub": str = "0100";break; //0111?
            case "add": str = "0101";break;
            
            case "inc": str = "0110"; break;
            case "dec": str = "0111";break;
            case "swap": str = "1000";break;
            case "int": str = "1001";break;
            case "jmp": str = "1010";break;
            case "cmp": str = "1011";break;
            case "je": str = "1100";break;
            case "jne": str = "1101";break;
            case "param": str = "1110";break;
            case "push": str = "1111";break;
            case "pop": str = "0000";break;
        }
        if ("int".equals(op)){
            int val = Integer.parseInt(partes[1].toLowerCase().replace("h", ""));
            String valBin = String.format("%08d", Integer.parseInt(Integer.toBinaryString(val & 0xFF)));
            str += " " + valBin;
        }else if ("param".equals(op)){
            for(int i = 1; i < partes.length; i++){
                int val = Integer.parseInt(partes[1].toLowerCase().replace(",", ""));
                String valBin = String.format("%08d", Integer.parseInt(Integer.toBinaryString(val & 0xFF)));
                str += " " + valBin;
            }
        }else{
            if (partes.length >1){
                String reg = partes[1].replace(",", "").toLowerCase();
                switch(reg){
                    case "ax": str += " 0001"; break;
                    case "bx": str += " 0010"; break; 
                    case "cx": str += " 0011"; break;
                    case "dx": str += " 0100";break; 
                    }
                if(instr.contains(",")){
                    switch(partes[2].toLowerCase()){
                        case "ax": str += " 0001"; break;
                        case "bx": str += " 0010";  break; 
                        case "cx": str += " 0011"; break;
                        case "dx": str += " 0100";break;
                        default:
                            int val = Integer.parseInt(partes[2]);
                            String valBin = String.format("%08d", Integer.parseInt(Integer.toBinaryString(val & 0xFF)));
                            str += " " + valBin;
                    }
                }
            }
        }
        return str;
        
    }
    //bcp
    public void actualizarBCPDesdeCPU(BCP bcp) {
        bcp.setAc(cpu.getAC());
        bcp.setAx(cpu.getAX());
        bcp.setBx(cpu.getBX());
        bcp.setCx(cpu.getCX());
        bcp.setDx(cpu.getDX());
        bcp.setIr(cpu.getIR());
    }
    

    public void crearProcesos(){
        int contProceso=0;
        List<String> archAcc = new ArrayList<>();
        for(int i=0;i<getIntr().size();i++){
            String instru = disco.getDisco(i);
            if(instru.contains("|")){
                String[] partes = instru.split("\\|");
                String nombreArchivo = partes[0];
                
                int base = Integer.parseInt(partes[1]);
                int alcance = Integer.parseInt(partes[2]);
                String estado ;
                if(i<5){estado="nuevo";}
                else {estado = "espera";}
                archAcc.add(nombreArchivo);
                BCP bcp = new BCP(contProceso++,estado,i+1,base,alcance);
                bcp.getArchivos().addAll(archAcc);
                plan.agregarProceso(bcp);
                
            }
        } 
    }
    public void guardarBCPMemoria(BCP bcp, int posicion){
        memoria.setMemoria(posicion++,"p"+bcp.getIdProceso());
        memoria.setMemoria(posicion++,bcp.getEstado());
        memoria.setMemoria(posicion++,Integer.toString(bcp.getPc()));
        memoria.setMemoria(posicion++,Integer.toString(bcp.getBase()));
        memoria.setMemoria(posicion++,Integer.toString(bcp.getAlcance()));
        memoria.setMemoria(posicion++,Integer.toString(bcp.getAc()));
        memoria.setMemoria(posicion++,Integer.toString(bcp.getAx()));
        memoria.setMemoria(posicion++,Integer.toString(bcp.getBx()));
        memoria.setMemoria(posicion++,Integer.toString(bcp.getCx()));
        memoria.setMemoria(posicion++,Integer.toString(bcp.getDx()));
        memoria.setMemoria(posicion++,bcp.getIr());
        memoria.setMemoria(posicion++,Long.toString(bcp.getTiempoInicio()));
        memoria.setMemoria(posicion++,Long.toString(bcp.getTiempoFin()));
        memoria.setMemoria(posicion++,Long.toString(bcp.getTiempoTotal()));
        memoria.setMemoria(posicion++, bcp.getPila().toString());
        memoria.setMemoria(posicion++, String.join(",", bcp.getArchivos()));
    }
    
    public int numProcesos(){
        List<String> lista = getIntr();
        int numProceso = 0;
        for(String i:lista){
            if(i.contains("|")){
                numProceso++;
            }
        }
        return numProceso;   
    }
    
    public int getEspacioSO(int totalMemoria){
        int espacioSO = Math.max(20, totalMemoria/5);
        return espacioSO;
    }
    

    public void configurarMemoria(int totalMemoria, int numprocs){
        int espacioSO = Math.max(20, totalMemoria/5);
        int espacioUsuario = totalMemoria - espacioSO;
        
        int espacioBCP = 16*numProcesos();
        if(espacioUsuario<espacioBCP){
            System.out.println("Error:No hay suficiente espacio para "+numProcesos()+"procesos");
        }
    }
    
    
    public CPU getCPU() {return cpu;}
    public Disco getDisco() {return disco;}
    public Planificador getPlanificador() {return plan;}
    public BCP getBCP() {return bcp;}
    public Memoria getMemoria(){return memoria;}
    public Stack getPila(){return pila;}
}
