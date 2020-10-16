import CFG.CFGNode;
import CFG.MethodVisitor;
import com.github.javaparser.StaticJavaParser;

import java.io.File;

public class BaseTest {

    public CFGNode buildCFG(String path) throws Exception{
        return new MethodVisitor().visit(
                StaticJavaParser.parse(new File(path)),
                null
        );
    }


}
