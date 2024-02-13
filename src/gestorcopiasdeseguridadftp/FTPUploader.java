package gestorcopiasdeseguridadftp;
import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class FTPUploader {
    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private FTPClient ftp;

    public FTPUploader(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        ftp = new FTPClient();
    }

    public void uploadFile(String localFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(localFile)) {
            ftp.connect(server, port);
            ftp.login(user, password);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            boolean done = ftp.storeFile(new File(localFile).getName(), inputStream);
            if (done) {
                System.out.println("El archivo se ha subido correctamente al servidor FTP.");
            } else {
                System.out.println("Ha ocurrido un error al subir el archivo al servidor FTP.");
            }
        }
    }

    public void disconnect() throws IOException {
        if (ftp.isConnected()) {
            ftp.logout();
            ftp.disconnect();
        }
    }
}
