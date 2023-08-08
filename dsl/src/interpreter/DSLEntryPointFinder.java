package interpreter;

import dslToGame.DSLEntryPoint;
import dslToGame.QuestConfig;
import parser.DungeonASTConverter;
import parser.ast.*;
import runtime.GameEnvironment;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DSLEntryPointFinder implements AstVisitor<Object> {
    private ArrayList<DSLEntryPoint> entryPoints;
    private Path filePath;
    private final GameEnvironment environment;
    private AggregateType questConfigDataType;

    public DSLEntryPointFinder() {
        this.environment = new GameEnvironment();
        var symbols = environment.getGlobalScope().getSymbols();
        for (Symbol symbol : symbols) {
            if (symbol instanceof AggregateType aggregateType) {
                if (aggregateType.getOriginType().equals(QuestConfig.class)) {
                    this.questConfigDataType = aggregateType;
                }
            }
        }
    }

    public Optional<List<DSLEntryPoint>> getEntryPoints(Path filePath) {
        try {
            // we don't want to do the whole interpretation here...
            // we only want to know, which (well formed) entry points exist
            // would be enough to do this in a light AST-Visitor..
            String content = Files.readString(filePath);
            Node programAST = DungeonASTConverter.getProgramAST(content);

            List<DSLEntryPoint> list = findEntryPoints(filePath, programAST);
            return Optional.of(list);
        } catch (IOException e) {
            // ok, be like that then..
            return Optional.empty();
        }
    }


    private List<DSLEntryPoint> findEntryPoints(Path filePath, Node programAST) {
        this.entryPoints = new ArrayList<>();
        this.filePath = filePath;
        programAST.accept(this);
        return entryPoints;
    }

    @Override
    public Object visit(Node node) {
        switch (node.type) {
            case Program:
                visitChildren(node);
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visit(IdNode node) {
        return node.getName();
    }

    @Override
    public Object visit(StringNode node) {
        return node.getValue();
    }

    @Override
    public Object visit(ListTypeIdentifierNode node) {
        return node.getName();
    }

    @Override
    public Object visit(SetTypeIdentifierNode node) {
        return node.getName();
    }

    @Override
    public Object visit(DotDefNode node) {
        return null;
    }

    private String getDisplayName(ObjectDefNode questConfigDefNode) {
        String displayName = questConfigDefNode.getIdName();
        for (Node node : questConfigDefNode.getPropertyDefinitions()) {
            PropertyDefNode propertyDefNode = (PropertyDefNode) node;
            if (propertyDefNode.getIdName().equals("name")) {
                displayName = (String) propertyDefNode.getStmtNode().accept(this);
            }
        }
        return displayName;
    }

    @Override
    public Object visit(ObjectDefNode node) {
        Node typeSpecifier = node.getTypeSpecifier();
        String typeSpecifierName = (String)typeSpecifier.accept(this);
        if (typeSpecifierName.equals(questConfigDataType.getName())) {
            // found one
            String configName = node.getIdName();
            String displayName = getDisplayName(node);
            this.entryPoints.add(new DSLEntryPoint(this.filePath, displayName, configName));
        }
        return null;
    }

    @Override
    public Object visit(FuncDefNode node) {
        return null;
    }
}
