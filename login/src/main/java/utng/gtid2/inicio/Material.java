package utng.gtid2.inicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Material {

    private final StringProperty id;
    private final StringProperty codigo;
    private final StringProperty nombre;
    private final StringProperty categoria;
    private final StringProperty descripcion;
    private final StringProperty cantidad;
    private final StringProperty stockMinimo;
    private final StringProperty unidadMedida;
    private final StringProperty precio;
    private final StringProperty proveedor;
    private final StringProperty fechaRegistro;
    private final StringProperty estadoStock;

    public Material(String id, String codigo, String nombre, String categoria,
                     String descripcion, String cantidad, String stockMinimo,
                     String unidadMedida, String precio, String proveedor,
                     String fechaRegistro, String estadoStock) {
        this.id = new SimpleStringProperty(id);
        this.codigo = new SimpleStringProperty(codigo);
        this.nombre = new SimpleStringProperty(nombre);
        this.categoria = new SimpleStringProperty(categoria);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.cantidad = new SimpleStringProperty(cantidad);
        this.stockMinimo = new SimpleStringProperty(stockMinimo);
        this.unidadMedida = new SimpleStringProperty(unidadMedida);
        this.precio = new SimpleStringProperty(precio);
        this.proveedor = new SimpleStringProperty(proveedor);
        this.fechaRegistro = new SimpleStringProperty(fechaRegistro);
        this.estadoStock = new SimpleStringProperty(estadoStock);
    }

    /**
     * Calcula el estado de stock según cantidad vs. stock mínimo.
     * Reglas: 0 = Agotado | menor o igual al mínimo = Bajo Stock | mayor = Disponible
     */
    public static String calcularEstado(String cantidadStr, String minimoStr) {
        try {
            double cantidad = Double.parseDouble(cantidadStr);
            double minimo = Double.parseDouble(minimoStr);
            if (cantidad <= 0) return "Agotado";
            if (cantidad <= minimo) return "Bajo Stock";
            return "Disponible";
        } catch (NumberFormatException e) {
            return "Disponible";
        }
    }

    // ---------------- Getters / Setters / Properties ----------------

    public String getId() { return id.get(); }
    public void setId(String v) { id.set(v); }
    public StringProperty idProperty() { return id; }

    public String getCodigo() { return codigo.get(); }
    public void setCodigo(String v) { codigo.set(v); }
    public StringProperty codigoProperty() { return codigo; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v); }
    public StringProperty nombreProperty() { return nombre; }

    public String getCategoria() { return categoria.get(); }
    public void setCategoria(String v) { categoria.set(v); }
    public StringProperty categoriaProperty() { return categoria; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String v) { descripcion.set(v); }
    public StringProperty descripcionProperty() { return descripcion; }

    public String getCantidad() { return cantidad.get(); }
    public void setCantidad(String v) { cantidad.set(v); }
    public StringProperty cantidadProperty() { return cantidad; }

    public String getStockMinimo() { return stockMinimo.get(); }
    public void setStockMinimo(String v) { stockMinimo.set(v); }
    public StringProperty stockMinimoProperty() { return stockMinimo; }

    public String getUnidadMedida() { return unidadMedida.get(); }
    public void setUnidadMedida(String v) { unidadMedida.set(v); }
    public StringProperty unidadMedidaProperty() { return unidadMedida; }

    public String getPrecio() { return precio.get(); }
    public void setPrecio(String v) { precio.set(v); }
    public StringProperty precioProperty() { return precio; }

    public String getProveedor() { return proveedor.get(); }
    public void setProveedor(String v) { proveedor.set(v); }
    public StringProperty proveedorProperty() { return proveedor; }

    public String getFechaRegistro() { return fechaRegistro.get(); }
    public void setFechaRegistro(String v) { fechaRegistro.set(v); }
    public StringProperty fechaRegistroProperty() { return fechaRegistro; }

    public String getEstadoStock() { return estadoStock.get(); }
    public void setEstadoStock(String v) { estadoStock.set(v); }
    public StringProperty estadoStockProperty() { return estadoStock; }

    @Override
    public String toString() {
        return codigo.get() + " - " + nombre.get();
    }
}
