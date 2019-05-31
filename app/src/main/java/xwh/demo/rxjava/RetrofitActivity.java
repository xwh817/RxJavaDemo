package xwh.demo.rxjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

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

	/**
	 * 和接口的json数据对应，
	 * 那么就可以直接用Gson或FastJson转成Java数据了。
	 * 不用自己去解析json字符串。
	 */
	class UserList{
		int code;
		List<User> data;
	}

	interface UserUtil{
		@GET("getData")
		Call<UserList> getAll();


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
		Call<UserList> call = userUtil.getAll();

		call.enqueue(new Callback<UserList>() {
			@Override
			public void onResponse(Call<UserList> call, Response<UserList> response) {
				UserList list = response.body();
				for(User user : list.data) {
					log(user);
				}
			}

			@Override
			public void onFailure(Call<UserList> call, Throwable t) {
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
				.subscribeOn(Schedulers.io())   // 设置事件源在io线程上发生
				.map(jsonObject ->  new Gson().fromJson(jsonObject, UserList.class))
				.map(userList -> userList.data)
				/*.map(userList -> Observable.fromIterable(userList.data).filter(item -> item.age >= 30))
				.subscribe(userObservable ->  userObservable
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(user -> log(user))
				)*/
				.observeOn(AndroidSchedulers.mainThread())  // 设置观察者线程
				.subscribe(new DisposableObserver<List<User>>() {
					@Override
					public void onComplete() {
					}
					@Override
					public void onError(Throwable e) {
						log(e.getMessage());
					}
					@Override
					public void onNext(List<User> list) {
						for(User user : list) {
							log(user);
						}
					}
				});

	}
}
