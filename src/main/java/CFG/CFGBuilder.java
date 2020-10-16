package CFG;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import guru.nidi.graphviz.attribute.Shape;

import java.util.ArrayList;
import java.util.List;

public class CFGBuilder {

    private CFGNode start;
    private CFGNode end;

    CFGBuilder() {
        start = CFGNode.builder().label(CFGNode.START).build();
        end = CFGNode.builder().label(CFGNode.END).build();
        start.addChild(end);
    }

    public CFGNode build(BlockStmt body) {
        if (body != null) {
            handleBody(body, new ArrayList<>(List.of(start)),end, end, end);
        }
        return start;
    }

    /**
     * Помещает newNode между startNode и endNode
     *
     * @param startNodes вершина графа, после которой будет добавлена новая
     * @param endNode вершина графа до которой будет добавлена новая
     * @param newNode добавляемая вершина
     */
    private void placeNewNode(List<CFGNode> startNodes, CFGNode endNode, CFGNode newNode) {
        startNodes.forEach(node -> {
            node.removeChild(endNode);
            node.addChild(newNode);
        });
        newNode.addChild(endNode);

        endNode.getLinkedBreaks().forEach(node -> {node.getChildren().clear(); node.getChildren().add(newNode);});
        newNode.addLinkedBreaks(endNode.getLinkedBreaks());
        endNode.removeLinkedBreaks();
    }

    /**
     * Добавление 'if' выражения в граф
     *
     * @param statement добавляемое выражение
     * @param startNodes вершина графа, к которой будет прикреплено добавляемое выражение
     * @param endNode вершина графа, которая будет прикреплена к добавляемому выражению
     * @return
     */
    private List<CFGNode> addIfStatement(IfStmt statement, List<CFGNode> startNodes, CFGNode endNode,
                                         CFGNode continueTarget, CFGNode breakTarget) {
        CFGNode conditionNode = CFGNode.builder()
                .shape(Shape.DIAMOND)
                .body(List.of(statement.getCondition()))
                .build();

        placeNewNode(startNodes, endNode, conditionNode);

        List<CFGNode> thenNodes = handleBody(
                statement.getThenStmt().asBlockStmt(),
                new ArrayList<>(List.of(conditionNode)),
                endNode, continueTarget, breakTarget);

        if (statement.getElseStmt().isPresent()) {
            List<CFGNode> elseNodes = handleBody(
                    statement.getElseStmt().get().asBlockStmt(),
                    new ArrayList<>(List.of(conditionNode)),
                    endNode, continueTarget, breakTarget);
            thenNodes.addAll(elseNodes);
            return thenNodes;
        }

        thenNodes.add(conditionNode);
        return thenNodes;
    }

    /**
     *  Добавление 'while' выражения в граф
     *
     * @param whileStmt добавляемое выражение
     * @param startNodes вершина графа, к которой будет прикреплено добавляемое выражение
     * @param endNode вершина графа, которая будет прикреплена к добавляемому выражению
     * @return
     */
    public List<CFGNode> addWhileStatement(WhileStmt whileStmt, List<CFGNode> startNodes, CFGNode endNode) {
        CFGNode conditionNode = CFGNode.builder()
                .shape(Shape.DIAMOND)
                .body(List.of(whileStmt.getCondition()))
                .build();

        placeNewNode(startNodes, endNode, conditionNode);
        handleBody(whileStmt.getBody().asBlockStmt(), new ArrayList<>(List.of(conditionNode)), conditionNode,
                conditionNode, endNode);

        return new ArrayList<>(List.of(conditionNode));
    }

    /**
     *  Добавление 'for' выражения в граф
     *
     * @param forStmt добавляемое выражение
     * @param startNodes вершина графа, к которой будет прикреплено добавляемое выражение
     * @param endNode вершина графа, которая будет прикреплена к добавляемому выражению
     * @return
     */
    public List<CFGNode> addForStatement(ForStmt forStmt, List<CFGNode> startNodes, CFGNode endNode) {
        CFGNode initNode = CFGNode.builder()
                .shape(Shape.BOX)
                .body(new ArrayList<Node>(forStmt.getInitialization()))
                .build();
        CFGNode conditionNode = CFGNode.builder()
                .shape(Shape.DIAMOND)
                .body(forStmt.getCompare().isPresent() ? List.of(forStmt.getCompare().get()) : null)
                .build();
        CFGNode updateNode = CFGNode.builder()
                .body(new ArrayList<Node>(forStmt.getUpdate()))
                .build();

        placeNewNode(startNodes, endNode, initNode);
        placeNewNode(new ArrayList<>(List.of(initNode)), endNode, conditionNode);
        updateNode.addChild(conditionNode);

        handleBody(forStmt.getBody().asBlockStmt(), new ArrayList<>(List.of(conditionNode)), updateNode, updateNode,
                endNode);

        return new ArrayList<>(List.of(conditionNode));
    }

    /**
     *
     *  Добавление 'do while' выражения в граф
     *
     * @param doStmt добавляемое выражение
     * @param startNodes вершина графа, к которой будет прикреплено добавляемое выражение
     * @param endNode вершина графа, которая будет прикреплена к добавляемому выражению
     * @return
     */
    public List<CFGNode> addDoWhileStatement(DoStmt doStmt, List<CFGNode> startNodes, CFGNode endNode) {
        CFGNode conditionNode = CFGNode.builder()
                .shape(Shape.DIAMOND)
                .body(List.of(doStmt.getCondition()))
                .build();


        startNodes.forEach(node -> {node.clearChildren();node.addChild(conditionNode);});
        conditionNode.addChildren(startNodes);

        handleBody(doStmt.getBody().asBlockStmt(), startNodes, conditionNode, conditionNode, endNode);

        startNodes.get(0).getChildren().get(0).addLinkedBreaks(endNode.getLinkedBreaks());
        endNode.removeLinkedBreaks();
        startNodes.get(0).getChildren().get(0).getLinkedBreaks().forEach(node -> {
            node.getChildren().clear();
            node.getChildren().add(startNodes.get(0).getChildren().get(0));
        });

        return new ArrayList<>(List.of(conditionNode));
    }

