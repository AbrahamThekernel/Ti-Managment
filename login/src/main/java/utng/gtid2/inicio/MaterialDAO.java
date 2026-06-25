package utng.gtid232.jjcm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Repositorio en memoria (simulación de base de datos) para Materiales.
 * Centraliza la lista de datos y el material actualmente seleccionado,
 * para que ListaInventario, AgregarMaterial, EditarMaterial y DetalleMaterial
 * compartan información sin necesidad de una base de datos real.
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/MaterialDAO.java
 */
public class MaterialDAO {

    private static final ObservableList<Material> MATERIALES = FXCollections.observableArrayList();
    private static Material materialSeleccionado;
    private static boolean datosInicializados = false;
    private static int contadorId = 0;

    public static ObservableList<Material> obtenerTodos() {
        if (!datosInicializados) {
            cargarDatosPrueba();
            datosInicializados = true;
        }
        return MATERIALES;
    }

    public static void agregar(Material material) {
        MATERIALES.add(material);
    }

    public static void eliminar(Material material) {
        MATERIALES.remove(material);
    }

    public static void actualizar(Material anterior, Material nuevo) {
        int idx = MATERIALES.indexOf(anterior);
        if (idx >= 0) {
            MATERIALES.set(idx, nuevo);
        }
    }

    public static Material getMaterialSeleccionado() {
        return materialSeleccionado;
    }

    public static void setMaterialSeleccionado(Material material) {
        materialSeleccionado = material;
    }

    /** Genera un ID consecutivo simple para nuevos materiales. */
    public static String generarNuevoId() {
        contadorId++;
        return String.valueOf(obtenerTodos().size() + contadorId);
    }

    /** Genera un código consecutivo simple tipo MAT-00X. */
    public static String generarNuevoCodigo() {
        int siguiente = obtenerTodos().size() + 1;
        return String.format("MAT-%03d", siguiente);
    }

    private static void cargarDatosPrueba() {
        MATERIALES.add(new Material("1", "MAT-001", "Cable eléctrico 12 AWG", "Eléctrico",
                "Cable para instalaciones eléctricas residenciales.",
                "245", "50", "Metros", "12.50", "ElectroPro S.A.",
                "01 enero 2024", "Disponible"));

        MATERIALES.add(new Material("2", "MAT-002", "Tubo PVC 1/2\"", "Plomería",
                "Tubo PVC sanitario de 1/2 pulgada, largo estándar 3 metros.",
                "18", "20", "Piezas", "45.00", "Ferremat S.A.",
                "03 enero 2024", "Bajo Stock"));

        MATERIALES.add(new Material("3", "MAT-003", "Pintura blanca 19 L", "Acabados",
                "Pintura vinílica blanca para interiores.",
                "0", "5", "Cubetas", "850.00", "Pinturas del Bajío",
                "10 enero 2024", "Agotado"));

        MATERIALES.add(new Material("4", "MAT-004", "Varilla 3/8\" x 12 m", "Estructura",
                "Varilla corrugada para refuerzo de concreto.",
                "60", "10", "Piezas", "180.00", "Aceros del Centro",
                "15 enero 2024", "Disponible"));

        MATERIALES.add(new Material("5", "MAT-005", "Cemento gris 50 kg", "Construcción",
                "Cemento Portland gris, saco de 50 kg.",
                "8", "10", "Sacos", "210.00", "Cementos Nacionales",
                "20 enero 2024", "Bajo Stock"));
    }
}
