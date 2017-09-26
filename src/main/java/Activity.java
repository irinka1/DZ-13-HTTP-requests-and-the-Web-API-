
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)

public class Activity {
    public Snippet snippet;
    public ID1 id;
}