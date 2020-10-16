package CFG;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.util.LinkedHashMap;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class CFGToDotConverter {

    private final LinkedHashMap<String, MutableNode> setOfNodes;
    private final MutableGraph graph;

    public CFGToDotConverter() {
        setOfNodes = new LinkedHashMap<>();
        graph = mutGraph("graph").setDirected(true);
    }

    public CFGToDotConverter convert(CFGNode root) {
        handleCFGNode(root);
        buildGraph();
        return this;
    }

    private void buildGraph() {
        setOfNodes.forEach((String key, MutableNode value) -> graph.add(value));
    }

    public void saveGraph(String path) {
        try {
            Graphviz.fromGraph(graph).render(Format.PNG).toFile(new File(path));
        } catch (Exception e) {
            System.out.println("Error saving graph.");
        }
    }

    private void handleCFGNode(CFGNode cfgNode) {
        MutableNode curMutNode;

        if (setOfNodes.containsKey(cfgNode.getUniqueId())) {
            curMutNode = setOfNodes.get(cfgNode.getUniqueId());
        } else {
            curMutNode = mutNode(cfgNode.getUniqueId().toString())
                    .add(cfgNode.getShape(),
                            Label.of(cfgNode.getBodyAsString().equals("") ? cfgNode.getLabel() :
                                    cfgNode.getBodyAsString()
                                            .replaceAll(";", "")
                                            .replaceAll("/\\*.*\\*/[\n]?", "")
                                            .replaceAll("//.*\n", ""))
                    );
            setOfNodes.put(cfgNode.getUniqueId(), curMutNode);
        }

        for (CFGNode child: cfgNode.getChildren()) {
            if ((child.getLabel().equals(CFGNode.SWITCH_EXIT) || child.getLabel().equals(CFGNode.CONTINUE) ||
                    child.getLabel().equals(CFGNode.BREAK) ) && child.getChildren().size() > 0) {
                child = child.getChildren().get(0);
            }

            if (setOfNodes.containsKey(child.getUniqueId())) {
                MutableNode newChild = setOfNodes.get(child.getUniqueId());
                curMutNode.addLink(newChild);
            } else {
                MutableNode newChild = mutNode(child.getUniqueId().toString())
                        .add(child.getShape(), Label.of(child.getBodyAsString().equals("") ? child.getLabel() :
                                child.getBodyAsString()
                                        .replaceAll(";", "\n")
                                        .replaceAll("/*\\*.*\\*/[\n]?", "")
                                        .replaceAll("//.*\n", ""))
                        );
                setOfNodes.put(child.getUniqueId(), newChild);
                curMutNode.addLink(newChild);
                handleCFGNode(child);
            }
        }

    }

}
