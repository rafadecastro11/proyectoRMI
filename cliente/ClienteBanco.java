package cliente;

import comun.*;
import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente interactivo del Sistema Bancario Distribuido.
 * Permite al usuario identificarse y realizar operaciones bancarias.
 * Universidad de Sevilla - Sistemas Distribuidos
 */
public class ClienteBanco {

    // Puerto y nombre del servicio (deben coincidir con el servidor)
    private static final int RMI_PORT = 54321;
    private static final String SERVICE_NAME = "Banco";

    private static Banco banco;
    private static Cuenta cuentaUsuario;
    private static String idTitular;
    private static String nombreTitular;
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Método principal que ejecuta el menú interactivo.
     * @param args Argumentos: [hostname] (por defecto: localhost)
     */
    public static void main(String[] args) {
        String hostname = (args.length > 0) ? args[0] : "localhost";

        System.out.println("=== Sistema Bancario Distribuido ===");
        System.out.println("Universidad de Sevilla - Sistemas Distribuidos");
        System.out.println();

        try {
            // Conectar con el servicio RMI
            conectarServicio(hostname);

            // Menú principal
            boolean continuar = true;
            while (continuar) {
                mostrarMenuPrincipal();
                int opcion = leerOpcion(1, 5);

                switch (opcion) {
                    case 1:
                        identificarse();
                        break;
                    case 2:
                        registrarse();
                        break;
                    case 3:
                        if (cuentaUsuario != null) {
                            mostrarMenuOperaciones();
                        } else {
                            System.out.println("Debe identificarse o registrarse primero.");
                        }
                        break;
                    case 4:
                        listarCuentas();
                        break;
                    case 5:
                        continuar = false;
                        System.out.println("Saliendo del sistema...");
                        break;
                }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.err.println("Posibles causas:");
            System.err.println("  - El servidor no está ejecutándose");
            System.err.println("  - Problemas de conectividad");
            System.exit(1);
        }
    }

    private static void conectarServicio(String hostname) throws Exception {
        String url = "rmi://" + hostname + ":" + RMI_PORT + "/" + SERVICE_NAME;
        System.out.println("[INFO] Conectando al servicio: " + url);

        banco = (Banco) Naming.lookup(url);
        System.out.println("[OK] Conectado al servidor bancario");
        System.out.println();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n=== MENÚ PRINCIPAL ===");
        if (cuentaUsuario != null) {
            System.out.println("Usuario: " + nombreTitular + " (ID: " + idTitular + ")");
            System.out.println("Saldo actual: " + obtenerSaldoFormatado());
        }
        System.out.println("1. Identificarse");
        System.out.println("2. Registrarse (crear nuevo usuario)");
        System.out.println("3. Realizar operaciones");
        System.out.println("4. Ver cuentas del sistema");
        System.out.println("5. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static void identificarse() {
        try {
            System.out.print("\nIntroduzca su ID de titular: ");
            idTitular = scanner.nextLine().trim();

            if (idTitular.isEmpty()) {
                System.out.println("ID inválido.");
                return;
            }

            // Buscar si existe una cuenta con este ID en la base de datos
            List<Cuenta> cuentas = banco.obtenerCuentas();
            boolean encontrado = false;
            String nombreEncontrado = null;

            for (Cuenta cuenta : cuentas) {
                String idCuenta = cuenta.getIdTitular();
                if (idCuenta != null && idCuenta.equals(idTitular)) {
                    nombreEncontrado = cuenta.getNombre();
                    encontrado = true;
                    break;
                }
            }

            if (encontrado) {
                // Crear una nueva instancia de Cuenta para este usuario
                Titular titular = new Titular(idTitular, nombreEncontrado);
                cuentaUsuario = banco.crearCuenta(titular);
                nombreTitular = nombreEncontrado;
                System.out.println("Identificación correcta. Bienvenido, " + nombreTitular);
            } else {
                System.out.println("No existe una cuenta con ID: " + idTitular);
                System.out.println("Use la opción 'Registrarse' para crear una nueva cuenta.");
                idTitular = null;
                nombreTitular = null;
            }

        } catch (Exception e) {
            System.err.println("Error al identificar: " + e.getMessage());
        }
    }

    private static void registrarse() {
        try {
            System.out.println("\n=== REGISTRO DE NUEVO USUARIO ===");
            System.out.print("Introduzca su ID de titular: ");
            idTitular = scanner.nextLine().trim();

            if (idTitular.isEmpty()) {
                System.out.println("ID inválido.");
                return;
            }

            // Verificar si el usuario ya existe
            List<Cuenta> cuentas = banco.obtenerCuentas();
            for (Cuenta cuenta : cuentas) {
                if (cuenta.getIdTitular() != null && cuenta.getIdTitular().equals(idTitular)) {
                    System.out.println("Ya existe un usuario con ID: " + idTitular);
                    System.out.println("Use la opción 'Identificarse' para acceder.");
                    return;
                }
            }

            System.out.print("Introduzca su nombre completo: ");
            nombreTitular = scanner.nextLine().trim();

            if (nombreTitular.isEmpty()) {
                System.out.println("Nombre inválido.");
                return;
            }

            // Crear el usuario en la base de datos
            Titular nuevoTitular = new Titular(idTitular, nombreTitular);
            banco.crearUsuario(nuevoTitular);
            System.out.println("Usuario registrado exitosamente.");
            System.out.println("Bienvenido, " + nombreTitular);

            // Crear la cuenta remota para el usuario
            cuentaUsuario = banco.crearCuenta(nuevoTitular);

        } catch (Exception e) {
            System.err.println("Error al registrar: " + e.getMessage());
        }
    }

    private static void mostrarMenuOperaciones() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n=== OPERACIONES BANCARIAS ===");
            System.out.println("Titular: " + nombreTitular);
            System.out.println("Saldo actual: " + obtenerSaldoFormatado());
            System.out.println("1. Consultar saldo");
            System.out.println("2. Ingresar cantidad");
            System.out.println("3. Retirar cantidad");
            System.out.println("4. Volver al menú principal");
            System.out.print("Seleccione una opción: ");

            int opcion = leerOpcion(1, 4);

            switch (opcion) {
                case 1:
                    consultarSaldo();
                    break;
                case 2:
                    ingresarCantidad();
                    break;
                case 3:
                    retirarCantidad();
                    break;
                case 4:
                    volver = true;
                    break;
            }
        }
    }

