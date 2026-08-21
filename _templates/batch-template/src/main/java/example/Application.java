package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * バッチアプリケーションのエントリポイント（サンプル）。
 *
 * <p>実プロダクト作成時はパッケージを {@code jp.co.example.{product}.batch} 等に変更してください。</p>
 *
 * <p>実際のバッチ処理は {@code CommandLineRunner} または {@code ApplicationRunner}
 * を実装したクラスを {@code @Component} として登録してください。</p>
 */
@SpringBootApplication
public class Application {

    /**
     * アプリケーションを起動します。
     *
     * @param args コマンドライン引数（例: {@code --job=jobnet1.job1}）
     */
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(Application.class, args)));
    }
}
