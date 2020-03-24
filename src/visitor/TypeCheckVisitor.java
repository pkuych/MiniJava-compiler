package visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import javax.sound.midi.MidiChannel;

import com.sun.xml.internal.ws.encoding.MtomCodec;

import sun.security.util.AuthResources_es;
import sun.util.resources.ms.CalendarData_ms_MY;
import symbol.MClass;
import symbol.MClassList;
import symbol.MIdentifier;
import symbol.MMethod;
import symbol.MType;
import symbol.MVar;
import syntaxtree.AllocationExpression;
import syntaxtree.AndExpression;
import syntaxtree.ArrayAllocationExpression;
import syntaxtree.ArrayAssignmentStatement;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.ArrayType;
import syntaxtree.AssignmentStatement;
import syntaxtree.Block;
import syntaxtree.BooleanType;
import syntaxtree.BracketExpression;
import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.CompareExpression;
import syntaxtree.Expression;
import syntaxtree.ExpressionList;
import syntaxtree.ExpressionRest;
import syntaxtree.FalseLiteral;
import syntaxtree.FormalParameter;
import syntaxtree.FormalParameterList;
import syntaxtree.FormalParameterRest;
import syntaxtree.Goal;
import syntaxtree.Identifier;
import syntaxtree.IfStatement;
import syntaxtree.IntegerLiteral;
import syntaxtree.IntegerType;
import syntaxtree.MainClass;
import syntaxtree.MessageSend;
import syntaxtree.MethodDeclaration;
import syntaxtree.MinusExpression;
import syntaxtree.NotExpression;
import syntaxtree.PlusExpression;
import syntaxtree.PrimaryExpression;
import syntaxtree.PrintStatement;
import syntaxtree.Statement;
import syntaxtree.ThisExpression;
import syntaxtree.TimesExpression;
import syntaxtree.TrueLiteral;
import syntaxtree.Type;
import syntaxtree.TypeDeclaration;
import syntaxtree.VarDeclaration;
import syntaxtree.WhileStatement;
import typecheck.ErrorPrinter;

/**
 * @author yuchanghong
 * Created on 2020/3/7
 */

public class TypeCheckVisitor extends GJDepthFirst<MType, MType> {

    private MClassList allClassList = null;

    private String paramList = "";
    private ArrayList<String> paramArray = new ArrayList<>();