    private static String obtenerSaldoFormatado() {
        try {
            return String.format("%.2f EUR", cuentaUsuario.obtenerSaldo());
        } catch (Exception e) {
            return "Error";
        }
    }

    private static void consultarSaldo() {
        try {
            float saldo = cuentaUsuario.obtenerSaldo();
            System.out.println("Saldo actual: " + String.format("%.2f EUR", saldo));
        } catch (Exception e) {
            System.err.println("Error al consultar saldo: " + e.getMessage());
        }
    }

    private static void ingresarCantidad() {
        try {
            System.out.print("Cantidad a ingresar: ");
            String input = scanner.nextLine().trim();
            float cantidad = Float.parseFloat(input);

            if (cantidad <= 0) {
                System.out.println("La cantidad debe ser positiva.");
                return;
            }

            cuentaUsuario.ingresar(cantidad);
            System.out.println("Ingreso realizado. Nuevo saldo: " + String.format("%.2f EUR", cuentaUsuario.obtenerSaldo()));

        } catch (NumberFormatException e) {
            System.out.println("Cantidad inválida.");
        } catch (Exception e) {
            System.err.println("Error al ingresar: " + e.getMessage());
        }
    }

    private static void retirarCantidad() {
        try {
            System.out.print("Cantidad a retirar: ");
            String input = scanner.nextLine().trim();
            float cantidad = Float.parseFloat(input);

            if (cantidad <= 0) {
                System.out.println("La cantidad debe ser positiva.");
                return;
            }

            cuentaUsuario.retirar(cantidad);
            System.out.println("Retirada realizada. Nuevo saldo: " + String.format("%.2f EUR", cuentaUsuario.obtenerSaldo()));

        } catch (NumberFormatException e) {
            System.out.println("Cantidad inválida.");
        } catch (Exception e) {
            System.err.println("Error al retirar: " + e.getMessage());
        }
    }

    private static void listarCuentas() {
        try {
            List<Cuenta> cuentas = banco.obtenerCuentas();
            System.out.println("\n=== CUENTAS DEL SISTEMA ===");
            System.out.println("Total: " + cuentas.size() + " cuenta(s)");

            int i = 1;
            for (Cuenta cuenta : cuentas) {
                String id = cuenta.getIdTitular();
                String nombre = cuenta.getNombre();
                float saldo = cuenta.obtenerSaldo();
                System.out.println("  " + i + ". " + nombre +
                                   " (ID: " + id + ")" +
                                   " - Saldo: " + String.format("%.2f EUR", saldo));
                i++;
            }

        } catch (Exception e) {
            System.err.println("Error al listar cuentas: " + e.getMessage());
        }
    }

    private static int leerOpcion(int min, int max) {
        try {
            String input = scanner.nextLine().trim();
            int opcion = Integer.parseInt(input);
            if (opcion >= min && opcion <= max) {
                return opcion;
            }
            System.out.println("Opción no válida. Debe ser entre " + min + " y " + max + ".");
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Introduzca un número.");
        }
        return -1;
    }
}
