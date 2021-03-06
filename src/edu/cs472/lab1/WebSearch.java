package edu.cs472.lab1;

import java.util.HashSet;
import java.util.List;

import edu.cs472.lab1.heuristics.AggregateHeuristic;
import edu.cs472.lab1.heuristics.SearchHeuristicComparator;

public class WebSearch
{
    SearchNodeSet OPEN;
    HashSet<String> CLOSED;

    static final boolean DEBUG = false;

    final int BEAM_WIDTH = 2;

    /**
     * Searching for the page with this exact String
     */
    final String GOAL_PATTERN;

    final SearchStrategy SEARCH_STRATEGY;

    public static enum SearchStrategy
    {
        BREADTH, DEPTH, BEST, BEAM
    };

    public static void main(String args[])
    {
        if (args.length != 2) {
            System.out
                    .println("You must provide the directoryName and searchStrategyName.  Please try again.");
            return;
        }

        String startURL = args[0];
        String searchStrategyName = args[1];

        SearchStrategy strategy;
        if (searchStrategyName.equalsIgnoreCase("breadth"))
            strategy = SearchStrategy.BREADTH;

        else if (searchStrategyName.equalsIgnoreCase("depth"))
            strategy = SearchStrategy.DEPTH;

        else if (searchStrategyName.equalsIgnoreCase("best"))
            strategy = SearchStrategy.BEST;

        else if (searchStrategyName.equalsIgnoreCase("beam"))
            strategy = SearchStrategy.BEAM;

        else {
            System.out.println("The valid search strategies are:");
            System.out.println("  BREADTH DEPTH BEST BEAM");
            return;
        }

        final String GOAL_PATTERN = "QUERY1 QUERY2 QUERY3 QUERY4";
        WebSearch ws = new WebSearch(GOAL_PATTERN, strategy);
        ws.performSearch(startURL);
        Utilities.waitHere("Press ENTER to exit.");
    }

    public WebSearch(String goalPattern, SearchStrategy searchStrategy)
    {
        this.SEARCH_STRATEGY = searchStrategy;
        this.GOAL_PATTERN = Utilities.normalizeSearchString(goalPattern);
    }

    public void performSearch(String startURL)
    {
        int nodesVisited = 0;

        switch (SEARCH_STRATEGY) {
        case BREADTH:
            OPEN = new SearchNodeSetBF();
            break;
        case DEPTH:
            OPEN = new SearchNodeSetDF();
            break;
        case BEST:
            OPEN = new SearchNodeSetBestFirst(new SearchHeuristicComparator(
                    new AggregateHeuristic(GOAL_PATTERN)));
            break;
        case BEAM:
            OPEN = new SearchNodeSetBeam(new SearchHeuristicComparator(
                    new AggregateHeuristic(GOAL_PATTERN)), BEAM_WIDTH);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unimplemented searchStrategy passed to performSearch");
        }

        CLOSED = new HashSet<String>();

        OPEN.add(new SearchNode(startURL));
        while (!OPEN.isEmpty()) {
            SearchNode currentNode = OPEN.pop();
            nodesVisited++;

            if (visitNode(currentNode))
                break;

            // Provide a status report.
            LOG("Nodes visited = " + nodesVisited + " |OPEN| = " + OPEN.size());
        }

        System.out.println(" Visited " + nodesVisited + " nodes, starting @"
                + " " + startURL + ", using: " + SEARCH_STRATEGY + " search.");
    }

    /**
     * Detects goal reached, expands the node, adds node to CLOSED, adds
     * children to OPEN if needed
     * 
     * @param parent
     *            the node to visit
     * @return true if it is a goal node
     */
    private boolean visitNode(SearchNode parent)
    {
        LOG("Visiting node at URL: " + parent.getNodeURL());
        List<SearchNode> newChildren = parent.getChildren();
        if (parent.isGoalForPattern(GOAL_PATTERN)) {
            System.out.println("Found solution at depth " + parent.getDepth());
            parent.reportSolutionPath();
            return true;
        }

        // Remember this node was visited.
        CLOSED.add(parent.getNodeURL());

        for (SearchNode child : newChildren) {
            LOG("Found a link to " + child.getNodeName());

            if (OPEN.containsURL(child.getNodeURL())) {
                LOG("    this node is in the OPEN list.");
            } else if (CLOSED.contains(child.getNodeURL())) {
                LOG("    this node is in the CLOSED list.");
            } else
                OPEN.add(child);
        }

        return false;
    }

    public static void LOG(String msg)
    {
        if (DEBUG)
            System.out.println(msg);
    }
}