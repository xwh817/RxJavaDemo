package xwh.demo.rxjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class RetrofitActivity extends AppCompatActivity {

	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_retrofit);
		mTextView = this.findViewById(R.id.text);
	}

	private void log(Object log) {
		mTextView.append( "\n" + log);
	}

	class User{
		String name;
		String address;
		int age;

		@Override
		public String toString() {
			return name + ",  " + age;
		}
	}

	interface UserUtil{
		@GET("getData")
		Call<JsonObject> getAll();


		@GET("getData")
		Observable<JsonObject> getAllWithReJava();
	}

	public void get(View view){
		String baseUrl = "http://rap2api.taobao.org/app/mock/162333/";
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		UserUtil userUtil = retrofit.create(UserUtil.class);
		Call<JsonObject> call = userUtil.getAll();

		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				JsonArray jsonArray = response.body().getAsJsonArray("data");
				for(int i=0; i<jsonArray.size(); i++) {
					User user = new Gson().fromJson(jsonArray.get(i), User.class);
					log(user);
				}

			}

			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				t.printStackTrace();
				log("onFailure: " + t.getMessage());
			}
		});
	}


	public void getWithRxJava(View view) {
		String baseUrl = "http://rap2api.taobao.org/app/mock/162333/";
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.build();

		UserUtil userUtil = retrofit.create(UserUtil.class);

		userUtil.getAllWithReJava()     // 调用接口相当于一个事件源
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new DisposableObserver<JsonObject>() {
					@Override
					public void onComplete() {
					}
					@Override
					public void onError(Throwable e) {
					}
					@Override
					public void onNext(JsonObject movieSubject) {
						JsonArray jsonArray = movieSubject.getAsJsonArray("data");
						for(int i=0; i<jsonArray.size(); i++) {
							User user = new Gson().fromJson(jsonArray.get(i), User.class);
							log(user);
						}
					}
				});

	}
}
