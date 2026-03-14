package blockly.dgir.compiler.java.transformations;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class DeadCodeElimination extends VoidVisitorAdapter<Void> {
  public void visit(ForStmt n, Void arg) {
    // Recursively visit child nodes first to ensure we eliminate dead code in nested blocks.
    super.visit(n, arg);

    Statement body = n.getBody();
    if (body instanceof ContinueStmt) {
      // If the body of the for loop is a continue statement, then we can replace it with an empty
      // block.
      n.setBody(new BlockStmt(n.getBody().getTokenRange().orElse(null), new NodeList<>()));
    }
    if (body instanceof BlockStmt block) {
      // If the last op is a continue op we can remove it.
      if (!block.getStatements().isEmpty()
          && block.getStatements().getLast().orElseThrow() instanceof ContinueStmt) {
        block.getStatements().removeLast();
      }
    }
  }

  public void visit(BlockStmt n, Void arg) {
    // Recursively visit child nodes first to ensure we eliminate dead code in nested blocks.
    super.visit(n, arg);

    if (n.getStatements().size() == 1) {
      Statement stmt = n.getStatement(0);
      if (stmt instanceof BlockStmt block) {
        // If the block only contains a single statement which is itself a block, we can flatten it.
        n.replace(block);
      }
    }

    for (int i = 0; i < n.getStatements().size(); i++) {
      Statement stmt = n.getStatement(i);
      // If we encounter a continue statement, all subsequent statements in the block are dead code
      // and can be removed.
      if (stmt instanceof ContinueStmt) {
        n.getStatements()
            .removeIf(s -> s.getBegin().orElseThrow().isAfter(stmt.getBegin().orElseThrow()));
        break; // No need to check further statements in this block since they have been removed.
      } else if (stmt instanceof BreakStmt) {
        n.getStatements()
            .removeIf(s -> s.getBegin().orElseThrow().isAfter(stmt.getBegin().orElseThrow()));
        break; // No need to check further statements in this block since they have been removed.
      } else if (stmt instanceof ReturnStmt) {
        n.getStatements()
            .removeIf(s -> s.getBegin().orElseThrow().isAfter(stmt.getBegin().orElseThrow()));
        break; // No need to check further statements in this block since they have been removed.
      }
    }
  }
}
