import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
/*Создать JavaFX приложение в окне которого есть:
поле для ввода текста
кнопка - search. При нажатии очищаются предыдущие результаты поиска(если есть) и загружаются новые в асинхронном потоке.
Программа должна использовать YouTube API для отображения результатов поиска видео.

В результатах должно содержаться:
Название видео
Название канала
Дата публикации
Кнопка - View. При нажатии на которую воспроизводиться видео в окне программы.
В результатах поиска добавляется 5-ый элемент - изображение из видео (thumbnails)
*/
import java.io.IOException;

public class Task1 extends Application {

    private static String URL = "http://www.youtube.com/embed/";
    private static String AUTO_PLAY = "?autoplay=1";

    public static void main(String[] args) throws UnirestException {
        initApplication();
        launch(args);
    }


    private static void initApplication() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Pane root = new Pane();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        final TextField putText = new TextField();
        putText.setTranslateX(100);
        putText.setTranslateY(60);

        final Button searchVideo = new Button("Search");
        searchVideo.setTranslateX(100);
        searchVideo.setTranslateY(10);

        root.getChildren().addAll(searchVideo, putText);

        searchVideo.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                try {
                    getActivity(root, putText.getText());
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
            }
        });

        primaryStage.setHeight(500);
        primaryStage.setWidth(800);
        primaryStage.show();
    }

    private void getActivity(final Pane root, String searchVideo) throws UnirestException {

       HttpResponse<ActivityResponse> response = Unirest.get("https://www.googleapis.com/youtube/v3/search")
                .queryString("type", "video")
                .queryString("q", searchVideo)
                .queryString("maxResults", 1)
               .queryString("part", "snippet")
                .queryString("key", "AIzaSyAoJtBYFKQT3rF5Qy0P0hHEFdAMDnOXOhA")
               .asObject(ActivityResponse.class);


        ActivityResponse activity = response.getBody();

        for(int i = 0; i < activity.items.size(); i++) {

        final Activity item = activity.items.get(i);
        if (item.snippet == null) continue;

        final TextField nameVideo = new TextField(item.snippet.title);
        nameVideo.setTranslateX(300);
        nameVideo.setTranslateY(50);

        final TextField nameChanel = new TextField(item.snippet.channelTitle);
        nameChanel.setTranslateX(300);
        nameChanel.setTranslateY(100);

        final TextField dateVideo = new TextField(item.snippet.publishedAt);
        dateVideo.setTranslateX(300);
        dateVideo.setTranslateY(150);


        String url1 = item.snippet.thumbnails.medium.url;
        ImageView image = new ImageView(url1);
        image.setTranslateX(300);
        image.setTranslateY(250);


        Button button = new Button("View");
        button.setTranslateX(300);
        button.setTranslateY(200);
        root.getChildren().addAll(button,nameChanel,nameVideo,dateVideo,image);

        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                String videoId = item.id.videoId;
                System.out.println(videoId);

                final WebView webview = new WebView();
                webview.getEngine().load(
                        URL+videoId+AUTO_PLAY
                );
                webview.setPrefSize(640, 390);

                final Button close = new Button("close");
                close.setTranslateX(webview.getWidth() + 10);
                close.setTranslateY(10);
                close.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        root.getChildren().remove(close);
                        root.getChildren().remove(webview);
                    }
                });
                root.getChildren().addAll(webview, close);
            }
        });
        }
    }
}
