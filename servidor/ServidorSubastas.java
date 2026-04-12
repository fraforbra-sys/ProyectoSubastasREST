package servidor;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Servidor principal del sistema de subastas RMI.
 * Inicia el rmiregistry, crea la instancia del gestor y la registra.
 *
 * Uso: java servidor.ServidorSubastas [puerto]
 * Si no se especifica puerto, usa el 54321 por defecto.
 */
public class ServidorSubastas {

    public static void main(String[] args) {
        int puerto = 1099; // Puerto por defecto

        if (args.length >= 1) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido. Usando puerto por defecto: " + puerto);
            }
        }

        System.out.println("========================================");
        System.out.println("  SERVIDOR DE SUBASTAS RMI");
        System.out.println("  Puerto: " + puerto);
        System.out.println("========================================");

        try {
            // 1. Crear el registry en el puerto especificado
            System.out.println("[Servidor] Iniciando RMI Registry en puerto " + puerto + "...");
            LocateRegistry.createRegistry(puerto);
            System.out.println("[Servidor] RMI Registry iniciado correctamente");

            // 2. Crear la instancia del gestor de subastas
            System.out.println("[Servidor] Creando GestorSubastasImpl...");
            GestorSubastasImpl gestor = new GestorSubastasImpl();

            // 3. Registrar el servicio en el registry
            String nombreServicio = "rmi://localhost:" + puerto + "/GestorSubastas";
            System.out.println("[Servidor] Registrando servicio como: " + nombreServicio);
            Naming.rebind(nombreServicio, gestor);

            System.out.println("========================================");
            System.out.println("  SERVIDOR LISTO");
            System.out.println("  Servicio disponible en: " + nombreServicio);
            System.out.println("  Presione Ctrl+C para detener");
            System.out.println("========================================");

            // Mantener el servidor ejecutándose
            // El servidor se mantiene activo gracias al registry y los objetos exportados

        } catch (RemoteException e) {
            System.err.println("[Servidor] Error de comunicación RMI: " + e.getMessage());
            System.err.println("Posibles causas:");
            System.err.println("  - El puerto " + puerto + " ya está en uso");
            System.err.println("  - No se puede crear el registry");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[Servidor] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
