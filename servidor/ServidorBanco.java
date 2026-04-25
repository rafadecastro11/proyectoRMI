package servidor;

import comun.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

/**
 * Servidor principal del Sistema Bancario Distribuido.
 * Inicia el registro RMI y exporta el objeto Banco.
 * Universidad de Sevilla - Sistemas Distribuidos
 */
public class ServidorBanco {

   
    private static final int RMI_PORT = 54321;


    private static final String SERVICE_NAME = "Banco";


    public static void main(String[] args) {
        System.out.println("=== Iniciando Servidor Bancario Distribuido ===");
        System.out.println("Universidad de Sevilla - Sistemas Distribuidos");
        System.out.println();

        try {
            // Iniciar el registro RMI en el puerto especificado
            System.out.println("[INFO] Iniciando RMI Registry en puerto " + RMI_PORT + "...");
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("[OK] RMI Registry iniciado en puerto " + RMI_PORT);

            // Crear la instancia del banco
            System.out.println("[INFO] Creando objeto BancoImpl...");
            BancoImpl banco = new BancoImpl();
            System.out.println("[OK] BancoImpl creado");

            // Exportar el objeto banco al registro RMI
            String url = "rmi://localhost:" + RMI_PORT + "/" + SERVICE_NAME;
            System.out.println("[INFO] Exportando servicio como: " + url);
            Naming.rebind(url, banco);
            System.out.println("[OK] Servicio bancario registrado correctamente");

            System.out.println();
            System.out.println("=== Servidor Bancario LISTO ===");
            System.out.println("Esperando peticiones de clientes...");
            System.out.println("Presione Ctrl+C para detener el servidor");

        } catch (RemoteException e) {
            System.err.println("[ERROR] RemoteException: " + e.getMessage());
            System.err.println("Verifique que el puerto " + RMI_PORT + " no esté en uso");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[ERROR] Exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
