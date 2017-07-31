package youtube.downloader;

import com.github.axet.vget.VGet;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemContentDetails;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;




public class YouTubeDownloader
{
		private static final String youtube_baseURL = "https://www.youtube.com/watch?v=";
		private static final String youtubeinmp3_baseURL = "http://www.youtubeinmp3.com/fetch/?video="+youtube_baseURL;
		
		private static YouTube youtube;
	
		/** Application name. */
	    private static final String APPLICATION_NAME = "youtube-java-test";

	    /** Directory to store user credentials for this application. */
	    private static final java.io.File DATA_STORE_DIR = new java.io.File(
	    System.getProperty("user.home"), ".credentials/youtube-java-test");

	    /** Global instance of the {@link FileDataStoreFactory}. */
	    private static FileDataStoreFactory DATA_STORE_FACTORY;

	    /** Global instance of the JSON factory. */
	    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	    /** Global instance of the HTTP transport. */
	    private static HttpTransport HTTP_TRANSPORT;

	    /** Global instance of the scopes required by this quickstart.
	     *
	     * If modifying these scopes, delete your previously saved credentials
	     * at ~/.credentials/drive-java-quickstart
	     */
	    private static final Collection<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube");

	    static {
	        try {
	            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
	        } catch (Throwable t) {
	            t.printStackTrace();
	            System.exit(1);
	        }
	    }
	    
	    /**
	     * Creates an authorized Credential object.
	     * @return an authorized Credential object.
	     * @throws IOException
	     */
	    public static Credential authorize() throws IOException 
	    {
	        // Load client secrets.
	        InputStream in = YouTubeDownloader.class.getResourceAsStream("/youtube/downloader/resources/clients_secrets.json");
	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader( in ));

