package symbol;

/**
 * @author yuchanghong
 * Created on 2020/3/7
 */
public class MIdentifier extends MType {
    public MIdentifier(String name, String type, int row, int col) {
        super(type, row, col);
        setName(name);
    }

}
