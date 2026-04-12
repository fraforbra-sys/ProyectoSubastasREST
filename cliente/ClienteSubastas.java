package cliente;

import comun.*;

import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente principal del sistema de subastas RMI.
 * Prueba toda la funcionalidad:
 * - Autenticación
 * - Creación de subastas (Factory)
 * - Registro de observadores (Callbacks)
 * - Realización de pujas
 * - Obtención de historial (Objetos serializables)
 *
 * Uso: java cliente.ClienteSubastas [puerto] [host]
 */
public class ClienteSubastas {

    private static IGestorSubastas gestor;
    private static ObservadorClienteImpl observador;
    private static String usuarioActual;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int puerto = 54321; // Puerto por defecto
        String host = "localhost";

        if (args.length >= 1) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido. Usando puerto por defecto: " + puerto);
            }
        }
        if (args.length >= 2) {
            host = args[1];
        }

        System.out.println("========================================");
        System.out.println("  CLIENTE DE SUBASTAS RMI");
        System.out.println("  Conectando a: rmi://" + host + ":" + puerto + "/GestorSubastas");
        System.out.println("========================================");

        try {
            // 1. Conectar con el servidor
            String urlServicio = "rmi://" + host + ":" + puerto + "/GestorSubastas";
            System.out.println("[Cliente] Buscando servicio...");
            gestor = (IGestorSubastas) Naming.lookup(urlServicio);
            System.out.println("[Cliente] Conectado correctamente con el servidor");

            // 2. Autenticarse
            autenticar();

            // 3. Crear observador para recibir callbacks
            System.out.println("[Cliente] Creando observador para callbacks...");
            observador = new ObservadorClienteImpl(usuarioActual);
            System.out.println("[Cliente] Observador creado y exportado");

            // 4. Menú principal
            mostrarMenu();

        } catch (Exception e) {
            System.err.println("[Cliente] Error: " + e.getMessage());
            System.err.println("Asegúrate de que el servidor esté ejecutándose.");
            e.printStackTrace();
        }
    }

    private static void autenticar() {
        System.out.println("\n--- AUTENTICACIÓN ---");
        System.out.println("1. Iniciar sesión");
        System.out.println("2. Registrarse como nuevo usuario");
        System.out.print("Opción: ");

        String opcion = scanner.nextLine().trim();

        if ("2".equals(opcion)) {
            registrarNuevoUsuario();
        }

        // Proceso de login (después del registro o si eligió opción 1)
        System.out.print("\nUsuario: ");
        String usuario = scanner.nextLine().trim();
        System.out.print("Contraseña: ");
        String password = scanner.nextLine().trim();

        try {
            while (!gestor.autenticar(usuario, password)) {
                System.out.println("Credenciales inválidas. Intente de nuevo.");
                System.out.print("Usuario: ");
                usuario = scanner.nextLine().trim();
                System.out.print("Contraseña: ");
                password = scanner.nextLine().trim();
            }
            usuarioActual = usuario;
            System.out.println("Autenticación correcta. Bienvenido, " + usuario);
        } catch (Exception e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void registrarNuevoUsuario() {
        System.out.println("\n--- REGISTRO DE NUEVO USUARIO ---");
        System.out.println("Requisitos:");
        System.out.println("  - Nombre de usuario: 3-50 caracteres (letras, números, guiones)");
        System.out.println("  - Contraseña: mínimo 4 caracteres");
        System.out.println();

        String username;
        String password;
        String passwordConfirm;

        // Obtener nombre de usuario válido
        while (true) {
            System.out.print("Nombre de usuario deseado: ");
            username = scanner.nextLine().trim();

            try {
                // Verificar si ya existe
                if (gestor.existeUsuario(username)) {
                    System.out.println("El usuario '" + username + "' ya está registrado. Intente otro.");
                    continue;
                }
                break; // Nombre disponible
            } catch (Exception e) {
                System.out.println("Error al verificar usuario: " + e.getMessage());
            }
        }

        // Obtener contraseña válida
        while (true) {
            System.out.print("Contraseña: ");
            password = scanner.nextLine().trim();

            if (password.length() < 4) {
                System.out.println("La contraseña debe tener al menos 4 caracteres.");
                continue;
            }

            System.out.print("Confirmar contraseña: ");
            passwordConfirm = scanner.nextLine().trim();

            if (!password.equals(passwordConfirm)) {
                System.out.println("Las contraseñas no coinciden. Intente de nuevo.");
                continue;
            }
            break; // Contraseña válida y confirmada
        }

        // Intentar registrar
        try {
            boolean exito = gestor.registrarUsuario(username, password);
            if (exito) {
                System.out.println("\n¡Registro exitoso! Ahora puede iniciar sesión.");
            } else {
                System.out.println("\nRegistro fallido. Intente de nuevo.");
            }
        } catch (Exception e) {
            System.err.println("Error en registro: " + e.getMessage());
        }
    }

    private static void mostrarMenu() {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n========== MENÚ PRINCIPAL ==========");
            System.out.println("1. Crear nueva subasta");
            System.out.println("2. Listar subastas activas");
            System.out.println("3. Buscar subastas por nombre");
            System.out.println("4. Buscar subastas por precio");
            System.out.println("5. Unirse a una subasta (ver detalles, pujar, observar)");
            System.out.println("6. Ver mi estado (observadores activos)");
            System.out.println("0. Salir");
            System.out.print("Opción: ");

            String opcion = scanner.nextLine().trim();

            try {
                switch (opcion) {
                    case "1":
                        crearSubasta();
                        break;
                    case "2":
                        listarSubastas();
                        break;
                    case "3":
                        buscarPorNombre();
                        break;
                    case "4":
                        buscarPorPrecio();
                        break;
                    case "5":
                        unirseASubasta();
                        break;
                    case "6":
                        System.out.println("Usuario actual: " + usuarioActual);
                        System.out.println("Observador: " + (observador != null ? "Activo" : "Inactivo"));
                        break;
                    case "0":
                        salir = true;
                        System.out.println("Saliendo del cliente...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void crearSubasta() throws Exception {
        System.out.println("\n--- CREAR NUEVA SUBASTA ---");

        System.out.print("ID del artículo: ");
        String id = scanner.nextLine().trim();
        System.out.print("Nombre del artículo: ");
        String nombre = scanner.nextLine().trim();
        System.out.print("Descripción: ");
        String descripcion = scanner.nextLine().trim();
        System.out.print("URL de imagen: ");
        String urlImagen = scanner.nextLine().trim();
        System.out.print("Precio de salida (€): ");
        double precioSalida = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Duración (segundos): ");
        long duracion = Long.parseLong(scanner.nextLine().trim());

        Articulo articulo = new Articulo(id, nombre, descripcion, urlImagen, precioSalida);
        ISubasta subasta = gestor.crearSubasta(articulo, duracion);

        System.out.println("\n¡Subasta creada con éxito!");
        System.out.println("  ID: " + subasta.getIdSubasta());
        System.out.println("  Artículo: " + articulo.getNombre());
        System.out.println("  Precio inicial: " + precioSalida + "€");
        System.out.println("  Duración: " + duracion + " segundos");
    }

    private static void listarSubastas() throws Exception {
        System.out.println("\n--- SUBASTAS ACTIVAS ---");
        List<ISubasta> subastas = gestor.listarSubastasActivas();

        if (subastas.isEmpty()) {
            System.out.println("No hay subastas activas en este momento.");
            return;
        }

        System.out.println("Total: " + subastas.size() + " subastas activas\n");
        int i = 1;
        for (ISubasta s : subastas) {
            System.out.println("[" + i + "] " + s.getArticulo().getNombre() +
                               " - Precio: " + s.getPrecioActual() + "€" +
                               " - Tiempo restante: " + s.getTiempoRestante() + "s");
            i++;
        }
    }

    private static void buscarPorNombre() throws Exception {
        System.out.print("Texto a buscar: ");
        String texto = scanner.nextLine().trim();

        List<ISubasta> resultados = gestor.buscarSubastas(texto);

        if (resultados.isEmpty()) {
            System.out.println("No se encontraron subastas que coincidan con '" + texto + "'");
            return;
        }

        System.out.println("\nResultados (" + resultados.size() + "):");
        for (ISubasta s : resultados) {
            System.out.println("  - " + s.getArticulo().getNombre() +
                               ": " + s.getPrecioActual() + "€");
        }
    }

    private static void buscarPorPrecio() throws Exception {
        System.out.print("Precio mínimo (€): ");
        double min = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Precio máximo (€): ");
        double max = Double.parseDouble(scanner.nextLine().trim());

        // Necesitamos acceder al método específico del gestor
        // Para simplificar, hacemos una búsqueda general y filtramos
        List<ISubasta> todas = gestor.listarSubastasActivas();

        System.out.println("\nSubastas en rango [" + min + " - " + max + "€]:");
        boolean encontradas = false;
        for (ISubasta s : todas) {
            double precio = s.getPrecioActual();
            if (precio >= min && precio <= max) {
                System.out.println("  - " + s.getArticulo().getNombre() +
                                   ": " + precio + "€");
                encontradas = true;
            }
        }
        if (!encontradas) {
            System.out.println("  No hay subastas en ese rango de precios.");
        }
    }

    private static void unirseASubasta() throws Exception {
        System.out.println("\n--- UNIRSE A SUBASTA ---");
        List<ISubasta> subastas = gestor.listarSubastasActivas();

        if (subastas.isEmpty()) {
            System.out.println("No hay subastas activas.");
            return;
        }

        // Mostrar subastas
        int i = 1;
        for (ISubasta s : subastas) {
            System.out.println("[" + i + "] " + s.getArticulo().getNombre() +
                               " - Precio: " + s.getPrecioActual() + "€");
            i++;
        }

        System.out.print("\nSeleccione número de subasta (0 para cancelar): ");
        int seleccion = Integer.parseInt(scanner.nextLine().trim());

        if (seleccion == 0) {
            return;
        }
        if (seleccion < 1 || seleccion > subastas.size()) {
            System.out.println("Selección inválida.");
            return;
        }

        ISubasta subasta = subastas.get(seleccion - 1);
        menuSubasta(subasta);
    }

    private static void menuSubasta(ISubasta subasta) throws Exception {
        boolean salir = false;

        // Registrar como observador automáticamente
        System.out.println("\n[Cliente] Registrándose como observador de la subasta...");
        subasta.registrarObservador(observador);
        System.out.println("[Cliente] Registrado correctamente. Recibirá notificaciones de pujas.");

        while (!salir) {
            System.out.println("\n=== SUBASTA: " + subasta.getArticulo().getNombre() + " ===");
            System.out.println("  Precio actual: " + subasta.getPrecioActual() + "€");
            System.out.println("  Líder actual: " + subasta.getLiderActual());
            System.out.println("  Tiempo restante: " + subasta.getTiempoRestante() + "s");
            System.out.println("  Estado: " + (subasta.isActiva() ? "ACTIVA" : "FINALIZADA"));

            System.out.println("\n1. Realizar puja");
            System.out.println("2. Ver historial completo (detalles serializados)");
            System.out.println("3. Ver artículo");
            System.out.println("0. Volver al menú principal");
            System.out.print("Opción: ");

            String opcion = scanner.nextLine().trim();

            try {
                switch (opcion) {
                    case "1":
                        realizarPuja(subasta);
                        break;
                    case "2":
                        verHistorial(subasta);
                        break;
                    case "3":
                        verArticulo(subasta);
                        break;
                    case "0":
                        salir = true;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void realizarPuja(ISubasta subasta) throws Exception {
        if (!subasta.isActiva()) {
            System.out.println("Esta subasta ya ha finalizado.");
            return;
        }

        double precioActual = subasta.getPrecioActual();
        System.out.println("Precio actual: " + precioActual + "€");
        System.out.print("Su puja (debe ser mayor que " + precioActual + "€): ");

        double cantidad = Double.parseDouble(scanner.nextLine().trim());

        boolean exito = subasta.pujar(usuarioActual, cantidad);

        if (exito) {
            System.out.println("¡Puja realizada con éxito! Ahora es el líder con " + cantidad + "€");
        } else {
            System.out.println("Puja rechazada. El precio debe ser mayor que " + precioActual + "€");
        }
    }

    private static void verHistorial(ISubasta subasta) throws Exception {
        System.out.println("\n=== HISTORIAL COMPLETO DE LA SUBASTA ===");

        // Este método devuelve un objeto Serializable completo
        HistorialSubasta historial = subasta.obtenerDetalles();

        Articulo articulo = historial.getArticulo();
        System.out.println("\nARTÍCULO:");
        System.out.println("  ID: " + articulo.getId());
        System.out.println("  Nombre: " + articulo.getNombre());
        System.out.println("  Descripción: " + articulo.getDescripcion());
        System.out.println("  Precio salida: " + articulo.getPrecioSalida() + "€");

        System.out.println("\nESTADO ACTUAL:");
        System.out.println("  Precio: " + historial.getPrecioActual() + "€");
        System.out.println("  Líder: " + historial.getLiderActual());
        System.out.println("  Activa: " + (historial.isActiva() ? "Sí" : "No"));

        System.out.println("\nHISTORIAL DE PUJAS (" + historial.getNumeroPujas() + "):");
        if (historial.getPujas().isEmpty()) {
            System.out.println("  No hay pujas registradas.");
        } else {
            for (Puja puja : historial.getPujas()) {
                System.out.println("  - " + puja.getUsuario() + ": " + puja.getCantidad() +
                                   "€ [" + puja.getTimestamp() + "]");
            }
        }

        System.out.println("\n[Cliente] Objeto HistorialSubasta recibido correctamente (serializado)");
    }

    private static void verArticulo(ISubasta subasta) throws Exception {
        Articulo a = subasta.getArticulo();
        System.out.println("\n=== ARTÍCULO ===");
        System.out.println("ID: " + a.getId());
        System.out.println("Nombre: " + a.getNombre());
        System.out.println("Descripción: " + a.getDescripcion());
        System.out.println("Precio salida: " + a.getPrecioSalida() + "€");
        System.out.println("Imagen: " + a.getUrlImagen());
    }
}