    /**
     *  Добавление switch выражения в граф
     *
     * @param switchStmt добавляемое выражение
     * @param startNodes вершина графа, к которой будет прикреплено добавляемое выражение
     * @param endNode вершина графа, которая будет прикреплена к добавляемому выражению
     * @return список вершин, являющихся последними в добавляемом выражении
     */
    public List<CFGNode> addSwitchStatement(SwitchStmt switchStmt, List<CFGNode> startNodes, CFGNode endNode,
                                            CFGNode continueTarget) {
        CFGNode switchExitNode = CFGNode.builder().label(CFGNode.SWITCH_EXIT).build();
        startNodes.forEach(node -> node.removeChild(endNode));

        switchStmt.getEntries().forEach(switchEntry -> {
            CFGNode newNode = CFGNode.builder().label(CFGNode.STATEMENTS).build();
            startNodes.forEach(node -> node.addChild(newNode));
            newNode.addChild(switchExitNode);

            for (int i = 0; i < switchEntry.getStatements().size(); i++) {
                handleBody(
                        switchEntry.getStatement(i).asBlockStmt(),
                        new ArrayList<>(List.of(newNode)),
                        switchExitNode, continueTarget, switchExitNode);
            }
        });

        return new ArrayList<>(List.of(switchExitNode));
    }

    /**
     *  Добавление statement'a в граф
     * @param statement выражение, которое бует добавлено в граф
     * @param startNodes вершина графа, к которой будет прикреплено добавляемое выражение
     * @param endNode вершина графа, которая будет прикреплена к добавляемому выражению
     * @return
     */
    public List<CFGNode> addStatement(Statement statement, List<CFGNode> startNodes, CFGNode endNode,
                                      CFGNode continueTarget, CFGNode breakTarget) {
        CFGNode newNode = CFGNode.builder().body(List.of(statement)).label(CFGNode.STATEMENTS).build();
        placeNewNode(startNodes, endNode, newNode);

        if (statement.isContinueStmt() && continueTarget != null) {
            newNode.setLabel(CFGNode.CONTINUE);
            newNode.clearChildren();
            newNode.addChild(continueTarget);
            return new ArrayList<>();
        } else if (statement.isBreakStmt() && breakTarget != null) {
            newNode.setLabel(CFGNode.BREAK);
            newNode.clearChildren();
            newNode.addChild(breakTarget);
            breakTarget.addLinkedBreak(newNode);
            return new ArrayList<>();
        }

        return new ArrayList<>(List.of(newNode));
    }

    /**
     *  Добавление statement'a в узел графа
     *
     * @param statement выражение, которое бует добавлено в узел графа
     * @param node узел графа, в который будет добавлен statement
     */
    public boolean appendStatement(Statement statement, CFGNode node, CFGNode continueTarget, CFGNode breakTarget) {
        if (statement.isContinueStmt()) {
            CFGNode continueNode = CFGNode.builder().label(CFGNode.CONTINUE).build();
            node.clearChildren();
            node.addChild(continueNode);
            continueNode.addChild(continueTarget);
            return false;
        } else if (statement.isBreakStmt()) {
            CFGNode breakNode = CFGNode.builder().label(CFGNode.BREAK).build();
            node.clearChildren();
            node.addChild(breakNode);
            breakNode.addChild(breakTarget);
            return false;
        }

        node.getBody().add(statement);

        return true;
    }

    /**
     *  Цепляет body между startNodes и endNode
     *
     * @param body выражения, заключённые между '{' и '}'
     * @param startNodes вершиы, к которым будет прицеплено body
     * @param endNode вершина, которая будет прицеплена к body
     * @return список вершин, являющихся низом исходного body
     */
    private List<CFGNode> handleBody(BlockStmt body, List<CFGNode> startNodes, CFGNode endNode, CFGNode continueTarget,
                                     CFGNode breakTarget) {
        List<CFGNode> curStartNodes = startNodes;

        for (int i = 0; i < body.getStatements().size(); i++) {
            if (body.getStatement(i).isIfStmt()) {
                curStartNodes = addIfStatement(body.getStatement(i).asIfStmt(), curStartNodes, endNode, continueTarget,
                        breakTarget);
            } else if (body.getStatement(i).isWhileStmt()) {
                curStartNodes = addWhileStatement(body.getStatement(i).asWhileStmt(), curStartNodes, endNode);
            } else if(body.getStatement(i).isForStmt()) {
                curStartNodes = addForStatement(body.getStatement(i).asForStmt(), curStartNodes, endNode);
            } else if(body.getStatement(i).isDoStmt()) {
                curStartNodes = addDoWhileStatement(body.getStatement(i).asDoStmt(), curStartNodes, endNode);
            } else if(body.getStatement(i).isSwitchStmt()) {
                curStartNodes = addSwitchStatement(body.getStatement(i).asSwitchStmt(), curStartNodes, endNode,
                        continueTarget);
            } else if (curStartNodes.size() == 1 && curStartNodes.get(0).getLabel().equals(CFGNode.STATEMENTS)) {
                if (!appendStatement(body.getStatement(i), curStartNodes.get(0), continueTarget, breakTarget)) {
                    curStartNodes.clear();
                }
            } else {
                curStartNodes = addStatement(body.getStatement(i), curStartNodes, endNode, continueTarget, breakTarget);
            }
        }

        return curStartNodes;
    }

}