    /**
     * 检查函数的重载
     * 遍历符号表
     * 对于每一个方法（方法所在的类不会存在同名方法） 去父类中查找是否存在同名方法
     * 1 存在同名方法 检查返回类型 参数列表是否一致 完全一致则是覆盖 不需要处理
     * 2 存在同名方法 检查返回类型 参数列表不完全一致 是重载 要报错
     * 3 不存在同名方法 无需处理
     * 结束
     */
    public void CheckMethodOverriding() {
        String msg = "不允许方法的重载";
        for (Map.Entry<String, MClass> current : allClassList.classList.entrySet()) {
            MClass currentClass = current.getValue();
            if (current.getValue().getParent() != null) {
                MClass parentClass = allClassList.classList.get(currentClass.getParent());
                for (Map.Entry<String, MMethod> method : current.getValue().methodList.entrySet()) {

                    String methodName = method.getKey();
                    MMethod currentMethod = method.getValue();
                    while (parentClass != null) {
                        MMethod parentMethod = parentClass.methodList.get(methodName);
                        if (parentMethod != null) { //父类中有这个方法的定义
                            if (parentMethod.getReturnType().equals(currentMethod.getReturnType())
                                    && parentMethod.getParamString().equals(currentMethod.getParamString())) {
                                break;
                            } else {
                                ErrorPrinter.print(msg + currentMethod.getName(), currentMethod.getRow(), currentMethod.getCol());
                                return;
                            }
                        } else { //去父类继续找
                            if (parentClass.getParent() != null) {
                                parentClass = allClassList.classList.get(parentClass.getParent());
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * 检查类的循环继承  顺便检查了继承了未定义类的情况
     * 先初始化一个空的Set用于存放已经遍历过的类
     * 对于没有遍历过的类
     * 如果没有继承关系，直接结束
     * 如果有继承关系，就把这个类加入一个Set中并去查找它父类的继承关系，知道查找到循环继承结束
     * 输出循环定义检查的结果信息 如果正确返回null 否则返回错误的类名
     */
    public void CheckClassExtends() {
        HashSet<String> checked = new HashSet<>();
        String res = null;
        for (Map.Entry<String, MClass> myClass : allClassList.classList.entrySet()) {
            if (!checked.contains(myClass.getKey())) {
                checked.add(myClass.getKey());
                MClass currentClass = myClass.getValue();
                if (currentClass.getParent() == null) {
                    continue;
                } else {
                    HashSet<String> extend = new HashSet<>();
                    while (true) {
                        extend.add(currentClass.getName());
                        String name = currentClass.getName();
                        currentClass = allClassList.classList.get(currentClass.getParent());
                        if (currentClass == null) {
                            res += name + "继承了未定义的类 ";
                            break;
                        }
                        checked.add(currentClass.getName());
                        if (currentClass.getParent() == null) {
                            break;
                        }
                        if (extend.contains(currentClass.getParent())) {
                            res += "类循环继承:" + currentClass.getName();
                            break;
                        }
                    }
                }
            }
        }

        if (res != null) {
            ErrorPrinter.print(res, 0, 0);
        }
    }

    /**
     * 直接在符号表中查找是否有这个type的定义
     * 如果没有则报错并直接结束 使用了未定义的类型一般会有其他空指针问题 这里直接结束了
     */
    public void CheckTypeDeclared(MType type) {
        String msg = null;
        HashSet<String> types = new HashSet<String>() {
            {
                add("Integer");
                add("Boolean");
                add("Array");
            }
        };

        if (allClassList.classList.get(type.getType()) == null && !types.contains(type.getType())) {
            msg = "使用了未定义的类型" + type.getType();
            System.exit(0);
        }
        if (msg != null) {
            ErrorPrinter.print(msg, type.getRow(), type.getCol());
        }
    }

    /**
     * 功能：检查变量是否定义过
     * 实现：如果是一个类（主类）就直接查找查找
     *      如果是一个方法 1.就现在方法中查找变量（包括参数列表和变量列表）
     *                   2.去类中查找
     *                   3.去父类中查找
     * @param argu 类或者方法 ***不能修改***
     * @param id 待检查的变量
     * @return 检查无误返回true 有错误返回false
     */
    public boolean CheckVarDeclared(MType argu, MIdentifier id) {
        MType classOrMethod = null;
        String msg = "使用了未定义的变量";
        String type = null;
        if (argu.getType().equals("Class")) {
            // in MainClass
            if (((MClass)argu).varList.get(id.getName()) == null) {
                ErrorPrinter.print(msg, id.getRow(), id.getCol());
                return false;
            } else {
                type = ((MClass)argu).varList.get(id.getName()).getType();
                id.setType(type);
                return true;
            }
        } else if (argu.getType().equals("Method")){
            classOrMethod = argu;
            if (((MMethod) classOrMethod).varList.get(id.getName()) != null) {
                type = ((MMethod)classOrMethod).varList.get(id.getName()).getType();
                id.setType(type);
                return true;
            } else if (((MMethod)classOrMethod).paramList.get(id.getName()) != null) {
                type = ((MMethod)classOrMethod).paramList.get(id.getName()).getType();
                id.setType(type);
                return true;
            } else if (((MMethod)classOrMethod).myThis.varList.get(id.getName()) != null) {
                type = ((MMethod)classOrMethod).myThis.varList.get(id.getName()).getType();
                id.setType(type);
                return true;
            } else {
                classOrMethod = ((MMethod)classOrMethod).myThis;
                while (((MClass) classOrMethod).getParent() != null) {
                    classOrMethod = allClassList.classList.get(((MClass) classOrMethod).getParent());
                    if (((MClass)classOrMethod).varList.get(id.getName()) != null) {
                        type = ((MClass)classOrMethod).varList.get((id.getName())).getType();
                        id.setType(type);
                        return true;
                    }
                }

                ErrorPrinter.print(msg + id.getName(), id.getRow(), id.getCol());
                return false;
            }
        }
        return true;
    }

    /**
     * 检查exp的类型是不是type 这里需要考虑上溯造型 也就是exp可以是type的一个子类
     */
    public void CheckExpEqual(MType exp, String type, String msg) {
        HashSet<String> types = new HashSet<String>() {
            {
                add("Integer");
                add("Boolean");
                add("Array");
            }
        };
        if (!exp.getType().equals(type)) {
            if (!types.contains(exp.getType())) {
                MClass currentClass = allClassList.classList.get(exp.getType());
                if (currentClass != null) {
                    while (currentClass.getParent() != null) {
                        if (currentClass.getParent().equals(type)) {
                            return;
                        } else {
                            currentClass = allClassList.classList.get(currentClass.getParent());
                        }
                    }
                }
            }
            ErrorPrinter.print(msg, exp.getRow(), exp.getCol());
        }
    }

    /**
     * 检查exp的类型是不是type 这里需要考虑上溯造型 也就是exp可以是type的一个子类
     */
    public boolean CheckExpEqual(String sonType, String type) {
        HashSet<String> types = new HashSet<String>() {
            {
                add("Integer");
                add("Boolean");
                add("Array");
            }
        };
        if (!sonType.equals(type)) {
            if (!types.contains(sonType)) {
                MClass currentClass = allClassList.classList.get(sonType);
                while (currentClass.getParent() != null) {
                    if (currentClass.getParent().equals(type)) {
                        return true;
                    } else {
                        currentClass = allClassList.classList.get(currentClass.getParent());
                    }
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 检查类中是否包含这个方法的定义
     * 直接在类的方法map中查找method的name
     * 如果找不到就去其父类中查找
     *
     * 注意minijava中允许方法的重载 所以如果返回值和方法名一致的话 参数列表必须一致 否则会算作重载
     * @param myClass 定义方法的类
     * @param method 待检查的方法
     */
    public void CheckMethodDeclared(MClass myClass, MMethod method) {
        String res = null;
        MClass currentClass = myClass;
        String name = currentClass.getName();
        MMethod _method = currentClass.methodList.get(method.getName());
//        if (method.getName().equals("Change")) {
//            System.out.println(_method.paramArray.toString());
//            System.out.println(paramArray.toString());
//            System.out.println(_method.paramArray.equals(paramArray));
//        }
        while (true) {
            if (_method != null) {
                if (paramArray.size() != _method.paramArray.size()) {
                    res = "参数列表不匹配 定义的参数列表" + _method.paramArray.toString() + " 调用的参数列表" + paramArray.toString();
                }
                if (!_method.paramArray.equals(paramArray)) {
                    for (int i = 0; i < paramArray.size(); i++) {
                        if (!CheckExpEqual(paramArray.get(i), _method.paramArray.get(i))) {
                            res = "参数列表不匹配 定义的参数列表" + _method.paramArray.toString() + " 调用的参数列表" + paramArray.toString();
                            break;
                        }
                    }
                    break;
                } else {
                    res = null;
                    break;
                }
            } else {
                if (currentClass.getParent() != null) {
                    currentClass = allClassList.classList.get(currentClass.getParent());
                    _method = currentClass.methodList.get(method.getName());
                } else {
                    res = name + "类及其父类中没有定义方法" + method.getFullName();
                    break;
                }
            }
        }

        paramArray.clear();
        if (res != null) {
            ErrorPrinter.print(res, method.getRow(), method.getCol());
        }
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public MType visit(Goal n, MType argu) {
        MType _ret=null;
        allClassList = (MClassList)argu;
        CheckClassExtends();
        CheckMethodOverriding();
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     *
     * Statement需要当前累的指针 可以在符号表中查询当前类名获得指针
     */
    public MType visit(MainClass n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f1.accept(this, argu);
        MClass mainClass = allClassList.classList.get(id.getName());
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, mainClass);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public MType visit(TypeDeclaration n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     *
     * MethodDeclaration中需要用到其所在的类,要在这里把当前类的指针传给f6
     * 类的指针可以通过在符号表中查找类名获得
     */
    public MType visit(ClassDeclaration n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f1.accept(this, argu);
        MClass currentClass = allClassList.classList.get(id.getName());
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, currentClass);
        n.f5.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     *
     * MethodDeclaration中需要用到其所在的类,要在这里把当前类的指针传给f6
     * 类的指针可以通过在符号表中查找类名获得
     */
    public MType visit(ClassExtendsDeclaration n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f1.accept(this, argu);
        MClass currentClass = allClassList.classList.get(id.getName());
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, currentClass);
        n.f7.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     *
     * 检查是否使用了未定义的类
     */
    public MType visit(VarDeclaration n, MType argu) {
        MType _ret=null;
        MType type = n.f0.accept(this, argu);
        CheckTypeDeclared(type);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     *
     * 检查返回类型f1是否定义
     * 检查实际返回类型f10是否与f1一致
     * Statement中需要检查变量是否定义 因此需要在这里把当前这个方法的指针传给f8
     * 这个方法已经在符号表中了 可以根据方法名查找到
     * @param argu 这个方法所在的类的指针
     */
    public MType visit(MethodDeclaration n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        MType type = n.f1.accept(this, argu);
        CheckTypeDeclared(type);
        MIdentifier id = (MIdentifier)n.f2.accept(this, argu);
        MMethod newMethod = ((MClass)argu).methodList.get(id.getName());
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, newMethod);
        n.f9.accept(this, argu);

        MType exp = n.f10.accept(this, newMethod);
        CheckExpEqual(exp, type.getType(), "表达式类型和函数返回类型不一致");
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     */
    public MType visit(FormalParameterList n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public MType visit(FormalParameter n, MType argu) {
        MType _ret=null;
        MType type = n.f0.accept(this, argu);
        CheckTypeDeclared(type);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public MType visit(FormalParameterRest n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public MType visit(Type n, MType argu) {
        MType _ret = n.f0.accept(this, argu);
        // 这里的identifier是一个type 为了后续方便 所以就把它的name作为它的type了
        if (_ret.getType().equals("Identifier")) {
            _ret.setType(_ret.getName());
        }
        return _ret;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public MType visit(ArrayType n, MType argu) {
        MType _ret = new MType("Array", n.f0.beginLine, n.f0.endColumn);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "boolean"
     */
    public MType visit(BooleanType n, MType argu) {
        MType _ret = new MType("Boolean", n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "int"
     */
    public MType visit(IntegerType n, MType argu) {
        MType _ret = new MType("Integer", n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public MIdentifier visit(Identifier n, MType argu) {
        MIdentifier _ret = new MIdentifier(n.f0.toString(), "Identifier", n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     *
     * Statement可能出现在MainClass中 也可能出现在method中
     * @param argu Statement所在的主类或者method
     */
    public MType visit(Statement n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public MType visit(Block n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     *
     * 如果statement在类中（主类）在主类中查找这个变量的定义
     * 如果statement在一个method中 那么这个变量的定义应该先从method中查找 再去method所在的类中查找
     * 确定f0的type需要在符号表中查找
     * 检查f0的类型和f2的类型是否一致
     * @param argu 所在的类或者方法
     */
    public MType visit(AssignmentStatement n, MType argu) {
        MType _ret=null;
        MIdentifier id = (MIdentifier)n.f0.accept(this, argu);
        CheckVarDeclared(argu, id);
        n.f1.accept(this, argu);
        MType exp = n.f2.accept(this, argu);
        CheckExpEqual(exp, id.getType(), "=的操作数类型必须一致 左边的类型" + id.getType() + " 右边的类型" + exp.getType());
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     *
     * 对数组元素的赋值操作
     * 检查f0必须是一个Array
     * f0是一个数组名 这个变量可能定义在当前这个类中 也可能定义在当前这个方法中
     *
     * @param argu
     * @return 返回值不重要
     */
    public MType visit(ArrayAssignmentStatement n, MType argu) {
        MType _ret=null;
        MIdentifier id = (MIdentifier)n.f0.accept(this, argu);
        CheckVarDeclared(argu, id);
        CheckExpEqual(id, "Array", id.getName() + "是" + id.getType() + "类型 需要Array类型");
        n.f1.accept(this, argu);
        MType exp = n.f2.accept(this, argu);
        CheckExpEqual(exp, "Integer", "数组下标类型必须是int");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        MType exp2 = n.f5.accept(this, argu);
        CheckExpEqual(exp2, "Integer", "=两边操作数类型不一致");
        n.f6.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     *
     * 检查if括号内表达式类型必须是bool
     */
    public MType visit(IfStatement n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        MType exp = n.f2.accept(this, argu);
        CheckExpEqual(exp, "Boolean", "if语句表达式必须是Boolean");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     *
     * 检查whiile括号内表达式类型必须是bool
     */
    public MType visit(WhileStatement n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        MType exp = n.f2.accept(this, argu);
        CheckExpEqual(exp, "Boolean", "while语句表达式必须是Boolean");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     *
     * print参数必须是整数
     */
    public MType visit(PrintStatement n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        MType exp = n.f2.accept(this, argu);
        CheckExpEqual(exp, "Integer", "print参数必须是整数");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     *
     * Expression节点都需要有效的返回值用于类型检查
     */
    public MType visit(Expression n, MType argu) {
        MType _ret = n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     *
     * "&&" 的两个操作数都是bool型
     * 返回bool型
     */
    public MType visit(AndExpression n, MType argu) {
        MType _ret = new MType("Boolean", n.f1.beginLine, n.f1.beginColumn);
        String msg = "&&操作数必须是bool型";
        MType exp1 = n.f0.accept(this, argu);
        CheckExpEqual(exp1, "Boolean", msg);
        n.f1.accept(this, argu);
        MType exp2 = n.f2.accept(this, argu);
        CheckExpEqual(exp2, "Boolean", msg);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     *
     * "<"的两个操作数都必须是int型
     * 返回bool型
     */
    public MType visit(CompareExpression n, MType argu) {
        MType _ret = new MType("Boolean", n.f1.beginLine, n.f1.beginColumn);
        String msg = "<操作数必须是int型";
        MType exp1 = n.f0.accept(this, argu);
        CheckExpEqual(exp1, "Integer", msg);
        n.f1.accept(this, argu);
        MType exp2 = n.f2.accept(this, argu);
        CheckExpEqual(exp2, "Integer", msg);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     *
     * "+"的操作数必须是int型
     * 返回int型
     */
    public MType visit(PlusExpression n, MType argu) {
        MType _ret = new MType("Integer", n.f1.beginLine, n.f1.beginColumn);
        String msg = "+的操作数必须是int型";
        MType exp1 = n.f0.accept(this, argu);
        CheckExpEqual(exp1, "Integer", msg);
        n.f1.accept(this, argu);
        MType exp2 = n.f2.accept(this, argu);
        CheckExpEqual(exp2, "Integer", msg);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     *
     * "-"操作数必须是int型
     * 返回int型
     */
    public MType visit(MinusExpression n, MType argu) {
        MType _ret = new MType("Integer", n.f1.beginLine, n.f1.beginColumn);
        String msg = "-操作数必须是int型";
        MType exp1 = n.f0.accept(this, argu);
        CheckExpEqual(exp1, "Integer", msg);
        n.f1.accept(this, argu);
        MType exp2 = n.f2.accept(this, argu);
        CheckExpEqual(exp2, "Integer", msg);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     *
     * "*"的操作数必须是int型
     * 返回int型
     */
    public MType visit(TimesExpression n, MType argu) {
        MType _ret = new MType("Integer", n.f1.beginLine, n.f1.beginColumn);
        String msg = "*操作数必须是int型";
        MType exp1 = n.f0.accept(this, argu);
        CheckExpEqual(exp1, "Integer", msg);
        n.f1.accept(this, argu);
        MType exp2 = n.f2.accept(this, argu);
        CheckExpEqual(exp2, "Integer", msg);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     *
     * minijava中的Array只有int,数组下边也必须是int
     * 返回int型
     */
    public MType visit(ArrayLookup n, MType argu) {
        MType _ret = new MType("Integer", n.f1.beginLine, n.f1.beginColumn);
        MType exp1 = n.f0.accept(this, argu);
        CheckExpEqual(exp1, "Array", exp1.getName() + "是" + exp1.getType() + "类型 需要Array类型");
        n.f1.accept(this, argu);
        MType exp2 = n.f2.accept(this, argu);
        CheckExpEqual(exp2, "Integer", "数组下标必须是int型");
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     *
     * 数组必须是int型
     * 返回int型
     */
    public MType visit(ArrayLength n, MType argu) {
        MType _ret = new MType("Integer", n.f1.beginLine, n.f1.beginColumn);
        MType exp = n.f0.accept(this, argu);
        CheckExpEqual(exp, "Integer", "数组类型必须是int");
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     *
     * f0返回的name需要在allClassList中定义过
     * f2需要是f0中定义的一个方法
     * f4参数列表需要和f2的参数列表一致
     * 返回类型是方法的返回类型
     */
    public MType visit(MessageSend n, MType argu) {
        MType _ret=null;
        MType myClass = n.f0.accept(this, argu);
        CheckTypeDeclared(myClass);
        n.f1.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f2.accept(this, argu);
        MClass currentClass = allClassList.classList.get(myClass.getType());
        MMethod newMethod = new MMethod(id.getName(),"null", id.getRow(), id.getCol(), currentClass);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        CheckMethodDeclared(currentClass, newMethod);
        n.f5.accept(this, argu);
        _ret = new MType(currentClass.methodList.get(id.getName()).getReturnType(), myClass.getRow(),myClass.getCol());
        return _ret;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     *
     * 方法参数列表 需要把参数列表插入到方法中
     * 这里f0的返回只需要其type，所以构造的param的name字段为null
     * @param argu 对应的方法
     * @return 返回值可以是空
     */
    public MType visit(ExpressionList n, MType argu) {
        MType _ret = new MMethod("paramList", null, 0, 0, null);
        MType param = n.f0.accept(this, argu);
        paramArray.add(param.getType());
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     *
     * 方法的参数列表 需要把参数列表插入到方法中
     * 这里f1的返回值只需要type，所以构造的param的name字段为null
     * @param argu 对应的方法
     * @return 返回值可以是空
     */
    public MType visit(ExpressionRest n, MType argu) {
        MType _ret = new MMethod(null, null, 0, 0, null);
        n.f0.accept(this, argu);
        MType param = n.f1.accept(this, argu);
        paramArray.add(param.getType());
        return _ret;
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     *
     * 对于Identifier返回的type要特殊处理 要去符号表中查找这个identifier对应的type是什么
     */
    public MType visit(PrimaryExpression n, MType argu) {
        MType _ret = n.f0.accept(this, argu);
        if (_ret.getType().equals("Identifier")) {
            CheckVarDeclared(argu, (MIdentifier)_ret);
        }
        return _ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public MType visit(IntegerLiteral n, MType argu) {
        MType _ret = new MType("Integer", n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "true"
     */
    public MType visit(TrueLiteral n, MType argu) {
        MType _ret = new MType("Boolean", n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "false"
     */
    public MType visit(FalseLiteral n, MType argu) {
        MType _ret = new MType("Boolean", n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> "this"
     *
     * this只能存在method中 所以argu是一个方法
     * @param argu 所在的方法
     */
    public MType visit(ThisExpression n, MType argu) {
        MType _ret = new MType(((MMethod)argu).myThis.getName(), n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     *
     * 需要检查数组下标的返回类型
     * 返回Array型
     */
    public MType visit(ArrayAllocationExpression n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        MType exp = n.f3.accept(this, argu);
        CheckExpEqual(exp, "Integer", "数组下标必须是int");
        _ret = new MType("Array", exp.getRow(), exp.getCol());
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     *
     * 需要检查f1的类型是否定义过 只需要在符号表中查找这个类
     * 返回类型就是identifier的name
     *
     * CheckTypeDeclared 检查的是id的type字段 这里id原来的type是Identifier没什么意义
     */
    public MType visit(AllocationExpression n, MType argu) {
        MType _ret;
        n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f1.accept(this, argu);
        id.setType(id.getName());
        CheckTypeDeclared(id);
        _ret = new MType(id.getName(), id.getRow(), id.getCol());
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     *
     * "！"操作数必须是Boolean型
     * 返回Boolean型
     */
    public MType visit(NotExpression n, MType argu) {
        MType _ret = new MType("Boolean", n.f0.beginLine, n.f0.beginColumn);
        n.f0.accept(this, argu);
        MType exp = n.f1.accept(this, argu);
        CheckExpEqual(exp, "Boolean", "!操作数必须是bool型");
        return _ret;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     *
     * 不需要类型检查
     * 返回括号内表达式的type
     */
    public MType visit(BracketExpression n, MType argu) {
        MType _ret;
        n.f0.accept(this, argu);
        MType exp = n.f1.accept(this, argu);
        _ret = new MType(exp.getType(), exp.getRow(), exp.getCol());
        n.f2.accept(this, argu);
        return _ret;
    }
}