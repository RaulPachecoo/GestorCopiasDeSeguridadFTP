package gestorcopiasdeseguridadftp;

import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Esta clase proporciona funcionalidades para subir archivos a un servidor FTP.
 */
class FTPUploader {
    private final String server;  // Dirección del servidor FTP
    private final int port;  // Puerto del servidor FTP
    private final String user;  // Nombre de usuario para la autenticación en el servidor FTP
    private final String password;  // Contraseña para la autenticación en el servidor FTP
    private FTPClient ftp;  // Cliente FTP

    /**
     * Constructor de la clase FTPUploader.
     * @param server La dirección del servidor FTP.
     * @param port El puerto del servidor FTP.
     * @param user El nombre de usuario para la autenticación en el servidor FTP.
     * @param password La contraseña para la autenticación en el servidor FTP.
     */
    public FTPUploader(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        ftp = new FTPClient();  // Inicialización del cliente FTP
    }

    /**
     * Sube un archivo al servidor FTP.
     * @param localFile La ruta del archivo local a subir.
     * @throws IOException Si ocurre un error de entrada/salida durante la subida del archivo.
     */
    public void uploadFile(String localFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(localFile)) {
            ftp.connect(server, port);  // Conexión al servidor FTP
            ftp.login(user, password);  // Autenticación en el servidor FTP
            ftp.enterLocalPassiveMode();  // Modo de transferencia pasiva
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);  // Establecer el tipo de archivo
            boolean done = ftp.storeFile(new File(localFile).getName(), inputStream);  // Subir el archivo
            if (done) {
                System.out.println("El archivo se ha subido correctamente al servidor FTP.");
            } else {
                System.out.println("Ha ocurrido un error al subir el archivo al servidor FTP.");
            }
        }
    }

    /**
     * Desconecta el cliente FTP del servidor.
     * @throws IOException Si ocurre un error de entrada/salida durante la desconexión.
     */
    public void disconnect() throws IOException {
        if (ftp.isConnected()) {
            ftp.logout();  // Cerrar sesión en el servidor FTP
            ftp.disconnect();  // Desconectar del servidor FTP
        }
    }
}
