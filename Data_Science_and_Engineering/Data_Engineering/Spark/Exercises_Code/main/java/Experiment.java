import java.util.Arrays;
import java.util.List;

public class Experiment {
    public static void main(String[] args) {
        String s = "a b c d";
        String[] splitedS = s.split("\\s");
//        List<String> splitedS = Arrays.asList(s.split("\\s"));
        System.out.println(splitedS[1]);
    }
}
