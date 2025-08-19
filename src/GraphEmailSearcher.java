import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;

public class GraphEmailSearcher {

	
    private static final String GRAPH_ENDPOINT = "https://graph.microsoft.com/v1.0";
    private static int prevPageSize = 0 ;
    private static String nextLink;
    private static String prevSearchUser;
    private static String prevFilterQuery;
    
    
    private static String parseJsonForKey(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }
    
    private static String extractAllEmailAddress(String json,String fieldName) {
    	String address = "";
    	try {
    		int currentIndex = 0;
    		while (true) {
    			int fieldIndex = json.indexOf("\"" + fieldName + "\"",currentIndex);
    			if (fieldIndex == -1 ) break;
    			int addressIndex = json.indexOf("\"address\"",fieldIndex);
    			if (addressIndex == -1 ) break;   
    			int startQuote = json.indexOf("\"",addressIndex + 9);
    			if (startQuote == -1 ) break;   
    			int endQuote = json.indexOf("\"",startQuote + 1);
    			if (endQuote == -1 ) break; 
    			address = address + json.substring(startQuote + 1,endQuote) + ";";
    			currentIndex = endQuote + 1;
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	return address;
    }
    
    
    public static String buildFilterQuery(
            String subject,
            String body,
            String from,
            String toRecipients,
            String receivedStart,
            String receivedEnd
    ) {
        List<String> filters = new ArrayList<>();

        if (subject != null && !subject.isEmpty()) {
            filters.add("contains(subject, '" + escapeFilterValue(subject) + "')");
        }
        if (body != null && !body.isEmpty()) {
            filters.add("contains(body/content, '" + escapeFilterValue(body) + "')");
        }
        if (from != null && !from.isEmpty()) {
            filters.add("from/emailAddress/address eq '" + escapeFilterValue(from) + "'");
        }
        if (toRecipients != null && !toRecipients.isEmpty()) {
            filters.add("toRecipients/any(r: r/emailAddress/address eq '" + escapeFilterValue(toRecipients) + "')");
        }
        if (receivedStart != null && !receivedStart.isEmpty() && receivedEnd != null && !receivedEnd.isEmpty()) {
            filters.add("receivedDateTime ge " + receivedStart + " and receivedDateTime le " + receivedEnd);
        }

        if (filters.isEmpty()) {
            return "";
        } else {
            return "$filter=" + String.join(" and ", filters);
        }
    }

    private static String escapeFilterValue(String value) {
        return value.replace("'", "''");
    }


    public static List<Email> searchEmails(
            String accessToken,
            String searchUser,
            String filterQuery,
            int pageSize,
            int pageNumber
    ) throws IOException {
    	if (prevPageSize== pageSize &&  prevSearchUser.equals(searchUser)  &&  filterQuery.equals(prevFilterQuery) && nextLink != null) {
            URL url = new URL(nextLink);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Accept", "application/json");
            int responseCode = conn.getResponseCode();
    		String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8); 
            nextLink = parseEmailsNextLinkFromResponse(response);
            prevPageSize = pageSize;
            prevSearchUser = searchUser;
            prevFilterQuery = filterQuery;
            return parseEmailsFromResponse(response);
    	}
    	
        int skip = (pageNumber - 1) * pageSize;
        String urlStr = GRAPH_ENDPOINT+  "/users/"
        		+ searchUser
        		+"/messages?"
                + "$top=" + pageSize
                + "&$skip=" + skip
                + "&$select=subject,from,receivedDateTime,toRecipients,bodyPreview";
                //+ "&$orderby=receivedDateTime desc";

        if (filterQuery != null && !filterQuery.isEmpty()) {
            urlStr += "&" + filterQuery;
        }
        urlStr = urlStr.replace(" ","%20");

        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorMsg = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("Failed to fetch emails: " + errorMsg);
        }

        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        nextLink = parseEmailsNextLinkFromResponse(response);
        prevPageSize = pageSize;
        prevSearchUser = searchUser;
        prevFilterQuery = filterQuery;        
        return parseEmailsFromResponse(response);
    }
    
    
    public static List<Email> searchAllEmails(
            String accessToken,
            String searchUser,
            String filterQuery
    ) throws IOException {
        String urlStr = GRAPH_ENDPOINT + "/users/"
        		+ searchUser
        		+"/messages?"
                + "&$select=subject,from,receivedDateTime,toRecipients,bodyPreview";
                //+ "&$orderby=receivedDateTime desc";

        if (filterQuery != null && !filterQuery.isEmpty()) {
            urlStr += "&" + filterQuery;
        }
        
        urlStr = urlStr.replace(" ","%20");
        

        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorMsg = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("Failed to fetch emails: " + errorMsg);
        }
        List<Email> searchAllEmail = new ArrayList<>();
        List<Email> searchEmail = new ArrayList<>();
        
        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String nextLink = parseEmailsNextLinkFromResponse(response);
        if (nextLink != null) {
        	searchEmail = parseEmailsFromResponse(response);
        	searchAllEmail.addAll(searchEmail);
        	while(true) {
                url = new URL(nextLink);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Accept", "application/json");
                responseCode = conn.getResponseCode();
        		response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8); 
                nextLink = parseEmailsNextLinkFromResponse(response);
                searchEmail = parseEmailsFromResponse(response);
            	searchAllEmail.addAll(searchEmail);
                if(nextLink == null) {
                	break;
                }
        	}
        	
        }else {
        	return parseEmailsFromResponse(response);
        }
        return searchAllEmail;
    }
    

    public static List<Email> parseEmailsFromResponse(String json) {
        List<Email> emails = new ArrayList<>();
        String[] items = json.split("\\{\"@odata.etag\"");
        for (String item : items) {
            if (item.contains("subject") && item.contains("id")) {
                String subject = parseJsonForKey(item, "subject");
                String fromAddress = extractAllEmailAddress(item, "from");
                String toRecipientsAddress = extractAllEmailAddress(item, "toRecipients");
                String receivedDateTime = parseJsonForKey(item, "receivedDateTime");
                String bodyPreview = parseJsonForKey(item, "bodyPreview");
                String messageId = parseJsonForKey(item, "id");
                emails.add(new Email(messageId, subject, fromAddress, toRecipientsAddress, receivedDateTime, bodyPreview));
            }
        }
        return emails;
    }
    
    
    private static String parseEmailsNextLinkFromResponse(String json) {
        String[] items = json.split("\"@odata.nextLink\":");
        try {
            String nextLink = items[1].toString().substring(1,items[1].toString().length()-2);
        }
        catch(ArrayIndexOutOfBoundsException e){
        	nextLink = null;
        }
        return nextLink;
    }
   
    
}
