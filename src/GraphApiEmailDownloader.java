import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;
import java.nio.charset.StandardCharsets;



public class GraphApiEmailDownloader {
	private static final String GRAPH_ENDPOINT = "https://graph.microsoft.com/v1.0";
    

    public static void downloadEmailAsEML(Email email, String accessToken,String savePath,String searchUser) throws IOException {
        String urlStr = GRAPH_ENDPOINT+  "/users/" + searchUser + "/messages/" + email.id + "/$value";
        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "message/rfc822");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorMsg = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            System.err.println("Failed to download email: " + errorMsg);
            return;
        }

        byte[] emlBytes = conn.getInputStream().readAllBytes();

        String safeSubject = sanitizeFilename(email.subject);
        String fromMail = sanitizeFilename(email.from);
        String receivedTime = email.receivedDateTime.replace(":", "-").replace("T", "_").replace("Z", "");
        String filename = safeSubject + "_" + fromMail + "_" + receivedTime + ".eml";
		
        if(savePath !=null) {
        	File dir = new File(savePath);
        	if (dir.canWrite()) {
        		filename = savePath + "\\" + filename;
        	}
		}
        
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(emlBytes);
            System.out.println("Saved email as " + filename);
        }
    }

    private static String sanitizeFilename(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

}

