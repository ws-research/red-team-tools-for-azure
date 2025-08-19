import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;
import java.net.URLEncoder;

public class GraphApiTokenGenerator {


    public static String getAccessToken(String TENANT_ID,String CLIENT_ID,String CLIENT_SECRET ) throws IOException {
    	
    	String tokenEndpoint = "https://login.microsoftonline.com/" + TENANT_ID + "/oauth2/v2.0/token";

        String params = "client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
                + "&scope=" + URLEncoder.encode("https://graph.microsoft.com/.default", "UTF-8")
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8")
                + "&grant_type=client_credentials";

        URL url = new URL(tokenEndpoint);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        String jsonResponse = response.toString();
        String token = null;

        String tokenKey = "\"access_token\":\"";
        int startIndex = jsonResponse.indexOf(tokenKey);
        if (startIndex != -1) {
            startIndex += tokenKey.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex != -1) {
                token = jsonResponse.substring(startIndex, endIndex);
            }
        }
        return token;
    }
}
