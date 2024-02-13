package gestorcopiasdeseguridadftp;



import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GestorCopiasDeSeguridadFTP {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            // Solicitar el nombre de la carpeta
            System.out.print("Ingrese el nombre de la carpeta a comprimir y subir al servidor FTP: ");
            String nombreCarpeta = br.readLine();

            // Comprimir la carpeta
            String nombreCarpetaZip = comprimirCarpeta(nombreCarpeta);

            // Subir el archivo comprimido al servidor FTP
            subirAFTP(nombreCarpetaZip);

            System.out.println("¡La operación se ha completado con éxito!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String comprimirCarpeta(String nombreCarpeta) throws IOException {
        // Crear un nombre de archivo comprimido basado en la fecha y hora actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String nombreCarpetaZip = nombreCarpeta + "_" + dateFormat.format(new Date()) + ".zip";

        // Ejecutar el comando zip en un proceso secundario para comprimir la carpeta
        Process process = Runtime.getRuntime().exec("7z a " + nombreCarpetaZip + " " + nombreCarpeta);
        try {
            process.waitFor(); // Esperar a que termine el proceso de compresión
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return nombreCarpetaZip;
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


