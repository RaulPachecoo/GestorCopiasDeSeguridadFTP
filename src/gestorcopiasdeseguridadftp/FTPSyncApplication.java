package gestorcopiasdeseguridadftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que proporciona métodos para sincronizar archivos y carpetas locales con un servidor FTP remoto.
 */

public class FTPSyncApplication {

    private static final String SERVIDOR_FTP = "127.0.0.1";
    private static final String USUARIO = "Raul";
    private static final String CONTRASENA = "1234";
    private static final String CARPETA_LOCAL = "C:\\Users\\Raúl\\OneDrive\\Escritorio\\Hola";
    private static final String CARPETA_REMOTA = "Hola";
    private static final long TIEMPO_REFRESCO = 15000; // 15 segundos

    public static void main(String[] args) throws IOException {
        while (true) {
            sincronizar();
            try {
                Thread.sleep(TIEMPO_REFRESCO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método que coordina la sincronización entre archivos y carpetas locales y remotos.
     *
     * @throws IOException Si ocurre un error de entrada/salida durante la sincronización.
     */
    private static void sincronizar() throws IOException {
        FTPClient clienteFTP = new FTPClient();
        clienteFTP.connect(SERVIDOR_FTP);
        clienteFTP.login(USUARIO, CONTRASENA);

        List<String> carpetasRemotas = obtenerCarpetasRemotas(clienteFTP, CARPETA_REMOTA);
        List<String> archivosRemotos = obtenerArchivosRemotos(clienteFTP, CARPETA_REMOTA);
        List<String> archivosLocales = obtenerArchivosLocales(CARPETA_LOCAL);
        List<String> carpetasLocales = obtenerCarpetasLocales(CARPETA_LOCAL);

        for (String carpetaRemota : carpetasRemotas) {
            if (!carpetasLocales.contains(carpetaRemota)) {
                borrarCarpeta(clienteFTP, carpetaRemota);
            }
        }

        for (String archivoRemoto : archivosRemotos) {
            if (!archivosLocales.contains(archivoRemoto)) {
                borrarArchivo(clienteFTP, archivoRemoto);
            }
        }

        for (String archivoLocal : archivosLocales) {
            if (!archivosRemotos.contains(archivoLocal) || estaArchivoActualizado(clienteFTP, CARPETA_REMOTA, archivoLocal, new File(CARPETA_LOCAL + File.separator + archivoLocal).lastModified())) {
                añadirArchivo(clienteFTP, archivoLocal);
            }
        }

        for (String carpetaLocal : carpetasLocales) {
            String rutaCarpetaLocal = CARPETA_LOCAL + File.separator + carpetaLocal;
            String rutaCarpetaRemota = CARPETA_REMOTA + "/" + carpetaLocal;

            if (!carpetasRemotas.contains(carpetaLocal)) {
                añadirCarpeta(clienteFTP, carpetaLocal);
            } else {
                // Verificar si la carpeta local existe remotamente
                if (!carpetasRemotas.contains(carpetaLocal)) {
                    añadirCarpeta(clienteFTP, carpetaLocal);
                } else {
                    // Verificar si la carpeta local está actualizada remotamente
                    long ultimaModificacionLocal = new File(rutaCarpetaLocal).lastModified();
                    if (estaCarpetaActualizada(clienteFTP, rutaCarpetaRemota, ultimaModificacionLocal)) {
                        // La carpeta local está actualizada, no hacer nada
                    } else {
                        // La carpeta local no está actualizada, sincronizar contenido
                        borrarCarpeta(clienteFTP, rutaCarpetaRemota); // Eliminar la carpeta remota antes de volver a crearla
                        añadirCarpeta(clienteFTP, carpetaLocal);
                    }
                }
            }
        }

        clienteFTP.disconnect();
    }

    /**
     * Obtiene la lista de nombres de archivos en la carpeta remota especificada.
     *
     * @param clienteFTP Cliente FTP conectado al servidor remoto.
     * @param carpeta    Carpeta remota de la que se obtendrán los archivos.
     * @return Lista de nombres de archivos en la carpeta remota.
     * @throws IOException Si ocurre un error de entrada/salida durante la obtención de archivos remotos.
     */
    private static List<String> obtenerArchivosRemotos(FTPClient clienteFTP, String carpeta) throws IOException {
        List<String> archivos = new ArrayList<>();
        clienteFTP.changeWorkingDirectory(carpeta);
        for (FTPFile archivo : clienteFTP.listFiles()) {
            if (archivo.isFile()) {
                archivos.add(archivo.getName());
            }
        }
        return archivos;
    }

    /**
     * Obtiene la lista de nombres de carpetas en la carpeta remota especificada.
     *
     * @param clienteFTP Cliente FTP conectado al servidor remoto.
     * @param carpeta    Carpeta remota de la que se obtendrán las carpetas.
     * @return Lista de nombres de carpetas en la carpeta remota.
     * @throws IOException Si ocurre un error de entrada/salida durante la obtención de carpetas remotas.
     */
    private static List<String> obtenerCarpetasRemotas(FTPClient clienteFTP, String carpeta) throws IOException {
        List<String> carpetas = new ArrayList<>();
        clienteFTP.changeWorkingDirectory(carpeta);
        for (FTPFile archivo : clienteFTP.listFiles()) {
            if (archivo.isDirectory()) {
                carpetas.add(archivo.getName());
            }
        }
        return carpetas;
    }


    /**
     * Obtiene la lista de nombres de archivos en la carpeta local especificada.
     *
     * @param carpeta Carpeta local de la que se obtendrán los archivos.
     * @return Lista de nombres de archivos en la carpeta local.
     */
    private static List<String> obtenerArchivosLocales(String carpeta) {
        List<String> archivos = new ArrayList<>();
        File directorio = new File(carpeta);
        File[] listaArchivos = directorio.listFiles();
        if (listaArchivos != null) {
            for (File archivo : listaArchivos) {
                if (archivo.isFile()) {
                    archivos.add(archivo.getName());
                }
            }
        }
        return archivos;
    }

    /**
     * Obtiene la lista de nombres de carpetas en la carpeta local especificada.
     *
     * @param carpeta Carpeta local de la que se obtendrán las carpetas.
     * @return Lista de nombres de carpetas en la carpeta local.
     */
    private static List<String> obtenerCarpetasLocales(String carpeta) {
        List<String> carpetas = new ArrayList<>();
        obtenerCarpetasLocalesRecursivo(new File(carpeta), carpetas, carpeta);
        return carpetas;
    }

    /**
     * Método auxiliar recursivo para obtener las carpetas locales.
     *
     * @param carpeta    Carpeta actual en el proceso recursivo.
     * @param carpetas   Lista de carpetas locales.
     * @param carpetaBase    Ruta base de la carpeta local.
     */
    private static void obtenerCarpetasLocalesRecursivo(File carpeta, List<String> carpetas, String carpetaBase) {
        File[] listaArchivos = carpeta.listFiles();
        if (listaArchivos != null) {
            for (File archivo : listaArchivos) {
                if (archivo.isDirectory()) {
                    carpetas.add(archivo.getAbsolutePath().replace(carpetaBase, "").substring(1)); // Añadir la ruta relativa de la carpeta
                    obtenerCarpetasLocalesRecursivo(archivo, carpetas, carpetaBase);
                }
            }
        }
    }

    // Métodos auxiliares para la manipulación de archivos y carpetas en el servidor remoto...
    private static void borrarArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        clienteFTP.deleteFile(archivo);
        System.out.println("Archivo " + archivo + " eliminado");
    }

    private static void borrarCarpeta(FTPClient clienteFTP, String carpeta) throws IOException {
        FTPFile[] archivos = clienteFTP.listFiles(carpeta);
        if (archivos != null && archivos.length > 0) {
            for (FTPFile archivo : archivos) {
                String nombreArchivo = archivo.getName();
                String rutaArchivo = carpeta + "/" + nombreArchivo;
                if (archivo.isDirectory()) {
                    borrarCarpeta(clienteFTP, rutaArchivo); // Eliminar subcarpetas recursivamente
                } else {
                    clienteFTP.deleteFile(rutaArchivo); // Eliminar archivos
                }
            }
        }
        // Después de eliminar todos los archivos y subcarpetas, intentar eliminar la carpeta principal
        clienteFTP.removeDirectory(carpeta);
        System.out.println("Carpeta " + carpeta + " eliminada");
    }

    private static void añadirArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        File localFile = new File(CARPETA_LOCAL + File.separator + archivo);
        FileInputStream fis = new FileInputStream(localFile);
        clienteFTP.storeFile(archivo, fis);
        fis.close();
        System.out.println("Archivo " + archivo + " subido");
    }

    private static void añadirCarpeta(FTPClient clienteFTP, String carpeta) throws IOException {
        clienteFTP.makeDirectory(carpeta); // Crear la carpeta remota
        // Obtener la lista de archivos dentro de la carpeta local
        List<String> archivosLocales = obtenerArchivosLocales(CARPETA_LOCAL + File.separator + carpeta);
        for (String archivo : archivosLocales) {
            añadirArchivo(clienteFTP, carpeta + "/" + archivo); // Subir cada archivo al servidor remoto dentro de la carpeta creada
        }
        System.out.println("Carpeta " + carpeta + " subida");
    }


    private static boolean estaArchivoActualizado(FTPClient clienteFTP, String carpetaRemota, String nombreArchivo, long ultimaModificacionLocal) throws IOException {
        clienteFTP.changeWorkingDirectory(carpetaRemota);
        FTPFile[] archivosRemotos = clienteFTP.listFiles();

        for (FTPFile archivoRemoto : archivosRemotos) {
            if (archivoRemoto.getName().equals(nombreArchivo)) {
                long ultimaModificacionRemota = archivoRemoto.getTimestamp().getTimeInMillis();
                return ultimaModificacionLocal > ultimaModificacionRemota;
            }
        }

        return false;
    }

    private static boolean estaCarpetaActualizada(FTPClient clienteFTP, String carpetaRemota, long ultimaModificacionLocal) throws IOException {
        clienteFTP.changeWorkingDirectory(carpetaRemota);
        FTPFile[] carpetasRemotas = clienteFTP.listDirectories();

        for (FTPFile carpeta : carpetasRemotas) {
            if (carpeta.getTimestamp().getTimeInMillis() > ultimaModificacionLocal) {
                return true;
            }
        }

        return false;
    }
}