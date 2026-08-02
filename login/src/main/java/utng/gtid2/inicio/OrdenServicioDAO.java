package utng.gtid2.inicio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Repositorio en memoria (simulación de base de datos) para Órdenes de servicio.
 * Centraliza la lista de datos y la orden actualmente seleccionada,
 * para que ListaOrdenes, CrearOrden y DetalleOrden compartan información
 * sin necesidad de una base de datos real.
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/OrdenServicioDAO.java
 */
public class OrdenServicioDAO {

    private static final ObservableList<OrdenServicio> ORDENES = FXCollections.observableArrayList();
    private static OrdenServicio ordenSeleccionada;
    private static boolean datosInicializados = false;

    /** Carga datos de prueba una sola vez. */
    public static ObservableList<OrdenServicio> obtenerTodas() {
        if (!datosInicializados) {
            cargarDatosPrueba();
            datosInicializados = true;
        }
        return ORDENES;
    }

    public static void agregar(OrdenServicio orden) {
        ORDENES.add(orden);
    }

    public static void eliminar(OrdenServicio orden) {
        ORDENES.remove(orden);
    }

    public static void actualizar(OrdenServicio anterior, OrdenServicio nueva) {
        int idx = ORDENES.indexOf(anterior);
        if (idx >= 0) {
            ORDENES.set(idx, nueva);
        }
    }

    public static OrdenServicio getOrdenSeleccionada() {
        return ordenSeleccionada;
    }

    public static void setOrdenSeleccionada(OrdenServicio orden) {
        ordenSeleccionada = orden;
    }

    /** Genera un número de orden consecutivo simple. */
    public static String generarNuevoNumero() {
        int siguiente = obtenerTodas().size() + 1;
        return String.format("OS-2026-%03d", siguiente);
    }

    private static void cargarDatosPrueba() {
        ORDENES.add(new OrdenServicio(
                "OS-2026-001", "Grupo Alfa", "Instalación eléctrica completa",
                "Luis Ramos", "15 jun 2026", "Completada",
                "Eléctrico", "Normal", "Av. Insurgentes 100", "Cable, contactos"));

        ORDENES.add(new OrdenServicio(
                "OS-2026-002", "Constructora Beta", "Plomería general planta baja",
                "Ana Torres", "16 jun 2026", "En proceso",
                "Plomería", "Alta", "Av. Juárez 45, Centro", "Tubo PVC, pegamento"));

        ORDENES.add(new OrdenServicio(
                "OS-2026-003", "Casa López", "Pintura interior recámaras",
                "Sin asignar", "17 jun 2026", "Pendiente",
                "Acabados", "Normal", "Calle Reforma 22", "Pintura blanca"));

        ORDENES.add(new OrdenServicio(
                "OS-2026-004", "Inmobiliaria Norte", "Colocación de pisos",
                "Mario Díaz", "18 jun 2026", "En proceso",
                "Acabados", "Normal", "Blvd. Norte 300", "Loseta, cemento"));

        ORDENES.add(new OrdenServicio(
                "OS-2026-005", "Hotel Paraíso", "Reparación de techo",
                "Luis Ramos", "18 jun 2026", "Pausada",
                "Estructura", "Urgente", "Carretera Sur Km 5", "Lámina, varilla"));
    }
}
