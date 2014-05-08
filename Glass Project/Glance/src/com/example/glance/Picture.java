package com.example.glance;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;


////////////////////////////////////Picture////////////////////////////////////
public class Picture extends Activity {
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final String UPLOAD_OCR = "http://ec2-54-186-34-253.us-west-2.compute.amazonaws.com/upload_ocr.php"; //to upload the image and get the Words
	public static final String DICTIONARY = "http://ec2-54-186-34-253.us-west-2.compute.amazonaws.com/dictionary.php"; //to send the Words and get the Meanings
	public static final String WORD_IMAGE = "http://ec2-54-186-34-253.us-west-2.compute.amazonaws.com/word_image.php"; //to get the first Image from Google --NOT IMPLEMENTED
	
    // "http://ec2-54-186-34-253.us-west-2.compute.amazonaws.com/Sprintz.html"   //not implemented
	
    //Screens in glass are named Cards. When we get the response from the server we will put each of them in one Card
    //This variable will have cards we need based on how many words we have
	public static List<Card> mCards;
	
    //Words that will be sent to the server
	List<String>words = new ArrayList<String>();
    
    //Response for the words sent to the server
	List<String>meanings = new ArrayList<String>();
    
    //This is probably not doing anything
	private String current;
    
    
	Bitmap bitmap;
	
	private Uri fileUri;
    
    
	private CardScrollView mCardScrollView;
	
	
	@Override
	//*****************************onCreate()*****************************
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //Removes the app name to save space on the screen
		setContentView(R.layout.activity_picture);
	}
	
	@Override
	//*****************************onKeyDown()*****************************
    public boolean onKeyDown(int keycode, KeyEvent event) 
	{
        //If the user touch the center of the "touchpad" a picture will be taken
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) 
        { takePicture(); return true; }
        
        return false;
    }
	
	//*****************************takePicture()*****************************
    //I dont have much idea of what is going on here
    //What seems to me is that just declaring the intent and starting it would be enough
	private void takePicture() 
	{
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    File file = new File(Environment.getExternalStorageDirectory(),"test.jpg");
	    fileUri = Uri.fromFile(file);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	    
	    startActivityForResult(intent, MEDIA_TYPE_IMAGE);
	    
	    bitmap = (Bitmap) intent.getExtras().get("data"); //I believe this was a try to send the image to the server
	    
	    //The picture is not being sent to the server, So they are hardcoded to pretend the behavior
        //The method we supposed to call to get these words is:  getWords();
        words.add("hackathon");
	    words.add("basketball");
	    words.add("glass");
	    words.add("baseball");
	    words.add("football");
        
	    getMeanings();
	}
	
	//*****************************initiate()*****************************
    //This method is being called in the getMeanings() 
    //It is calling the method that will create the cards and is making the cards scrollable
    //I realized that maybe this could be done in just one method
	private void initiate()
	{	
		
		createCards();
		
		mCardScrollView = new CardScrollView(this);
        CardScroll cs = new CardScroll();
        mCardScrollView.setAdapter(cs);
        mCardScrollView.activate();
        setContentView(mCardScrollView);
	}

	//*****************************createCards()*****************************
    //This method creates the cards based on how many words we have
	private void createCards()
	{
		mCards = new ArrayList<Card>();
		Card card;
		
		for(int i=0; i<words.size(); i++)
		{
			current = words.get(i);
			card = new Card(getApplicationContext());
			card.setText(words.get(i).toString()); //setText will set the "Title", which is the Word we want the meaning
			card.setFootnote(meanings.get(i).toString()); //setFootnote will be above the "Title", this is the meaning. This footnote is too short (1 line) maybe we need to change the font or whatever to make the meanings fit the screen
			mCards.add(card);
		}
	}
	
	//*****************************getWords()*****************************
    //NOT WORKING ---- NOT WORKING ---- NOT WORKING ---- NOT WORKING ---- NOT WORKING
    
	//This method should send image and receive string array
    //Couldn't find a way to send an image to the server
    //There's an example of how to communicate with the server in the getMeanings()
	public void getWords()
	{
		HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(UPLOAD_OCR);
	
	    try 
	    {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("image", ""));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	
	        HttpResponse res = httpclient.execute(httppost);
	        
	        InputStream in = res.getEntity().getContent();
	        BufferedReader bfrd = new BufferedReader(new InputStreamReader(in),1024);
	        String line;
	        while((line = bfrd.readLine()) != null)
	        { words.add(line.toString()); }
	        
	    } catch (ClientProtocolException e) {
	    	
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	}

	//*****************************getMeanings()*****************************
	//It sends word to the server, the server sends the request to the dictionary API and retunrs the meaning
    //This method and LongOperation class were made with help from one of the Spritz' engineers, 
    //we were in a rush to deliver the project so I couldn't ask much what was going on, thus I can't explain much
	public void getMeanings() 
	{
		//Sending requests are time consuming, anything that uses more than 5 seconds in Android should be done in a separate thread
        LongOperation op = new LongOperation() {
			protected void onPostExecute(java.util.List<String> result) {
				Picture.this.meanings = result;
				initiate(); //Now that we have the meanings we can create the cards
			};
		};
		
		op.execute(words);
	} 

    
    
    ////////////////////////////////////LongOperation////////////////////////////////////
    //This class and getMeanings() were made with help from one of the Spritz' engineers, 
    //we were in a rush to deliver the project so I couldn't ask much what was going on, thus I can't explain much
    
    //AsyncTask in a nut shell:
    //  It is Thread to execute time consuming operations, 
    //  doInBackground() is where the magic happens
    //  onPreExecute() is what happens Before the execution --not implemented
    //  onPostExecute() is what happens After the execution -- implemented anonymously in the getMeanings()
    //  It takes 3 args, #1 Argument, #2 is to show progress, #3 Return type
	private class LongOperation extends AsyncTask<List<String>, Void, List<String>>
	{

		@Override
        //*****************************doInBackground()*****************************
		protected List<String> doInBackground(List<String>... arg0) 
        {
            List<String> meanings2 = new ArrayList<String>(); //This will be the return
			for(String word : arg0[0])
            {
				try 
		        {
		    		HttpClient httpclient = new DefaultHttpClient();
		            HttpPost httppost = new HttpPost(DICTIONARY); //DICTIONARY is the variable that contains the website that will receive the word and gives the meaning back
	
		            try 
		            {
		                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(); //The serve is waiting a key-value request, that's why we are using this
		                nameValuePairs.add(new BasicNameValuePair("word", word));
		                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	
		                // Execute HTTP Post Request
		                HttpResponse res = httpclient.execute(httppost);
		                HttpEntity entity = res.getEntity();
		                
		                meanings2.add( EntityUtils.toString(entity) );  //Translates the server's response to String
	
		            } catch (ClientProtocolException e) { } 
                      catch (IOException e) { }
		        } catch (Exception e) { e.printStackTrace(); }
			}
			return meanings2;
		}
		
	}
	
	
	////////////////////////////////////CardScroll////////////////////////////////////
    //This whole class was take from the official website, I didn't touch anything
	private class CardScroll extends CardScrollAdapter {

		
		@Override
        public int findIdPosition(Object id) 
        {
            return -1;
        }

        @Override
        public int findItemPosition(Object item) 
        {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() 
        {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) 
        {
            return mCards.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            return mCards.get(position).toView();
        }
    }
}