	        // Build flow and trigger user authorization request.
	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	            .setDataStoreFactory(DATA_STORE_FACTORY)
	            .setAccessType("offline")
	            .build();
	        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
	        return credential;
	    }

	    /**
	     * Build and return an authorized API client service, such as a YouTube
	     * Data API client service.
	     * @return an authorized API client service
	     * @throws IOException
	     */
	    public static YouTube getYouTubeService() throws IOException {
	        Credential credential = authorize();
	        return new YouTube.Builder(
	        HTTP_TRANSPORT, JSON_FACTORY, credential)
	            .setApplicationName(APPLICATION_NAME)
	            .build();
	    }
	
	
	
	
	public static void main(String[] args)
	{
        try {
            // This object is used to make YouTube Data API requests.
            youtube = getYouTubeService();
            Scanner reader = new Scanner(System.in);
            int choice=0;
            //while(choice != 1 || choice != 2){
                System.out.println("Welcome !"
                		+ "\nPlease enter your selection:"
                		+ "\n1.Download Playlist"
                		+ "\n2.Download Channel\n");
                choice = reader.nextInt();
            //}
            List<String[]> videosList = new ArrayList<>();
            switch(choice){
            case 1:
                System.out.println("Please enter the playlist id:");
                String playListId = reader.next();
                videosList = getPlayList(playListId, null);
                break;
            case 2:
                System.out.println("Please enter the channel username:");
                String userName = reader.next();
                videosList = getChannel(userName);
                break;
            }
            
            List<String[]> missingVideos = new ArrayList<>();
            //String playListId = "PLF0bEoBlLvohYe_fceNw4SecrhlaHuB9l";
            System.out.println(videosList.size() +" videos to download");
            for(String[] video : videosList)
            {    	
            	try{
	            	String title = video[0];
	            	String videoId = video[1];
	            	String downloadUrl = youtubeinmp3_baseURL+videoId;
	            	
	            	//file exist ?
	            	String existingFilename = "c:/temp/"+title+".mp3";
	            	File f = new File(existingFilename);
	            	if(f.exists()){
	            		System.out.println("File "+existingFilename+" already exists");
	            		continue;
	            	}
	            	
	            	System.out.println("downloading : "+title + " located at "+downloadUrl);
	            	//System.out.println("downloading : "+title + " located at "+youtube_baseURL+videoId);
	            	
	            	downloadVideo(video, missingVideos);
	            	
            	}
            	catch (IOException e) {
                    System.err.println("IOException: " + e.getMessage());
                    e.printStackTrace();
                }
                
//            	Object o = conn.getContent();
//            	String contentType = conn.getContentType();
//            	System.out.println(contentType);
            	
            	
            	//URL url = new URL(downloadUrl);
            	//FileUtils.copyURLToFile(url, new File("c:/temp/"+title+".mp3"));
            }
            System.out.println("Could not download the following videos:\n");
            for(String[] video:missingVideos){
            	System.out.println(video[0]+" at "+video[1]);
            }
            
        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode()
                    + " : " + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }

	}
	
	
	private static List<String[]> getPlayList(String playlistID, String nextToken) throws Exception
	{
		List<String[]> videosList = new ArrayList<>();
		HashMap<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet,contentDetails");
        parameters.put("maxResults", "50");
        parameters.put("playlistId", playlistID);

        YouTube.PlaylistItems.List playlistItemsListByPlaylistIdRequest = youtube.playlistItems().list(parameters.get("part").toString());
        if (parameters.containsKey("maxResults")) {
            playlistItemsListByPlaylistIdRequest.setMaxResults(Long.parseLong(parameters.get("maxResults").toString()));
        }

        if (parameters.containsKey("playlistId") && parameters.get("playlistId") != "") {
            playlistItemsListByPlaylistIdRequest.setPlaylistId(parameters.get("playlistId").toString());
        }
        
        if(nextToken !=null && !nextToken.isEmpty()){
            playlistItemsListByPlaylistIdRequest.setPageToken(nextToken);
        }

        PlaylistItemListResponse response = playlistItemsListByPlaylistIdRequest.execute();
        
        List<PlaylistItem> playlistitems = response.getItems();
        for(PlaylistItem item : playlistitems)
        {
        	PlaylistItemSnippet snippet = item.getSnippet();
        	String title = snippet.getTitle();
        	PlaylistItemContentDetails content = item.getContentDetails();
        	String videoId = content.getVideoId();
        	String[] video = new String[2];
        	video[0] = title;
        	video[1] = videoId;
        	videosList.add(video);
        }
        String nextPageToken = response.getNextPageToken();
        if(nextPageToken != null && !nextPageToken.isEmpty()){
        	videosList.addAll(getPlayList(playlistID, nextPageToken));
        }
        
        return videosList;
	}

	
	private static List<String[]> getChannel(String userName) throws Exception
	{
		String uploads = "";
		HashMap<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet,contentDetails,statistics");
        parameters.put("forUsername", userName);

        YouTube.Channels.List channelsListByUsernameRequest = youtube.channels().list(parameters.get("part").toString());
        if (parameters.containsKey("forUsername") && parameters.get("forUsername") != "") {
            channelsListByUsernameRequest.setForUsername(parameters.get("forUsername").toString());
        }

        ChannelListResponse response = channelsListByUsernameRequest.execute();
        List<Channel> items = response.getItems();
        for(Channel c: items){
        	ChannelContentDetails content = c.getContentDetails();
        	uploads = content.getRelatedPlaylists().getUploads();
        }
        
        return getPlayList(uploads, null);
	}

	private static void downloadVideo(String[] video, List<String[]> missingVideos) throws Exception
	{
		String title = video[0];
    	String videoId = video[1];
    	String downloadUrl = youtubeinmp3_baseURL+videoId;
    	
		URL website = new URL(downloadUrl);
    	HttpURLConnection httpConn = (HttpURLConnection) website.openConnection();
    	
    	
        String disposition = httpConn.getHeaderField("Content-Disposition");
        String contentType = httpConn.getContentType();
        int contentLength = httpConn.getContentLength();  
        String fileName = "";
        
        System.out.println("Content-Type = " + contentType);
        System.out.println("Content-Disposition = " + disposition);
        System.out.println("Content-Length = " + contentLength);
        
        if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 10,
                        disposition.length() - 1);
            }
        } else {
            // extracts file name from URL
            fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1,
            		downloadUrl.length());
        }
        
        
        System.out.println("fileName = " + fileName);
        
        if(contentType.equals("audio/mpeg"))
        {
        	 // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            //String saveFilePath = "c:/temp/"+ File.separator + fileName;
			String saveFilePath = "c:/temp/"+ fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
        }
        else{
        	missingVideos.add(video);
        }
    	
        httpConn.disconnect();
        System.out.println("Download Complete !");
        Thread.sleep(5000);
	}
	
	
//	private static void downloadVideo(String[] video, List<String[]> missingVideos) throws Exception
//	{
//		try{
//			String title = video[0];
//	    	String videoId = video[1];
//	    	
//	    	String downloadUrl = youtube_baseURL+videoId;
//	
//	    	String saveFilePath = "c:"+File.separator + "temp" + File.separator+ title + ".mp3";
//	    	
//			VGet v = new VGet(new URL(downloadUrl), new File(saveFilePath));
//	        v.download();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//			missingVideos.add(video);
//		}
//	}
}
