package controlador;


public class Particion {
    
  
    private int base;
    private int tamaño;
    private boolean ocupado;

    public Particion(int base, int tamaño, boolean ocupado) {

        this.base = base;
        this.tamaño = tamaño;
        this.ocupado = ocupado;
    }

    public int getBase() {
        return base;
    }

    public int getTamaño() {
        return tamaño;
    }

    public boolean isOcupado() {
        return ocupado;
    }


    public void setBase(int base) {
        this.base = base;
    }

    public void setTamaño(int tamaño) {
        this.tamaño = tamaño;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }
    @Override
    public String toString() {
        return "Particion{" +
           
                ", base=" + base +
                ", tamanno=" + tamaño +
                ", ocupada=" + ocupado +
                '}';
    }

    
    
    
    
}
