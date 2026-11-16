import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.sql.*;

public class EstacionMaritima {

    private static final String URL = "jdbc:mysql://localhost:3307/gasolineras_db";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static void main(String[] args) {
        System.out.println("Iniciando proceso de introducción de datos  de POSTES MARÍTIMOS...");

        try (Connection conexion = DriverManager.getConnection(URL, USER, PASS)) {
            conexion.setAutoCommit(false);

            ObjectMapper mapper = new ObjectMapper();
            // Lee el archivo postes.json
            JsonNode root = mapper.readTree(new File("postes.json"));

            JsonNode listaPostes = root.isArray() ? root : root.path("ListaEESSPrecio");
            System.out.println("Postes encontrados: " + listaPostes.size());

            int contador = 0;
            for (JsonNode poste : listaPostes) {
                try {
                    String rotulo = poste.path("Rótulo").asText();
                    String provincia = poste.path("Provincia").asText();
                    String municipio = poste.path("Municipio").asText();
                    String localidad = poste.path("Localidad").asText();
                    String cp = poste.path("C.P.").asText();
                    String direccion = poste.path("Dirección").asText();
                    String horario = poste.path("Horario").asText();
                    String fechaAct = poste.path("Fecha").asText();
                    String puerto = poste.path("Puerto").asText();

                    double latitud = obtenerUbicacion(poste.path("Latitud").asText());
                    double longitud = obtenerUbicacion(poste.path("Longitud").asText());

                    int idProvincia = obtenerInsertarProvincia(conexion, provincia);
                    int idMunicipio = obtenerInsertarMunicipio(conexion, municipio, idProvincia);
                    int idEmpresa = obtenerInsertarEmpresa(conexion, rotulo);

                    int idEstacion = insertarEstacion(conexion, idMunicipio, idEmpresa, localidad, cp,
                            direccion, latitud, longitud, horario, fechaAct, "MARITIMA");

                    insertarPrecios(conexion, idEstacion, poste, fechaAct);

                    contador++;
                    if (contador % 100 == 0) System.out.println("Procesados " + contador + " postes...");

                } catch (Exception e) {
                    System.err.println("Error procesando poste: " + e.getMessage());
                }
            }
            conexion.commit();
            System.out.println("¡Introducción de datos de postes completada! Total: " + contador);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double obtenerUbicacion(String valor) {
        if (valor == null || valor.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static int obtenerInsertarProvincia(Connection conexion, String nombre) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT id_provincia FROM provincia WHERE nombre = ?")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("id_provincia");
        }
        try (PreparedStatement ps = conexion.prepareStatement("INSERT INTO provincia (nombre) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Error provincia");
    }

    private static int obtenerInsertarMunicipio(Connection conexion, String nombre, int idProvincia) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT id_municipio FROM municipio WHERE nombre = ? AND id_provincia = ?")) {
            ps.setString(1, nombre);
            ps.setInt(2, idProvincia);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("id_municipio");
        }
        try (PreparedStatement ps = conexion.prepareStatement("INSERT INTO municipio (nombre, id_provincia) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setInt(2, idProvincia);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Error municipio");
    }

    private static int obtenerInsertarEmpresa(Connection conexion, String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) nombre = "DESCONOCIDA";
        try (PreparedStatement ps = conexion.prepareStatement("SELECT id_empresa FROM empresa WHERE nombre = ?")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("id_empresa");
        }
        try (PreparedStatement ps = conexion.prepareStatement("INSERT INTO empresa (nombre) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Error empresa");
    }

    private static int insertarEstacion(Connection conexion, int idMun, int idEmp, String loc, String cp, String dir, double lat, double lon, String horario, String fecha, String tipo) throws SQLException {
        String sql = "INSERT INTO estacion (id_municipio, id_empresa, localidad, codigo_postal, direccion, latitud, longitud, horario, fecha_actualizacion, tipo) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idMun);
            ps.setInt(2, idEmp);
            ps.setString(3, loc);
            ps.setString(4, cp);
            ps.setString(5, dir);
            ps.setDouble(6, lat);
            ps.setDouble(7, lon);
            ps.setString(8, horario);
            ps.setString(9, convertirFecha(fecha));
            ps.setString(10, tipo);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Error estación");
    }

    private static void insertarPrecios(Connection conexion, int idEstacion, JsonNode poste, String fecha) throws SQLException {
        // Con el nombre exacto de las tiquetas del archivo de postes marítimos
        String[] carburantes = {
                "Precio_x0020_Gasoleo_x0020_A_x0020_habitual",
                "Precio_x0020_Gasoleo_x0020_B",
                "Precio_x0020_Gasolina_x0020_95_x0020_E5",
                "Precio_x0020_Gasoleo_x0020_para_x0020_uso_x0020_marítimo"
        };

        for (String carb : carburantes) {
            String precioStr = poste.path(carb).asText();
            if (precioStr != null && !precioStr.trim().isEmpty()) {
                double valor = obtenerUbicacion(precioStr);

                // Limpio el nombre para que en la base de datos se vea "Gasoleo A habitual" en vez de "Precio_x0020_..."
                String nombreLimpio = carb.replace("Precio_x0020_", "").replace("_x0020_", " ").trim();

                int idCarburante = obtenerInsertarCarburante(conexion, nombreLimpio);
                String sql = "INSERT IGNORE INTO precio (id_estacion, id_carburante, fecha_actualizacion, precio) VALUES (?,?,?,?)";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setInt(1, idEstacion);
                    ps.setInt(2, idCarburante);
                    ps.setString(3, convertirFecha(fecha));
                    ps.setDouble(4, valor);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static int obtenerInsertarCarburante(Connection conexion, String nombre) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT id_carburante FROM carburante WHERE nombre = ?")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("id_carburante");
        }
        try (PreparedStatement ps = conexion.prepareStatement("INSERT INTO carburante (nombre) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Error carburante");
    }

    private static String convertirFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) return null;
        try {
            String[] partes = fecha.split(" ");
            String[] fechaParts = partes[0].split("/");
            return fechaParts[2] + "-" + fechaParts[1] + "-" + fechaParts[0] + " " + partes[1];
        } catch (Exception e) {
            return null;
        }
    }
}
