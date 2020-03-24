package symbol;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author yuchanghong
 * Created on 2020/3/7
 */
public class MClass extends MIdentifier {
    private String parent;
    public HashMap<String, MMethod> methodList = new HashMap<>();
    public HashMap<String, MVar> varList = new HashMap<>();

    public MClass(String name, String parent, int row, int col) {
        super(name, "Class", row, col);
        this.setParent(parent);
    }

    private void setParent(String parent) {
        this.parent = parent;
    }

    public String getParent() {
        return this.parent;
    }

    /**
     * 向类中加入一个方法 使用方法名检查是否重复定义
     * @param method 待加入的方法
     * @return 如果可以插入就返回null 否则返回错误信息（方法重复定义）
     */
    public String insertMethod(MMethod method) {
        if (methodList.isEmpty() || methodList.get(method.getName()) == null) {
            methodList.put(method.getName(), method);
            return null;
        } else {
            return "已经在类:" + this.getName() + "中定义了方法:" + method.getFullName();
        }
    }

    /**
     * 功能：在方法的varList中插入一个变量
     * 实现：把varList定义为HashMap，使用参数名作为key方便检查重复定义
     * @param var 要插入的变量
     * @return 没有重复就返回null 有重复就返回错误信息
     */
    public String insertVar(MVar var) {
        String key = var.getName();
        if (varList.isEmpty()) {
            varList.put(key, var);
            return null;
        } else {
            if (varList.get(key) == null) {
                varList.put(key, var);
                return null;
            } else {
                return "已在类" + this.getName() + "定义了变量" + var.getName();
            }
        }
    }
}
