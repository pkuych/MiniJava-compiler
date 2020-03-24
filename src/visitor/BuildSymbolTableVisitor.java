package visitor;

import com.sun.xml.internal.ws.encoding.MtomCodec;

import symbol.MClass;
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
public class BuildSymbolTableVisitor extends GJDepthFirst<MType, MType> {

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     * argu -> allclassList
     */
    public MType visit(Goal n, MType argu) {
        MType _ret=null;
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
     * argu -> allClassList
     */
    public MType visit(MainClass n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier) n.f1.accept(this, argu);
        MClass newClass = new MClass(id.getName(), null, id.getRow(), id.getCol());
        String msg = argu.insertClass(newClass);
        if (msg != null) {
            ErrorPrinter.print(msg, newClass.getRow(), newClass.getCol());
        }
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
        n.f14.accept(this, newClass);
        n.f15.accept(this, argu);
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
     */
    public MType visit(ClassDeclaration n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f1.accept(this, argu);
        MClass newClass = new MClass(id.getName(), null, id.getRow(), id.getCol());
        String msg = argu.insertClass(newClass);
        if (msg != null) {
            ErrorPrinter.print(msg, newClass.getRow(), newClass.getCol());
        }
        n.f2.accept(this, argu);
        n.f3.accept(this, newClass);
        n.f4.accept(this, newClass);
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
     */
    public MType visit(ClassExtendsDeclaration n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        MIdentifier parent = (MIdentifier)n.f3.accept(this, argu);
        MClass newClass = new MClass(id.getName(), parent.getName(), id.getRow(), id.getCol());
        String msg = argu.insertClass(newClass);
        if (msg != null) {
            ErrorPrinter.print(msg, newClass.getRow(), newClass.getCol());
        }
        n.f4.accept(this, argu);
        n.f5.accept(this, newClass);
        n.f6.accept(this, newClass);
        n.f7.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     *
     * @param argu 可能是主类 辅助类 或者辅助类中的方法
     */
    public MType visit(VarDeclaration n, MType argu) {
        MType _ret = null;
        MType type = n.f0.accept(this, argu);
        MIdentifier id = (MIdentifier) n.f1.accept(this, argu);
        MVar var = new MVar(id.getName(), type.getType(), type.getRow(), type.getCol());
        String msg = argu.insertVar(var);
        if (msg != null) {
            ErrorPrinter.print(msg, var.getRow(), var.getCol());
        }
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
     * 需要把新建的方法传给f4和f7
     * @param argu 方法所在的类
     */
    public MType visit(MethodDeclaration n, MType argu) {
        MType _ret=null;
        n.f0.accept(this, argu);

        MType type = n.f1.accept(this, argu);
        MIdentifier id = (MIdentifier)n.f2.accept(this, argu);
        MMethod newMethod = new MMethod(id.getName(), type.getType(), type.getRow(), type.getCol(), (MClass)argu);

        n.f3.accept(this, argu);
        n.f4.accept(this, newMethod);

        String msg = argu.insertMethod(newMethod);
        if (msg != null) {
            ErrorPrinter.print(msg, type.getRow(), type.getCol());
        }

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, newMethod);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
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
        MIdentifier id = (MIdentifier)n.f1.accept(this, argu);
        MVar var = new MVar(id.getName(), type.getType(), type.getRow(), type.getCol());
        String msg = argu.insertParam(var);
        if (msg != null) {
            ErrorPrinter.print(msg, var.getRow(), var.getCol());
        }
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
}