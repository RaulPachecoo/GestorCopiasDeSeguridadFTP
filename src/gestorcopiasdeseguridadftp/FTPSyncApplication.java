package gestorcopiasdeseguridadftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase proporciona funcionalidades para sincronizar archivos entre una carpeta local y una carpeta remota a través de FTP.
 */
public class FTPSyncApplication {
    private static final String SERVIDOR_FTP = "127.0.0.1";  // Dirección del servidor FTP
    private static final String USUARIO = "Raul";  // Nombre de usuario para la autenticación en el servidor FTP
    private static final String CONTRASENA = "1234";  // Contraseña para la autenticación en el servidor FTP
    private static final String CARPETA_LOCAL = "C:\\Users\\Raúl\\OneDrive\\Escritorio\\Hola";  // Ruta de la carpeta local
    private static final String CARPETA_REMOTA = "Hola";  // Ruta de la carpeta remota en el servidor FTP
    private static final long TIEMPO_REFRESCO = 15000; // Intervalo de sincronización en milisegundos (15 segundos)

    /**
     * Método principal que inicia la sincronización entre las carpetas local y remota.
     * @param args Argumentos de línea de comandos (no utilizado).
     */
    public static void main(String[] args) {
        while (true) {
            try {
                sincronizar();  // Llama al método para sincronizar carpetas
                Thread.sleep(TIEMPO_REFRESCO);  // Espera el tiempo de refresco antes de la próxima sincronización
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método para sincronizar las carpetas local y remota.
     * @throws IOException Si ocurre un error de entrada/salida durante la sincronización.
     */
    private static void sincronizar() throws IOException {
        FTPClient clienteFTP = new FTPClient();  // Cliente FTP
        try {
            clienteFTP.connect(SERVIDOR_FTP);  // Conexión al servidor FTP
            clienteFTP.login(USUARIO, CONTRASENA);  // Autenticación en el servidor FTP
            clienteFTP.enterLocalPassiveMode();  // Modo de transferencia pasiva

            List<String> archivosRemotos = obtenerArchivosRemotos(clienteFTP, CARPETA_REMOTA);  // Lista de archivos remotos
            List<String> archivosLocales = obtenerArchivosLocales(CARPETA_LOCAL);  // Lista de archivos locales

            for (String archivoLocal : archivosLocales) {
                File localFile = new File(CARPETA_LOCAL + File.separator + archivoLocal);  // Archivo local actual
                long ultimaModificacionLocal = localFile.lastModified();  // Última modificación del archivo local

                if (!archivosRemotos.contains(archivoLocal) || estaArchivoActualizado(clienteFTP, CARPETA_REMOTA, archivoLocal, ultimaModificacionLocal)) {
                    añadirArchivo(clienteFTP, archivoLocal);  // Subir el archivo al servidor FTP si no existe o está actualizado
                }
            }

            for (String archivoRemoto : archivosRemotos) {
                if (!archivosLocales.contains(archivoRemoto)) {
                    borrarArchivo(clienteFTP, archivoRemoto);  // Borrar el archivo remoto si no existe en la carpeta local
                }
            }

        } finally {
            clienteFTP.disconnect();  // Desconectar del servidor FTP
        }
    }

    /**
     * Obtiene la lista de archivos en la carpeta remota del servidor FTP.
     * @param clienteFTP Cliente FTP conectado al servidor.
     * @param carpeta Ruta de la carpeta remota.
     * @return Lista de nombres de archivos remotos.
     * @throws IOException Si ocurre un error de entrada/salida durante la obtención de archivos remotos.
     */
    private static List<String> obtenerArchivosRemotos(FTPClient clienteFTP, String carpeta) throws IOException {
        List<String> archivos = new ArrayList<>();  // Lista de nombres de archivos remotos
        clienteFTP.changeWorkingDirectory(carpeta);  // Cambiar directorio remoto
        for (String nombreArchivo : clienteFTP.listNames()) {
            archivos.add(nombreArchivo);  // Agregar nombre de archivo a la lista
        }
        return archivos;
    }

    /**
     * Obtiene la lista de archivos en la carpeta local.
     * @param carpeta Ruta de la carpeta local.
     * @return Lista de nombres de archivos locales.
     */
    private static List<String> obtenerArchivosLocales(String carpeta) {
        List<String> archivos = new ArrayList<>();  // Lista de nombres de archivos locales
        File directorio = new File(carpeta);  // Carpeta local
        File[] listaArchivos = directorio.listFiles();  // Lista de archivos en la carpeta local
        if (listaArchivos != null) {
            for (File archivo : listaArchivos) {
                archivos.add(archivo.getName());  // Agregar nombre de archivo a la lista
            }
        }
        return archivos;
    }

    /**
     * Borra un archivo en el servidor FTP.
     * @param clienteFTP Cliente FTP conectado al servidor.
     * @param archivo Nombre del archivo remoto a borrar.
     * @throws IOException Si ocurre un error de entrada/salida durante la eliminación del archivo.
     */
    private static void borrarArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        clienteFTP.deleteFile(archivo);  // Eliminar el archivo remoto
    }

    /**
     * Sube un archivo al servidor FTP.
     * @param clienteFTP Cliente FTP conectado al servidor.
     * @param archivo Nombre del archivo local a subir.
     * @throws IOException Si ocurre un error de entrada/salida durante la subida del archivo.
     */
    private static void añadirArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        File localFile = new File(CARPETA_LOCAL + File.separator + archivo);  // Archivo local
        FileInputStream fis = new FileInputStream(localFile);  // Stream de entrada para el archivo local
        clienteFTP.storeFile(archivo, fis);  // Subir el archivo al servidor FTP
        fis.close();  // Cerrar el stream de entrada
    }

    /**
     * Verifica si un archivo en la carpeta remota está actualizado en comparación con su versión local.
     * @param clienteFTP Cliente FTP conectado al servidor.
     * @param carpetaRemota Ruta de la carpeta remota.
     * @param nombreArchivo Nombre del archivo remoto.
     * @param ultimaModificacionLocal Última modificación del archivo local.
     * @return true si el archivo remoto está actualizado, false en caso contrario.
     * @throws IOException Si ocurre un error de entrada/salida durante la obtención de información del archivo remoto.
     */
    private static boolean estaArchivoActualizado(FTPClient clienteFTP, String carpetaRemota, String nombreArchivo, long ultimaModificacionLocal) throws IOException {
        clienteFTP.changeWorkingDirectory(carpetaRemota);  // Cambiar directorio remoto
        FTPFile[] archivosRemotos = clienteFTP.listFiles();  // Obtener lista de archivos remotos

        for (FTPFile archivoRemoto : archivosRemotos) {
            if (archivoRemoto.getName().equals(nombreArchivo)) {
                long ultimaModificacionRemota = archivoRemoto.getTimestamp().getTimeInMillis();  // Última modificación del archivo remoto
                return ultimaModificacionLocal > ultimaModificacionRemota;  // Verificar si el archivo local está más actualizado que el remoto
            }
        }

        return false;  // El archivo remoto no está actualizado
    }
}
