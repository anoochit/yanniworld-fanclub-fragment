package net.redlinesoft.app.yannifanclub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import net.redlinesoft.app.yannifanclub.R;

public class FragmentTwitter extends SherlockFragment {

	Activity activity;

	int data_size = 0;
	private ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
	private HashMap<String, String> map;
	ListView listItem;

	String url = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20json%20where%20url%3D%22http%3A%2F%2Fsearch.twitter.com%2Fsearch.json%3Fq%3Dyanni%22%20%20&format=json&callback=";

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		return super.getSherlockActivity();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_twitter, container,
				false);
		Log.d("VLOG", "onCreateView");

		// get xml data form yql
		if (checkNetworkStatus()) {
			new LoadContentAsync().execute();
		} else {
			Toast.makeText(activity.getBaseContext(), "No network connection!",
					Toast.LENGTH_SHORT).show();
		}

		return view;
	}

	@Override
	public void onAttach(Activity a) {
		// TODO Auto-generated method stub
		super.onAttach(a);
		activity = a;

	}

	public class LoadContentAsync extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// load json data
			try {

				JSONObject json_data = new JSONObject(getJSONUrl(url));
				JSONObject json_query = json_data.getJSONObject("query");
				JSONObject json_results = json_query.getJSONObject("results");
				JSONObject json_json_result = json_results
						.getJSONObject("json");
				JSONArray json_result = json_json_result
						.getJSONArray("results");
				Log.d("JSONW", String.valueOf(json_result.length()));

				for (int i = 0; i < json_result.length(); i++) {
					// parse json
					JSONObject c = json_result.getJSONObject(i);
					Log.d("JSONW", c.getString("profile_image_url").toString());
					Log.d("JSONW", c.getString("text").toString());

					// put into hashmap
					map = new HashMap<String, String>();
					map.put("image_url", c.getString("profile_image_url")
							.toString());
					map.put("title", c.getString("text").toString());
					MyArrList.add(map);
				}

				data_size = json_result.length();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("JSONW", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			ShowResult(MyArrList);
			activity.setProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			MyArrList.clear();
			activity.setProgressBarIndeterminateVisibility(true);
		}
	}

	public String getJSONUrl(String url) {
		StringBuilder str = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				// Download OK
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					str.append(line);
				}
			} else {
				Log.e("Log", "Failed to download file..");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str.toString();
	}

	public void ShowResult(ArrayList<HashMap<String, String>> myArrList) {
		try {
			listItem = (ListView) activity.findViewById(R.id.listItemTwitter);
			LazyTwitterAdapter adapter = new LazyTwitterAdapter(activity,
					myArrList);
			listItem.setAdapter(adapter);

		} catch (Exception e) {
			Toast.makeText(activity.getBaseContext(),
					"Cannot connect to twitter server!", Toast.LENGTH_SHORT)
					.show();

		}

	}

	public boolean checkNetworkStatus() {
		ConnectivityManager connectivityManager = (ConnectivityManager) activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

}
