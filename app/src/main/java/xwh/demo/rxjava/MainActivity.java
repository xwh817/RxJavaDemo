package xwh.demo.rxjava;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = this.findViewById(R.id.text);
	}

	// 观察者
	Observer<String> reader = new Observer<String>() {
		@Override
		public void onSubscribe(Disposable d) {
			log(Thread.currentThread().getName() + " onSubscribe");
		}

		@Override
		public void onNext(String value) {
			log(Thread.currentThread().getName() + " onNext:" + value);
		}

		@Override
		public void onError(Throwable e) {
			log("onError=" + e.getMessage());
		}

		@Override
		public void onComplete() {
			log(Thread.currentThread().getName() + " onComplete()");
		}
	};

	// 被观察者
	Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
		@Override
		public void subscribe(ObservableEmitter<String> emitter) throws Exception {
			emitter.onNext("步骤1 in " + Thread.currentThread().getName());
			Thread.sleep(1000);     // 任务在子线程里面执行
			emitter.onNext("步骤2 in " + Thread.currentThread().getName());
			Thread.sleep(1000);
			emitter.onNext("步骤3 in " + Thread.currentThread().getName());
			emitter.onComplete();
		}
	});

	public void test1(View view) {
		// 建立观察与被观察关系
		observable.subscribeOn(Schedulers.newThread())    // 在子线程订阅（执行）
				.observeOn(AndroidSchedulers.mainThread())  // 在主线程观察
				.subscribe(reader);

	}

	/**
	 * 链式任务
	 */
	public void linkTask(View view) {
		observable
				.create(emitter -> {     // 创建可观察任务
					emitter.onNext("步骤1 in " + Thread.currentThread().getName());
					Thread.sleep(1000);     // 任务在子线程里面执行
					emitter.onNext("步骤2 in " + Thread.currentThread().getName());
					Thread.sleep(1000);
					emitter.onComplete();
				})
				.subscribeOn(Schedulers.newThread())    // 在子线程订阅（执行）
				.observeOn(AndroidSchedulers.mainThread())  // 在主线程观察
				.subscribe(result -> {
					log("result:" + result);
				});     // 观察
	}

	public void gotoRetrofit(View view) {
		startActivity(new Intent(this, RetrofitActivity.class));
	}

	private void testJust() {
		// 被观察者
		Observable.just("one", "two", "three", "four", "five")
				.subscribeOn(Schedulers.newThread())    // 在子线程执行
				.observeOn(AndroidSchedulers.mainThread())  // 在主线程观察
				.subscribe(reader);

	}

	/**
	 * 有背压
	 */
	public void testBackpressure(View view) {
		Flowable.create(new FlowableOnSubscribe<String>() {
			@Override
			public void subscribe(FlowableEmitter<String> emitter) throws Exception {
				if (!emitter.isCancelled()) {
					for(int i=0;i<10;i++) {
						emitter.onNext("test" + (i+1));
					}
					emitter.onComplete();
				}
			}
		}, BackpressureStrategy.DROP)   // 多一个背压策略参数
				.subscribeOn(Schedulers.newThread())    // 在子线程订阅（执行）
				.observeOn(AndroidSchedulers.mainThread())  // 在主线程观察
				.subscribe(new Subscriber<String>() {
					Subscription mSubscription;
					@Override
					public void onSubscribe(Subscription s) {
						mSubscription = s;
						s.request(2);  // 注意这句代码，告诉发送端，接收端的初始处理能力。
					}

					@Override
					public void onNext(String re) {
						log("result:" + re);
						mSubscription.request(1);   // 相当于主动告知一下发送端（处理能力+1 每次request相当于告诉发送端我有了多少处理能力）
					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {

					}
				});
	}



	public void gotoOperator(View view) {
		startActivity(new Intent(this, OperatorActivity.class));
	}



	private void log(String log) {
		mTextView.append("\n" + log);
	}
}
