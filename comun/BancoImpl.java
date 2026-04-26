package comun;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BancoImpl extends UnicastRemoteObject implements Banco {

    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/banco";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    /**
     * Constructor de BancoImpl.
     * @throws RemoteException Si ocurre un error en la exportación del objeto
     */
    public BancoImpl() throws Exception {
        super();
        Class.forName("org.postgresql.Driver");
        System.out.println("BancoImpl inicializado - Driver PostgreSQL cargado");
    }

    /**
     * Crea una nueva cuenta bancaria para un titular.
     * @param t El titular de la cuenta (recibido por valor)
     * @return El objeto remoto Cuenta
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public Cuenta crearCuenta(Titular t) throws RemoteException {
        if (t == null || t.getId() == null || t.getId().trim().isEmpty()) {
            throw new RemoteException("El titular y su ID son obligatorios");
        }

        try {
            System.out.println("Creando cuenta para titular: " + t.getNombre() + " (ID: " + t.getId() + ")");

           
            CuentaImpl nuevaCuenta = new CuentaImpl(t.getId(), t.getNombre());
            return nuevaCuenta;

        } catch (Exception e) {
            throw new RemoteException("Error al crear cuenta: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene la lista de todas las cuentas bancarias desde la base de datos.
     * @return Lista de objetos remotos Cuenta
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public List<Cuenta> obtenerCuentas() throws RemoteException {
        List<Cuenta> cuentas = new ArrayList<>();
        String sql = "SELECT id_titular, nombre FROM cuentas";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String idTitular = rs.getString("id_titular");
                String nombre = rs.getString("nombre");

                try {
                    Cuenta cuenta = new CuentaImpl(idTitular, nombre);
                    cuentas.add(cuenta);
                } catch (Exception e) {
                    System.err.println("Error al crear cuenta para " + nombre + ": " + e.getMessage());
                }
            }

            System.out.println("Total de cuentas obtenidas: " + cuentas.size());

        } catch (SQLException e) {
            throw new RemoteException("Error al obtener cuentas: " + e.getMessage(), e);
        }

        return cuentas;
    }

    /**
     * Crea un nuevo usuario (titular) en la base de datos.
     * @param t El titular a crear
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    @Override
    public void crearUsuario(Titular t) throws RemoteException {
        if (t == null || t.getId() == null || t.getId().trim().isEmpty()) {
            throw new RemoteException("El titular y su ID son obligatorios");
        }
        if (t.getNombre() == null || t.getNombre().trim().isEmpty()) {
            throw new RemoteException("El nombre del titular es obligatorio");
        }

        String sql = "INSERT INTO cuentas (id_titular, nombre, saldo) VALUES (?, ?, 0.0)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getId());
            stmt.setString(2, t.getNombre());
            stmt.executeUpdate();

            System.out.println("Usuario creado: " + t.getNombre() + " (ID: " + t.getId() + ")");

        } catch (SQLException e) {
            throw new RemoteException("Error al crear usuario: " + e.getMessage(), e);
        }
    }
}
