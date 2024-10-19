import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

public class DemonioMonitorArchivos {
    private static String sourceDir;
    private static String destDir;

    public static void main(String[] args) throws IOException {
        // Identificar el sistema operativo
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("Ejecutando en: " + os);

        // Leer la configuración de carpetas
        Properties config = new Properties();
        try {
            config.load(Files.newBufferedReader(Paths.get("C:/Users/barre/Documents/Universidad/Cuarto_Semestre/Programacion_I/Demonio/Codigo/config.properties")));
            
            if (os.contains("win")) {
                // Si estamos en Windows
                sourceDir = config.getProperty("sourceDir.windows");
                destDir = config.getProperty("destDir.windows");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Si estamos en Linux o macOS
                sourceDir = config.getProperty("sourceDir.linux");
                destDir = config.getProperty("destDir.linux");
            } else {
                System.out.println("Sistema operativo no soportado.");
                return;
            }

            System.out.println("Directorio de origen: " + sourceDir);
            System.out.println("Directorio de destino: " + destDir);
            
        } catch (IOException e) {
            System.out.println("Error Cargando Archivo Config: " + e.getMessage());
            return;
        }

        // Crear el servicio de monitoreo
        WatchService servicioMonitoreo = FileSystems.getDefault().newWatchService();
        Path carpOrigen = Paths.get(sourceDir);
        carpOrigen.register(servicioMonitoreo, StandardWatchEventKinds.ENTRY_CREATE);

        // Ejecutar en segundo plano
        while (true) {
            WatchKey key;
            try {
                key = servicioMonitoreo.take();
            } catch (InterruptedException e) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path nombreArchivo = (Path) event.context();
                    System.out.println("Nuevo Archivo Detectado: " + nombreArchivo);
                    // Copiar el archivo al directorio de destino
                    Path archivoFuente = carpOrigen.resolve(nombreArchivo);
                    Path archivoDestino = Paths.get(destDir).resolve(nombreArchivo);
                    try {
                        Files.copy(archivoFuente, archivoDestino, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Archivo Copiado a: " + archivoDestino);
                    } catch (IOException e) {
                        System.out.println("Error Copiando el Archivo: " + e.getMessage());
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
