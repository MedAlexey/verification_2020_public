package CFG;

import com.github.javaparser.ast.Node;
import guru.nidi.graphviz.attribute.Shape;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CFGNode {
    public static final String START = "start";
    public static final String END = "end";
    public static final String STATEMENTS = "statements";
    public static final String SWITCH_EXIT = "switch exit";
    public static final String CONTINUE = "continue";
    public static final String BREAK = "break";

    private Shape shape;
    private List<CFGNode> children;
    private List<CFGNode> linkedBreaks;
    private List<Node> body;
    private String label;
    private String uniqueId;

    public String getBodyAsString() {
        StringBuilder builder = new StringBuilder();
        body.forEach(node -> builder.append(node.toString()));
        return builder.toString();
    }

    public void removeChild(CFGNode child) {
        children.remove(child);
    }

    public void addChildren(List<CFGNode> children) {
        this.children.addAll(children);
    }

    public void addChild(CFGNode child) {
        this.children.add(child);
    }

    public void addLinkedBreak(CFGNode linkedBreak) {
        this.linkedBreaks.add(linkedBreak);
    }

    public void addLinkedBreaks(List<CFGNode> linkedBreaks) {
        this.linkedBreaks.addAll(linkedBreaks);
    }

    public void clearChildren() {
        this.children.clear();
    }

    public List<CFGNode> getChildren() {
        return children;
    }

    public void removeLinkedBreaks() {
        this.linkedBreaks.clear();
    }

    @Builder()
    private CFGNode(Shape shape, List<Node> body, String label) {
        this.shape = shape == null ? Shape.BOX : shape;
        this.uniqueId = UUID.randomUUID().toString();
        this.label = label == null ?  "" : label;
        this.linkedBreaks = new ArrayList<>();
        this.children = new ArrayList<>();
        this.body = new ArrayList<>();
        if (body != null) {
            this.body.addAll(body);
        }
    }

}
