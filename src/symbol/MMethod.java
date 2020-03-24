package symbol;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author yuchanghong
 * Created on 2020/3/7
 */
public class MMethod extends MIdentifier {
    private String returnType;
    public String paramString = ""; //参数列表字符串化 用于检查方法的覆盖
    public HashMap<String, MVar> varList = new HashMap<>();
    public HashMap<String, MVar> paramList = new HashMap<>();
    //保存每个param的type 用于方法调用时检查形参和实惨是否一致 不能直接用字符串检查因为要考虑到上溯造型
    public ArrayList<String> paramArray = new ArrayList<>();


    public MClass myThis;

    public MMethod(String name, String returnType, int row, int col, MClass myThis) {
        super(name, "Method", row, col);
        setReturnType(returnType);
        this.myThis = myThis;
    }

    private void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getFullName() {
        return returnType + '_' + getName();
    }

    public String getParamString() {
        return paramString;
    }

    /**
     * 功能：方法的paramList中加入一个参数
     * 实现：paramList定义为一个HashMap，使用参数名作为key方便检查重复定义
     *      同时需要构造paramString用于检查方法参数列表
     * @param param 待插入的参数
     * @return 如果参数没有重复返回null 否则返回错误信息
     */
    public String insertParam(MVar param) {
        String key = param.getName();
        if (paramList.isEmpty() || paramList.get(key) == null) {
            if (paramList.isEmpty()) {
                paramString += param.getType();
            } else {
                paramString += ',';
                paramString += param.getType();
            }
            paramList.put(key, param);
            paramArray.add(param.getType());
            return null;
        } else {
            return "已在方法：" + this.getName() + "声明了参数：" + param.getName();
        }
    }

    /**
     * 构造paramString的方法 用于检查参数列表
     * @param param 参数 只有类型有效
     */
    public void insertParamByType(MVar param) {
        if (paramList.isEmpty()) {
            paramString += param.getType();
        } else {
            paramString += ',';
            paramString += param.getType();
        }
    }

    /**
     * 功能：在方法的varList中插入一个变量
     * 实现：把varList定义为HashMap，使用参数名作为key方便检查重复定义
     *      这里在检查重复定义时 需要检查变量是否和参数列表发生了冲突
     * @param var 插入的变量
     * @return 如果没有重复定义就返回null 否则返回错误信息
     */
    public String insertVar(MVar var) {
        String key = var.getName();
        if (paramList.get(key) == null && varList.get(key) == null) {
            varList.put(key, var);
            return null;
        } else {
            if (paramList.get(key) != null) {
                return "参数列表中定义了变量" + var.getName();
            } else {
                return "方法中已经定义了变量" + var.getName();
            }
        }
    }
}
