package utng.gtid2.inicio;

import java.time.LocalDate;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de datos para una Orden de servicio.
 * Usa StringProperty para ser 100% compatible con TableView + PropertyValueFactory.
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/OrdenServicio.java
 */
public class OrdenServicio {

    private final StringProperty numero;
    private final StringProperty cliente;
    private final StringProperty descripcion;
    private final StringProperty tecnico;
    private final StringProperty fecha;
    private final StringProperty estado;

    // Campos adicionales usados en DetalleOrden / CrearOrden
    private final StringProperty tipoServicio;
    private final StringProperty prioridad;
    private final StringProperty direccion;
    private final StringProperty materiales;

    public OrdenServicio(String numero, String cliente, String descripcion,
                          String tecnico, String fecha, String estado,
                          String tipoServicio, String prioridad,
                          String direccion, String materiales) {
        this.numero = new SimpleStringProperty(numero);
        this.cliente = new SimpleStringProperty(cliente);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.tecnico = new SimpleStringProperty(tecnico);
        this.fecha = new SimpleStringProperty(fecha);
        this.estado = new SimpleStringProperty(estado);
        this.tipoServicio = new SimpleStringProperty(tipoServicio);
        this.prioridad = new SimpleStringProperty(prioridad);
        this.direccion = new SimpleStringProperty(direccion);
        this.materiales = new SimpleStringProperty(materiales);
    }

    /** Constructor simplificado usado al crear una orden nueva desde el formulario. */
    public OrdenServicio(String numero, String cliente, String tipoServicio,
                          String tecnico, LocalDate fecha, String prioridad,
                          String direccion, String descripcion, String materiales) {
        this(numero, cliente, descripcion, tecnico,
                fecha != null ? fecha.toString() : "",
                "Pendiente", tipoServicio, prioridad, direccion, materiales);
    }

    // ---------------- Getters / Setters / Properties ----------------

    public String getNumero() { return numero.get(); }
    public void setNumero(String v) { numero.set(v); }
    public StringProperty numeroProperty() { return numero; }

    public String getCliente() { return cliente.get(); }
    public void setCliente(String v) { cliente.set(v); }
    public StringProperty clienteProperty() { return cliente; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String v) { descripcion.set(v); }
    public StringProperty descripcionProperty() { return descripcion; }

    public String getTecnico() { return tecnico.get(); }
    public void setTecnico(String v) { tecnico.set(v); }
    public StringProperty tecnicoProperty() { return tecnico; }

    public String getFecha() { return fecha.get(); }
    public void setFecha(String v) { fecha.set(v); }
    public StringProperty fechaProperty() { return fecha; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public StringProperty estadoProperty() { return estado; }

    public String getTipoServicio() { return tipoServicio.get(); }
    public void setTipoServicio(String v) { tipoServicio.set(v); }
    public StringProperty tipoServicioProperty() { return tipoServicio; }

    public String getPrioridad() { return prioridad.get(); }
    public void setPrioridad(String v) { prioridad.set(v); }
    public StringProperty prioridadProperty() { return prioridad; }

    public String getDireccion() { return direccion.get(); }
    public void setDireccion(String v) { direccion.set(v); }
    public StringProperty direccionProperty() { return direccion; }

    public String getMateriales() { return materiales.get(); }
    public void setMateriales(String v) { materiales.set(v); }
    public StringProperty materialesProperty() { return materiales; }

    @Override
    public String toString() {
        return numero.get() + " - " + cliente.get();
    }
}
