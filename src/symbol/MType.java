package symbol;

/**
 * @author yuchanghong
 * Created on 2020/3/7
 */
public class MType {
    private String name;
    private String type;
    private int row;
    private int col;

    public MType(String type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String insertVar(MVar var) {
        return null;
    }

    public String insertParam(MVar param) {
        return null;
    }

    public String insertClass(MClass myClass) {
        return null;
    }

    public String insertMethod(MMethod method) {
        return null;
    }
}
