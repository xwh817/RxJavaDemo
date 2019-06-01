package xwh.demo.rxjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class OperatorActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);
        mTextView = this.findViewById(R.id.text);

        test1();
        test2();
        test3();
    }


    /**
     * 操作符
     * debounce, 用指定时间间隔进行限流，等收不到数据之后，隔一段时间才把最后一个数据往下发。
     * throttleFirst，取间隔时间后的数据往下发，debounce是事件停止后的最后一个。
     * filter,对收到的数据进行过滤，如果不满足条件就不向下发送。
     *
     */

    ObservableEmitter<String> textEmitter = null;
    private void test1(){

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                textEmitter = emitter;
            }
        })
        .filter(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                boolean isOk = s.length() < 8;
                if (!isOk) {
                    log("超过了长度限制，不发送。");
                }
                return isOk;
            }
        })
        .debounce(1, TimeUnit.SECONDS)   // 停止输入后1秒才把最后结果发出。
        //.throttleFirst(1, TimeUnit.SECONDS)     // 限流，只取时间间隔内的第一个数据
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                log("accept: " + s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                log("onError: " + throwable.getMessage());
            }
        });


        EditText input = this.findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textEmitter != null && !textEmitter.isDisposed()) {
                    textEmitter.onNext(s.toString());
                }
            }
        });
    }


    /**
     * 操作符interval
     */
    private void test2() {
        final Button btTimer = this.findViewById(R.id.bt_timer);
        btTimer.setOnClickListener(v -> {

            long max = 5;
            Observable.interval(1, 1, TimeUnit.SECONDS)
                    .take(max)
                    .map(i -> max-i-1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    btTimer.setEnabled(false);
                    btTimer.setText("" + max);
                }

                @Override
                public void onNext(Long aLong) {
                    btTimer.setText("" + aLong);
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    btTimer.setEnabled(true);
                    btTimer.setText("倒计时");
                }
            });
        });


    }


    /**
     * 操作符
     * merge：合并两个Observable数据源序列，可能互相交错
     * concat：有序合并，等前一个事件序列结束之后再下一个
     */
    private void test3() {
        Button btTimer = this.findViewById(R.id.bt_merge);
        btTimer.setOnClickListener(v -> {
            //Observable<String> observable1 = Observable.just("a", "b", "c");
            //Observable<String> observable2 = Observable.just("1", "2");

            Observable<String> observable1 = Observable.interval(1, TimeUnit.SECONDS).take(3).map(l -> ""+l);

            Observable<String> observable2 = Observable.interval(1,1, TimeUnit.SECONDS).take(3).map(l -> (char)('a' + l)+"");

            // 另一种写法：observable1.concatWith(observable2)
            // 合并两个Observable
            Observable.concat(observable1, observable2)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            log(s);
                        }
                    });
        });
    }





    private void log(String log) {
        mTextView.append("\n" + log);
    }
}
