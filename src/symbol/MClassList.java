package symbol;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author yuchanghong
 * Created on 2020/3/7
 */
public class MClassList extends MType {
    public HashMap<String, MClass> classList;

    public MClassList() {
        super("classList", 0, 0);
        classList = new HashMap<>();
    }

    public String insertClass(MClass myClass) {
        if (classList.isEmpty() || classList.get(myClass.getName()) == null) {
            classList.put(myClass.getName(), myClass);
            return null;
        } else {
            return "重复定义类" + myClass.getName();
        }
    }
}
