package com.gcp;

import java.net.URL;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
// For Http Client
/*
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
*/

// Imports the Google Cloud client library
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;

public class WorkloadIdentityFederation {

    // Define Variables used in the project
    public static String url = "<Okta Web App Domain>/v1/token";
    public  static String clientid = "<Okta App Client ID>";
    public  static String clientsecret = "<Okta App Client Secret>";
    public  static String projectId = "<GCP Project ID>";

    public static void main(String[] args) {

        // Set Env Var Below
        // GOOGLE_APPLICATION_CREDENTIALS=GOOGLE_APPLICATION_CREDENTIALS=src/main/java/com/gcp/client-config.json

        // Instantiate the Class and call GetOktaToken() & listBuckets() methods
        try {
            // Logic Fetched from https://github.com/googleapis/google-auth-library-java/blob/main/README.md
            GoogleCredentials googleCredentials = GoogleCredentials.getApplicationDefault();

            WorkloadIdentityFederation widf = new WorkloadIdentityFederation();
            widf.GetOktaToken();
            widf.listBuckets(projectId,googleCredentials);

        }
        catch (Exception ex) {
            System.out.println("Error:  " + ex.getMessage());
        }
    }

    public static void GetOktaToken() {
        try {
            //Example Request Body for Oauth2.0
            /*
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(tokenURL)).headers("Accept", "application/json", "Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString("grant_type=client_credentials&client_id=" + clientID + "&client_secret=" + clientSecret)).build();
            */

            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            String authorization = "";
            authorization = clientid + ":" + clientsecret;
            byte[] encodedBytes;
            encodedBytes = Base64.getEncoder().encode(authorization.getBytes());
            String jsonInputString = "grant_type=client_credentials";
            conn.setRequestProperty("Authorization", "Basic " + new String(encodedBytes));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Accept", "*/*");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);

            /**
            Example Using Http Client
            If using Intellij https://stackoverflow.com/questions/52340914/intellij-cant-find-java-net-http-when-compiling-with-java-11
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + new String(encodedBytes))
                    .header("User-Agent", USER_AGENT)
                    .header("Cache-Control", "no-cache")
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                    .build();
            HttpResponse<String> responsehttp = client.send(request, HttpResponse.BodyHandlers.ofString());
             final int httpresponseCode = responsehttp.statusCode();
            **/

            OutputStream os = conn.getOutputStream();
            os.write(postData);

            final int responseCode = conn.getResponseCode();
            // Debug Post Request
            //InputStream responseBody = conn.getErrorStream();

            // Write the Oauth Token file Out
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("/tmp/okta-tokenjv.json")));

                //write contents of StringBuffer to a file
                bwr.write(response.toString());

                //flush the stream
                bwr.flush();

                //close the stream
                bwr.close();

                System.out.println("JSON Object Successfully written to the file!!");
            }
        } catch (Exception ex) {
            System.out.println("Error:  " + ex.getMessage());
        }

    }
    public static void listBuckets(String projectId, Credentials credential) {
        // The ID of your GCP project
        // String projectId = "your-project-id";

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(credential).build().getService();
        Page<Bucket> buckets = storage.list();

        for (Bucket bucket : buckets.iterateAll()) {
            System.out.println(bucket.getName());
        }
    }
}
