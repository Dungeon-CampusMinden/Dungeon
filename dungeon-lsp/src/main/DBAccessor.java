package main;

import dsl.parser.ast.Node;
import dsl.parser.ast.SourceFileReference;
import dsl.semanticanalysis.symbol.Symbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.Position;
import org.neo4j.ogm.session.Session;

public class DBAccessor {
  private final Session session;

  public DBAccessor(Session session) {
    this.session = session;
  }

  public Map<String, Object> resolveContextToNearestAstNode(Position position) {
    var result =
        session.query(
            """
        // find nearest ASTNode (furthest to right)
        match (n:AstNode)-[]-(nearestSfr:SourceFileReference) where nearestSfr.startLine = $startline and nearestSfr.endColumn < $endcolumn and n.hasErrorRecord = false
        and not exists {(n)<-[:CHILD_OF]-(:AstNode)}
        match (n:AstNode)-[:CHILD_OF]->(parent:AstNode)
        match (parent)-[]-(parentSfr:SourceFileReference)
        return n, parent, nearestSfr, parentSfr
        order by nearestSfr.startColumn desc
        limit 1
        """,
            Map.of("startline", position.getLine(), "endcolumn", position.getCharacter()));
    var iter = result.iterator();
    if (iter.hasNext()) {
      return iter.next();
    } else {
      return Map.of(
          "n",
          Node.NONE,
          "parent",
          Node.NONE,
          "nearestSfr",
          SourceFileReference.NULL,
          "parentSfr",
          SourceFileReference.NULL);
    }
  }

  public Map<String, Object> resolveContext(Position position) {
    var result =
        session.query(
            """
        // resolve context v3
        match (n:AstNode)-[]-(nearestSfr:SourceFileReference) where nearestSfr.startLine = $startline and nearestSfr.endColumn < $endcolumn and n.hasErrorRecord = false and not exists {(n)<-[:CHILD_OF]-(:AstNode)}
        CALL{
        with n, nearestSfr
        // match closest scope, either referenced scopes symbol (in memberaccess) or created (in normal scope)
        match p=(n)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(scope:ScopedSymbol)
        return scope,p
        UNION
        // match closest scope, either referenced scopes symbol (in memberaccess) or created (in normal scope)
        match p=(n)-[:CHILD_OF*]-(parent:AstNode)-[:CREATES]-(scope:IScope)
        return scope,p
        }
        return scope, n, nearestSfr,length(p)
        //order by length(p) desc
        Order by nearestSfr.endColumn desc, length(p)
        LIMIT 1
        """,
            Map.of("startline", position.getLine(), "endcolumn", position.getCharacter()));

    var iter = result.iterator();
    return iter.next();
  }

  // TODO: does not work as expected...
  public List<Symbol> getAllSymbolsInParentScopeLikeLhsType(Node memberAccessNode, String prefix) {
    var result =
        session.query(
            """
      call {
            match (na:AssignmentNode) where na.idx=$idx
            match (na)-[:PARENT_OF {idx:0}]->(lhsChild:AstNode)
            match (lhsChild)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(lhsType:IType)
            return lhsChild, na, lhsType
            limit 1
        }

   //get symbols in scope and parent scopes v2
        call{
            match (n:AstNode) where n.idx=$idx
            // match closest scope created (in normal scope)
            match p=(n)-[:CHILD_OF*]-(parent:AstNode)-[:CREATES]-(scope:IScope)
            return scope, p, parent
            order by length(p)
            limit 1
        }
        call {
            with scope
            match l=(scope)
            return scope as _scope, 1 as len, l
            UNION
            with scope
            // collect parent scopes
            match l=((scope)-[:PARENT_SCOPE|IN_SCOPE*]->(parentScope:IScope))
            return parentScope as _scope, length(l) as len, l
        }

        match (symbol:Symbol)-[:IN_SCOPE]->(_scope) where
        exists {(symbol)-[:OF_TYPE]->(lhsType)} and
        symbol.name STARTS WITH $prefix
        return symbol, lhsType
  """,
            Map.of("idx", memberAccessNode.getIdx(), "prefix", prefix));

    ArrayList<Symbol> symbols = new ArrayList<>();
    result.forEach(m -> symbols.add((Symbol) m.get("symbol")));
    return symbols;
  }

  public List<Symbol> getAllSymbolsInScopeAndParentScopes(Node prefixNode, String prefix) {
    var result =
        session.query(
            """
  //get symbols in scope and parent scopes v2
  call{
      match (n:AstNode) where n.idx=$idx
      // match closest scope created (in normal scope)
      match p=(n)-[:CHILD_OF*]-(parent:AstNode)-[:CREATES]-(scope:IScope)
      return scope, p, parent
      order by length(p)
      limit 1
  }
  call {
      with scope
      match l=(scope)
      return scope as _scope, 1 as len, l
      UNION
      with scope
      // collect parent scopes
      match l=((scope)-[:PARENT_SCOPE|IN_SCOPE*]->(parentScope:IScope))
      return parentScope as _scope, length(l) as len, l
  }
  //return distinct scope, _scope, len,l

  //UNWIND scopes as scopeToFocus
  match (symbol:Symbol)-[:IN_SCOPE]-(_scope) where symbol.name starts with $prefix
  return distinct symbol
  """,
            Map.of("idx", prefixNode.getIdx(), "prefix", prefix));

    ArrayList<Symbol> symbols = new ArrayList<>();
    result.forEach(m -> symbols.add((Symbol) m.get("symbol")));
    return symbols;
  }

  public List<Symbol> getSymbolsInScopeOfIdentifier(Node identifier) {
    var result =
        session.query(
            """
  call {
      match (n:AstNode) where n.idx=$idx
      match (n)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(scope:ScopedSymbol)
      return n, scope
      limit 1
  }
  match (symbol:Symbol)-[:IN_SCOPE]->(scope)
  return symbol
  """,
            Map.of("idx", identifier.getIdx()));

    ArrayList<Symbol> symbols = new ArrayList<>();
    result.forEach(m -> symbols.add((Symbol) m.get("symbol")));
    return symbols;
  }

  public List<Long> getChildIdxsOfMemberAccess(Node memberAccesNode) {
    var result =
        session.query(
            """
      match (n:MemberAccessNode) where n.idx=$idx
      match (n)-[childEdge:PARENT_OF]->(child:AstNode)
      return child.idx as childIdx, n order by childEdge.idx
    """,
            Map.of("idx", memberAccesNode.getIdx()));

    ArrayList<Long> idxs = new ArrayList<>();
    result.forEach(m -> idxs.add((Long) m.get("childIdx")));
    return idxs;
  }

  public List<Symbol> getSymbolsInScopeOfLhsIdentifierWithPrefix(
      Node memberAccessParentNode, String prefix) {
    var result =
        session.query(
            """
  call {
      match (n:MemberAccessNode) where n.idx=$idx
      match (n)-[:PARENT_OF {idx:0}]->(lhsChild:AstNode)
      match (lhsChild)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(scope:ScopedSymbol)
      return lhsChild, n, scope
      limit 1
  }
  match (symbol:Symbol)-[:IN_SCOPE]->(scope) where symbol.name STARTS WITH $prefix
  return symbol
  """,
            Map.of("idx", memberAccessParentNode.getIdx(), "prefix", prefix));

    ArrayList<Symbol> symbols = new ArrayList<>();
    result.forEach(m -> symbols.add((Symbol) m.get("symbol")));
    return symbols;
  }
}
