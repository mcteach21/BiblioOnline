package mchou.apps.biblio;

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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainGActivity extends Activity {
	public static final String BOOK_DETAIL_KEY = "book";
	private Button go;
	private ProgressBar progress;
	private ListView list;
	private BookGAdapter adapter;
	private EditText edt_search;
	private Spinner filter_options;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setContent();
		constraintAnimate();
	}
	private void constraintAnimate() {
		LinearLayout layout = findViewById(R.id.search_layout);

		AnimatorSet animatorSet = new AnimatorSet();
		ValueAnimator fadeAnim = ObjectAnimator.ofFloat(layout, "alpha", 0f, 1f);
		fadeAnim.setDuration(1000);
		fadeAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {

			}
		});

		animatorSet.play(fadeAnim);
		animatorSet.start();
	}
	private void setContent() {
		list = findViewById(R.id.list);

		ArrayList<BookG> items = new ArrayList<BookG>();
		adapter = new BookGAdapter(this, items); //new ArrayAdapter<Book>(this, android.R.layout.simple_list_item_1, items);
		list.setAdapter(adapter);

		setupBookSelectedListener();

		progress = findViewById(R.id.progress);
		progress.setVisibility(View.GONE);

		go = findViewById(R.id.go);
		go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CallAsyncLooper();

				ConstraintLayout root = findViewById(R.id.root);
				ConstraintSet finishingConstraintSet= new ConstraintSet();
				finishingConstraintSet.clone(MainGActivity.this, R.layout.activity_main_final);

				TransitionManager.beginDelayedTransition(root);
				finishingConstraintSet.applyTo(root);
			}
		});
		
		edt_search = findViewById(R.id.edt_search);
		filter_options = findViewById(R.id.filter_options);
	}
	private void setupBookSelectedListener() {
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Launch the detail view passing book as an extra
				Intent intent = new Intent(MainGActivity.this, BookGDetailActivity.class);
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
	class BookGAdapter extends ArrayAdapter<BookG> {
		    public BookGAdapter(Context context, ArrayList<BookG> aBooks) {
		        super(context, 0, aBooks);
		    }


		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {
		        final BookG book = getItem(position);
		        ViewHolder viewHolder;
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

		        viewHolder.tvTitle.setText(book.getTitle());
		        viewHolder.tvAuthor.setText(book.getAuthors());

		        Picasso.with(getContext()).load(Uri.parse(book.getCoverUrl())).error(R.drawable.ic_nocover).into(viewHolder.ivCover);

		        return convertView;
		    }
		}

	private void CallAsyncLooper() {
		Handler mainHandler = new Handler(Looper.getMainLooper());
		Runnable myRunnable = ()->asyncGoogleFetchItems();
		mainHandler.post(myRunnable);
	}
	/**
	 * Google Api (Books)
	 */

	private void asyncGoogleFetchItems() {
		//Toast.makeText(MainGActivity.this, "asyncGoogleFetchItems..", Toast.LENGTH_SHORT).show();

		MyHttpGoogleClient client = new MyHttpGoogleClient();
		client.getItems(edt_search.getText().toString(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					//Toast.makeText(MainGActivity.this, "asyncGoogleFetchItems - onSuccess.."+response, Toast.LENGTH_SHORT).show();
					progress.setVisibility(ProgressBar.VISIBLE);
					JSONArray docs = null;
					if(response != null) {
						docs = response.getJSONArray("items");
						final ArrayList<BookG> books = BookG.fromJson(docs);
						//Toast.makeText(MainGActivity.this, "Success - books : "+books.size(), Toast.LENGTH_SHORT).show();

						progress.setMax(books.size());
						int i=1;
						adapter.clear();
						for (BookG book : books) {
							progress.setProgress(i++);

							/*try {
								Thread.sleep(100);
							} catch (InterruptedException e) {}*/

							Log.i("Async","book : "+book);
							adapter.add(book); // add book through the adapter

						}
						adapter.notifyDataSetChanged();
					}
					else {
						Toast.makeText(MainGActivity.this, "Success - Response Null !", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				progress.setVisibility(ProgressBar.GONE);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Toast.makeText(MainGActivity.this, "asyncGoogleFetchItems - onFailure..", Toast.LENGTH_SHORT).show();
				progress.setVisibility(ProgressBar.GONE);
			}
		});
	}
	class MyHttpGoogleClient {
		private static final String API_BASE_URL = "https://www.googleapis.com/books/v1/volumes";
		private AsyncHttpClient client;

		public MyHttpGoogleClient() {
			this.client = new AsyncHttpClient();
		}

		private String getApiUrl(String relativeUrl) {
			return API_BASE_URL + relativeUrl;
		}

		// Method for accessing the search API
		public void getItems(final String query, JsonHttpResponseHandler handler) {
			try {
				String url = getApiUrl("?q=");
				String filter = URLEncoder.encode(query, "utf-8");

				String search_url;
				switch (filter_options.getSelectedItemPosition()){
					case 0:
						search_url = url + "inauthor:"+filter;
						break;
					case 1:
						search_url = url + "intitle:"+filter;
						break;
					default:
						search_url = url +filter;
				}
				Log.i("Async","search_url : "+search_url);
				client.get(search_url, handler);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		/*public void getExtraBookDetails(String openLibraryId, JsonHttpResponseHandler handler) {
			String url = getApiUrl("books/");
			client.get(url + openLibraryId + ".json", handler);
		}*/
	}
}
