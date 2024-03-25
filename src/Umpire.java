import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Umpire {
    public int id;

    public Umpire(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Umpire{" +
                "id=" + id +
                '}';
    }
}