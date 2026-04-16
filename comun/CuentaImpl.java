package comun;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementación del objeto remoto Cuenta.
 * Extiende UnicastRemoteObject y usa métodos synchronized para concurrencia.
 * Universidad de Sevilla - Sistemas Distribuidos
 */
public class CuentaImpl extends UnicastRemoteObject implements Cuenta {

    private static final long serialVersionUID = 1L;

    // Configuración de la base de datos
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/banco";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    private final String idTitular;
    private final String nombre;

    /**
     * Constructor de CuentaImpl.
     * @param idTitular Identificador del titular
     * @param nombre Nombre del titular
     * @throws RemoteException Si ocurre un error en la exportación del objeto
     */
    public CuentaImpl(String idTitular, String nombre) throws Exception {
        super();
        // Cargar explícitamente el driver JDBC
        Class.forName("org.postgresql.Driver");
        this.idTitular = idTitular;
        this.nombre = nombre;
        // Crear la cuenta en la base de datos si no existe
        inicializarCuenta();
    }

    /**
     * Inicializa la cuenta en la base de datos.
     */
    private void inicializarCuenta() {
        String sql = "INSERT INTO cuentas (id_titular, nombre, saldo) VALUES (?, ?, 0.0) " +
                     "ON CONFLICT (id_titular) DO NOTHING";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idTitular);
            stmt.setString(2, nombre);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al inicializar cuenta: " + e.getMessage());
        }
    }

    /**
     * Obtiene el saldo actual de la cuenta (método synchronized para concurrencia).
     * @return El saldo actual
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public synchronized float obtenerSaldo() throws RemoteException {
        String sql = "SELECT saldo FROM cuentas WHERE id_titular = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idTitular);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getFloat("saldo");
            }

        } catch (SQLException e) {
            throw new RemoteException("Error al obtener saldo: " + e.getMessage(), e);
        }

        return 0.0f;
    }

    /**
     * Ingresa una cantidad en la cuenta (método synchronized para concurrencia).
     * @param cuantia La cantidad a ingresar
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public synchronized void ingresar(float cuantia) throws RemoteException {
        if (cuantia <= 0) {
            throw new RemoteException("La cantidad a ingresar debe ser positiva");
        }

        String sql = "UPDATE cuentas SET saldo = saldo + ? WHERE id_titular = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setFloat(1, cuantia);
            stmt.setString(2, idTitular);
            int filas = stmt.executeUpdate();

            if (filas == 0) {
                throw new RemoteException("No se encontró la cuenta para ingresar");
            }

            System.out.println("Ingresada cantidad: " + cuantia + " en cuenta de " + nombre);

        } catch (SQLException e) {
            throw new RemoteException("Error al ingresar cantidad: " + e.getMessage(), e);
        }
    }

    /**
     * Retira una cantidad de la cuenta (método synchronized para concurrencia).
     * @param cuantia La cantidad a retirar
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public synchronized void retirar(float cuantia) throws RemoteException {
        if (cuantia <= 0) {
            throw new RemoteException("La cantidad a retirar debe ser positiva");
        }

        // Verificar saldo suficiente
        float saldoActual = obtenerSaldo();
        if (cuantia > saldoActual) {
            throw new RemoteException("Saldo insuficiente. Saldo actual: " + saldoActual);
        }

        String sql = "UPDATE cuentas SET saldo = saldo - ? WHERE id_titular = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setFloat(1, cuantia);
            stmt.setString(2, idTitular);
            int filas = stmt.executeUpdate();

            if (filas == 0) {
                throw new RemoteException("No se encontró la cuenta para retirar");
            }

            System.out.println("Retirada cantidad: " + cuantia + " de cuenta de " + nombre);

        } catch (SQLException e) {
            throw new RemoteException("Error al retirar cantidad: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el identificador del titular.
     * @return El ID del titular
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public String getIdTitular() throws RemoteException {
        return idTitular;
    }

    /**
     * Obtiene el nombre del titular.
     * @return El nombre del titular
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public String getNombre() throws RemoteException {
        return nombre;
    }

    @Override
    public String toString() {
        try {
            return "Cuenta{" +
                   "titular='" + nombre + '\'' +
                   ", idTitular='" + idTitular + '\'' +
                   ", saldo=" + obtenerSaldo() +
                   '}';
        } catch (RemoteException e) {
            return "Cuenta{" +
                   "titular='" + nombre + '\'' +
                   ", idTitular='" + idTitular + '\'' +
                   ", saldo=<error>}";
        }
    }
}
