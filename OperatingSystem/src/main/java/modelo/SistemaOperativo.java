/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.IOException;
import java.util.*;
import javax.swing.JOptionPane;
import static modelo.Memoria.particionesFija;
import controlador.Particion;


public class SistemaOperativo {
    private BCP bcp;
    private List<CPU> cpu;
    private Disco disco;
    private Memoria memoria;
    private Planificador plan;
    private List<String> instrucciones;
    private int cpuCounter = 0;
  
    private boolean cmpBandera = false;
    private Stack<Integer> pila = new Stack<>();
    
  
    public SistemaOperativo(){
        cpu = new ArrayList<>();
        bcp = new BCP();
        cpu.add(new CPU());
        cpu.add(new CPU());
        instrucciones = new ArrayList<>();

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
        //System.out.println(instrucciones);
        return instrucciones;
    }   

    public void inicializarSO(int tamanno){
        cpu.get(0).reset();
        cpu.get(1).reset();
        cpu.get(0).setPC(0);
        cpu.get(1).setPC(0);
        int base = getEspacioSO(tamanno);
        bcp.setPc(base);
    }

    public void cargarSO(String instr, CPU temp){
        int pos = temp.getPC();
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
    
    public void movRegistro(String registro,int valor, CPU temp){
        switch(registro.replace(",", "").toLowerCase()){
            case "ax":temp.setAX(valor);break;
            case "bx":temp.setBX(valor);break;
            case "cx":temp.setCX(valor);break;
            case "dx":temp.setDX(valor);break;
            
        }
    }

    public int getRegistro(String registro, CPU temp){
        switch(registro.replace(",", "").toLowerCase()){
            case "ax": return temp.getAX();
            case "bx": return temp.getBX();
            case "cx": return temp.getCX();
            case "dx": return temp.getDX();
            
        }
        return 0;
    }

    public String interprete(String instr, BCP proceso, CPU temp){
        String[] partes = instr.split(" ");
        String op = partes[0].toLowerCase();
        int newpc = proceso.getPc()+ 1;
        switch(op){
            case "load":
                temp.setAC(getRegistro(partes[1], temp));
                proceso.setPc(newpc);
                break;
            case "store":
                movRegistro(partes[1],temp.getAC(),temp);
                proceso.setPc(newpc);
                break;
            case "mov":
                switch (partes[2].toLowerCase()){
                    case "ax":
                    case "bx":
                    case "cx":
                    case "dx":
                        movRegistro(partes[1],getRegistro(partes[2],temp),temp);
                        break;
                    default:
                        movRegistro(partes[1],Integer.parseInt(partes[2]),temp);
                }
                proceso.setPc(newpc);
                break;
            case "sub":
                temp.setAC(temp.getAC()-getRegistro(partes[1],temp));
                proceso.setPc(newpc);
                break;
            case "add":
                temp.setAC(temp.getAC()+getRegistro(partes[1],temp));
                proceso.setPc(newpc);
                break;
            case "inc":
                if(partes.length == 1){
                    temp.setAC(temp.getAC()+1);
                }else{
                    switch(partes[1].toLowerCase()){
                    case "ax": temp.setAX(temp.getAX()+1); break;
                    case "bx": temp.setBX(temp.getBX()+1); break; 
                    case "cx": temp.setCX(temp.getCX()+1); break;
                    case "dx": temp.setDX(temp.getDX()+1); break; 
                    }
                }
                proceso.setPc(newpc);
                break;
            case "dec":
                if(partes.length == 1){
                    temp.setAC(temp.getAC()-1);
                }else{
                    switch(partes[1].toLowerCase()){
                    case "ax": temp.setAX(temp.getAX()-1); break;
                    case "bx": temp.setBX(temp.getBX()-1); break; 
                    case "cx": temp.setCX(temp.getCX()-1); break;
                    case "dx": temp.setDX(temp.getDX()-1);break; 
                    }
                }
                proceso.setPc(newpc);
                break;
            case "swap":
                int a = getRegistro(partes[1],temp);
                movRegistro(partes[1],getRegistro(partes[2],temp),temp);
                movRegistro(partes[2], a, temp);
                proceso.setPc(newpc);
                break;
            case "int":
                switch(partes[1].toLowerCase()){
                    case "20h":
                        return "~Exit";
                    case "10h":
                        return Integer.toString(temp.getDX());
                    case "09h":
                        return "~Input";
                    case "21h":
                        break;
                }
                proceso.setPc(newpc);
                break;
            case "jmp":
                int jmp = proceso.getPc()+ Integer.parseInt(partes[1]);
                //cpu.setPC((jmp >= 0)? jmp:0);
                proceso.setPc(jmp);
                break;
            case "cmp":
                cmpBandera = getRegistro(partes[1],temp) == getRegistro(partes[2],temp);
                proceso.setPc(newpc);
                break;
            case "je":
                if (cmpBandera){
                    int je = proceso.getPc()+ Integer.parseInt(partes[1]);
                    
                    //proceso.setPC((je >= 0)? je:0);
                    proceso.setPc(je);
                }
                break;
            case "jne":
                if (!cmpBandera){
                   
                    int jne = proceso.getPc()+ Integer.parseInt(partes[1]);
                   // cpu.setPC((jne >= 0)? jne:0);
                    proceso.setPc(jne);
                }
                break;
            case "param":
                for (int i= 1; i<partes.length;i++){
                    pila.push(Integer.parseInt(partes[i].replace(",","")));
                }
                proceso.setPc(newpc);
                break;
            case "push":
                Stack u = pila;
                u.push((Integer)getRegistro(partes[1],temp));
                proceso.setPc(newpc);
                break;
            case "pop":
                int pop = pila.pop();
                switch(partes[1].toLowerCase()){
                case "ax": temp.setAX(pop); break;
                case "bx": temp.setBX(pop); break; 
                case "cx": temp.setCX(pop); break;
                case "dx": temp.setDX(pop); break; 
                }
                proceso.setPc(newpc);
                break;
        }
        return "";
    }
    public int getValue(String val){
        int time =0;
        switch(val){
            case "20h":
                time= 1000;
                break;
            case "10h":
                time= 1000;
                break;
            case "9h":
                time= 500; //tiempo int - entrada de usuario
                break;
            case "21h":
                time= 1000;
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
                time= 1000;
                break;
            case "store":
                time= 1000;
                break;
            case "mov":
                time= 1000;
                break;
            case "sub":
                time= 1000;
                break;
            case "add":
                time= 1000;
                break;
            case "inc":
                time= 1000;
                break;
            case "dec":
                time= 1000;
                break;    
            case "swap":
                time= 1000;
                break;  
            case "int":
                time= getValue(val);
                break;
            case "jmp":
                time = 1000;
                break;
            case "cmp":
                time = 1000;
                break;
            case "jne":
                time = 1000;
                break;
            case "je":
                time = 1000;
                break;
            case "param":
                time = 1000;
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
    public void actualizarBCPDesdeCPU(int id,BCP bcp, CPU temp) {
        bcp.setAc(temp.getAC());
        bcp.setAx(temp.getAX());
        bcp.setBx(temp.getBX());
        bcp.setCx(temp.getCX());
        bcp.setDx(temp.getDX());
        bcp.setIr(temp.getIR());
        bcp.setRegistro(id,"ac",temp.getAC());
        bcp.setRegistro(id,"ax",temp.getAX());
        bcp.setRegistro(id,"bx",temp.getBX());
        bcp.setRegistro(id,"cx",temp.getCX());
        bcp.setRegistro(id,"dx",temp.getDX());
    }
    

    public void crearProcesos(){
        int contProceso=0;
        List<String> archAcc = new ArrayList<>();
        int cpu=1;
        for(int i=0;i<getIntr().size();i++){
            String instru = disco.getDisco(i);
            if(instru.contains("|")){
                String[] partes = instru.split("\\|");
                String nombreArchivo = partes[0];
                
                int base = Integer.parseInt(partes[1]);
                int alcance = Integer.parseInt(partes[2]);
                String estado ;
                if(i<5){estado="nuevo";}//<5
                else {estado = "nuevo";cpu=1;}
                archAcc.add(nombreArchivo);
                BCP bcp = new BCP(contProceso,estado,i+1,base,alcance,cpuCounter++%2);
                bcp.setCpuAsig("Hilo "+cpu);
           
                bcp.getArchivos().addAll(archAcc);
                System.out.println(bcp.toString());
                
                plan.agregarProceso(nombreArchivo,bcp);
                cpu++;
                contProceso++;
            
                
            }
        } 
        crearParticiones();
        memoria.inicializarBloqueDinamico(memoria.size());
    }
    public void crearParticiones(){
        particionesFija.clear();
        int contProceso=0;
        int inicio = getEspacioSO(memoria.size());
        while(inicio<memoria.size()){
            particionesFija.put(contProceso,new Particion(inicio,32,false));
            contProceso++;
            inicio+=32;
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
    
    
    public CPU getCPU(int i) {return cpu.get(i);}
    public Disco getDisco() {return disco;}
    public Planificador getPlanificador() {return plan;}
    public BCP getBCP() {return bcp;}
    public Memoria getMemoria(){return memoria;}
    public Stack getPila(){return pila;}
}
