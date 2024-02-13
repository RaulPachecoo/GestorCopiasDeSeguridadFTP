package gestorcopiasdeseguridadftp;



import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class GestorCopiasDeSeguridadFTP {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            // Solicitar el nombre de la carpeta
            System.out.print("Ingrese el nombre de la carpeta a comprimir y subir al servidor FTP: ");
            String nombreCarpeta = sc.nextLine();

            // Comprimir la carpeta
            String nombreCarpetaZip = comprimirCarpeta(nombreCarpeta);

            // Subir el archivo comprimido al servidor FTP
            subirAFTP(nombreCarpetaZip);

            System.out.println("¡La operación se ha completado con éxito!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String comprimirCarpeta(String nombreCarpeta) throws IOException, InterruptedException {
        // Crear un nombre de archivo comprimido basado en la fecha y hora actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String nombreCarpetaZip = nombreCarpeta + "_" + dateFormat.format(new Date()) + ".zip";

        // Crear el proceso para comprimir la carpeta usando ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder("7z", "a", nombreCarpetaZip, nombreCarpeta);
        Process process = processBuilder.start();

        // Esperar a que el proceso de compresión termine
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // La compresión se realizó correctamente
            return nombreCarpetaZip;
        } else {
            // La compresión falló
            throw new IOException("La compresión de la carpeta falló con código de salida: " + exitCode);
        }
    }

    private static void subirAFTP(String nombreFicheroZip) {
        // Configurar la conexión FTP
        String server = "127.0.0.1";
        int port = 21;
        String username = "Raul";
        String password = "1234";

        try {
            // Conectar y autenticarse en el servidor FTP
            FTPUploader ftpUploader = new FTPUploader(server, port, username, password);
            ftpUploader.uploadFile(nombreFicheroZip);
            ftpUploader.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


