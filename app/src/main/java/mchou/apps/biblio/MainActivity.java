package mchou.apps.biblio;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity {
	public static final String BOOK_DETAIL_KEY = "book";
	private Button go;
	private ProgressBar progress;
	private ListView list;
	private BookAdapter adapter;
	private EditText edt_search;
	private Spinner filter_options;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setContent();
		//constraintAnimate();

	}
	private void constraintAnimate() {
		//ImageView logo = findViewById(R.id.logo);

		LinearLayout layout = findViewById(R.id.search_layout);

		AnimatorSet animatorSet = new AnimatorSet();
		ValueAnimator fadeAnim = ObjectAnimator.ofFloat(layout, "alpha", 0f, 1f);
		fadeAnim.setDuration(1000);
		fadeAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				ConstraintLayout root = findViewById(R.id.root);

				ConstraintSet finishingConstraintSet= new ConstraintSet();
				finishingConstraintSet.clone(MainActivity.this, R.layout.activity_main_final);

				TransitionManager.beginDelayedTransition(root);
				finishingConstraintSet.applyTo(root);
			}
		});

		animatorSet.play(fadeAnim);
		animatorSet.start();

	}

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_biblio, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent=null;
		if (id == R.id.action_settings) {

		}else if (id == R.id.action_biblio) {
			intent = new Intent(getApplicationContext(), BiblioActivity.class);
		}
		else if (id == R.id.action_ws) {
			intent = new Intent(getApplicationContext(), WebServiceActivity.class);
		}
		if(intent!=null)
			startActivity(intent);
		return true;
	}*/
	private void setContent() {
		list = findViewById(R.id.list);

		ArrayList<Book> items = new ArrayList<Book>();
		adapter = new BookAdapter(this, items); //new ArrayAdapter<Book>(this, android.R.layout.simple_list_item_1, items);
		list.setAdapter(adapter);

		setupBookSelectedListener();

		progress = findViewById(R.id.progress);
		progress.setVisibility(View.GONE);

		go = findViewById(R.id.go);
		go.setOnClickListener((v) ->  {
			CallAsyncLooper();
			constraintAnimate();
		});
		
		edt_search = findViewById(R.id.edt_search);

		filter_options = findViewById(R.id.filter_options);
		filter_options.setVisibility(View.GONE);
	}
	private void setupBookSelectedListener() {
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
				intent.putExtra(BOOK_DETAIL_KEY, adapter.getItem(position));
				startActivity(intent);
			}
		});
	}

	private static class ViewHolder {
	   	// View lookup cache
	     public ImageView ivCover;
	     public TextView tvTitle;
	     public TextView tvAuthor;
	}
	class BookAdapter extends ArrayAdapter<Book> {
		    public BookAdapter(Context context, ArrayList<Book> aBooks) {
		        super(context, 0, aBooks);
		    }
			public void init() {
				super.clear();
			}
		    // Translates a particular `Book` given a position
		    // into a relevant row within an AdapterView
		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {
		        // Get the data item for this position
		        final Book book = getItem(position);
	//	        // Check if an existing view is being reused, otherwise inflate the view
		        ViewHolder viewHolder; // view lookup cache stored in tag
		        if (convertView == null) {
		            viewHolder = new ViewHolder();
		            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		            convertView = inflater.inflate(R.layout.item_book, parent, false);
		            viewHolder.ivCover = (ImageView)convertView.findViewById(R.id.ivBookCover);
		            viewHolder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
		            viewHolder.tvAuthor = (TextView)convertView.findViewById(R.id.tvAuthor);

		            convertView.setTag(viewHolder);
		        } else {
		            viewHolder = (ViewHolder) convertView.getTag();
		        }
		        // Populate the data into the template view using the data object
		        viewHolder.tvTitle.setText(book.getTitle());
		        viewHolder.tvAuthor.setText(book.getAuthor());

		        Picasso.with(getContext()).load(Uri.parse(book.getCoverUrl())).error(R.drawable.ic_nocover).into(viewHolder.ivCover);
		        // Return the completed view to render on screen
		        return convertView;
		    }
		}

	private void CallAsyncLooper() {
		Handler mainHandler = new Handler(Looper.getMainLooper());
		Runnable myRunnable = ()-> asyncFetchItems();
		mainHandler.post(myRunnable);
	}
	private void asyncFetchItems() {
		MyHttpClient client = new MyHttpClient();
		client.getItems(edt_search.getText().toString(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {

					progress.setVisibility(ProgressBar.VISIBLE);
					JSONArray docs = null;
					if(response != null) {

						docs = response.getJSONArray("docs");
						final ArrayList<Book> books = Book.fromJson(docs);
						Log.i("Async","Success - books : "+books.size());

						progress.setMax(books.size());
						int i=1;
						adapter.clear();
						for (Book book : books) {
							progress.setProgress(i++);

							//	                        	try {
								//									Thread.sleep(100);
							//								} catch (InterruptedException e) {}

							Log.i("Async","-"+book);
							adapter.add(book); // add book through the adapter

						}
						adapter.notifyDataSetChanged();
					}
					else {
						Log.i("Async","Success - Response Null !");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				progress.setVisibility(ProgressBar.GONE);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				progress.setVisibility(ProgressBar.GONE);
			}
		});
	}

	class MyHttpClient {
		private static final String API_BASE_URL = "http://openlibrary.org/";
		private AsyncHttpClient client;

		public MyHttpClient() {
			this.client = new AsyncHttpClient();
		}

		private String getApiUrl(String relativeUrl) {
			return API_BASE_URL + relativeUrl;
		}

		// Method for accessing the search API
		public void getItems(final String query, JsonHttpResponseHandler handler) {
			try {
				String url = getApiUrl("search.json?q=");

				Log.i("Async","url : "+url);

				client.get(url + URLEncoder.encode(query, "utf-8"), handler);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// Method for accessing books API to get publisher and no. of pages in a book.
		public void getExtraBookDetails(String openLibraryId, JsonHttpResponseHandler handler) {
			String url = getApiUrl("books/");
			client.get(url + openLibraryId + ".json", handler);
		}
	}


}
