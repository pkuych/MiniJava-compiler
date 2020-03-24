import java.io.FileInputStream;
import java.io.InputStream;

import symbol.MClassList;
import symbol.MType;
import syntaxtree.Node;
import typecheck.ErrorPrinter;
import visitor.BuildSymbolTableVisitor;
import visitor.TypeCheckVisitor;

/**
 * @author yuchanghong
 * Created on 2020/3/7
 */
public class Main {
    public static void main(String args[]){
        try {
            InputStream in = new FileInputStream(args[0]);
            Node root = new MiniJavaParser(in).Goal();
            MType allClassList = new MClassList();
            root.accept(new BuildSymbolTableVisitor(), allClassList);
            root.accept(new TypeCheckVisitor(), allClassList);
            //ErrorPrinter.printResult();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (TokenMgrError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
