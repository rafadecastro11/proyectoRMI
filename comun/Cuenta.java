package comun;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaz remota para una cuenta bancaria individual.
 * Universidad de Sevilla - Sistemas Distribuidos
 */
public interface Cuenta extends Remote {

    /**
     * Obtiene el saldo actual de la cuenta.
     * @return El saldo actual
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    float obtenerSaldo() throws RemoteException;

    /**
     * Ingresa una cantidad en la cuenta.
     * @param cuantia La cantidad a ingresar
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    void ingresar(float cuantia) throws RemoteException;

    /**
     * Retira una cantidad de la cuenta.
     * @param cuantia La cantidad a retirar
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    void retirar(float cuantia) throws RemoteException;

    /**
     * Obtiene el identificador del titular.
     * @return El ID del titular
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    String getIdTitular() throws RemoteException;

    /**
     * Obtiene el nombre del titular.
     * @return El nombre del titular
     * @throws RemoteException Si ocurre un error en la comunicación RMI
     */
    String getNombre() throws RemoteException;
}
