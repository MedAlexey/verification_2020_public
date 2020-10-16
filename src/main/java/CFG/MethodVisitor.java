package CFG;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class MethodVisitor extends GenericVisitorAdapter<CFGNode, Void> {

    @Override
    public CFGNode visit(MethodDeclaration n, Void arg) {
        super.visit(n, arg);
        return new CFGBuilder().build(n.getBody().isPresent() ? n.getBody().get() : null);
    }

}
