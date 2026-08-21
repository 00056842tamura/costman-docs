package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BS アプリケーションのエントリポイント（サンプル）。
 *
 * <p>実プロダクト作成時はパッケージを {@code jp.co.example.{product}.bs} 等に変更してください。</p>
 */
@SpringBootApplication
public class Application {

    /**
     * アプリケーションを起動します。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
