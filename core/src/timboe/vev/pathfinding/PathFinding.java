package timboe.vev.pathfinding;

import com.badlogic.gdx.Gdx;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import timboe.vev.entity.Sprite;
import timboe.vev.entity.Tile;

/**
 * Helper class containing pathfinding algorithms.
 *
 * @author Ben Ruijl
 *
 */
public class PathFinding {

  private static PathingCache cache = new PathingCache();

  /**
   * A Star pathfinding. Note that the heuristic has to be monotonic:
   * {@code h(x) <=
   * d(x, y) + h(y)}.
   *
   * @param start
   *            Starting node
   * @param goal
   *            Goal node
   * @return Shortest path from start to goal, or null if none found
   */
  public static <T extends Node<T>> LinkedList<T> doAStar(T start, T goal, Set<T> solutionKnownFrom, Set<Sprite> doneSet, PathingCache<T> cache) {
    if (goal == null || start == null) Gdx.app.error("PathFinding", "Called with goal = " + goal + " start = " + start);
    if (goal.getNeighbours().size() == 0) return null;

    //TODO this still isn't working properly :(
//    LinkedList<T> cacheHit = cache.getCacheHit(start, goal);
//    if (cacheHit != null) {
//      return cacheHit;
//    }

    Set<T> closed = new HashSet<T>();
    Map<T, T> fromMap = new HashMap<T, T>();
    LinkedList<T> route = new LinkedList<T>();
    Map<T, Double> gScore = new HashMap<T, Double>();
    final Map<T, Double> fScore = new HashMap<T, Double>();
    PriorityQueue<T> open = new PriorityQueue<T>(11, new Comparator<T>() {
      public int compare(T nodeA, T nodeB) {
        return Double.compare(fScore.get(nodeA), fScore.get(nodeB));
      }
    });

    gScore.put(start, 0.0);
    fScore.put(start, start.getHeuristic(goal));
    open.offer(start);

    while (!open.isEmpty()) {
      T current = open.poll();
      if (current.equals(goal)) { // I found it on my own

        while (current != null) {
          route.add(0, current);
          current = fromMap.get(current);
        }
        cache.addToCache(start, goal, route);
        return route;

      } else if (solutionKnownFrom != null && solutionKnownFrom.contains(current)) { // Someone else knows how to take it from here

        List<Tile> otherSolution = null;
        for (Sprite done : doneSet) {
          if (done.getPathingList().contains(current)) {
            otherSolution = done.getPathingList();
            break;
          }
        }

        // Add up to
        if (otherSolution == null) {
          Gdx.app.error("pathFinding","element was in solutionsKnownFrom but not any individual soln?!");
          return null;
        }
//        Gdx.app.error("pathFinding","partial pathfind. CACHED. Split at:" + ((Tile)current).x + "," + ((Tile)current).y);
        for (int i = otherSolution.size() - 1; i >= 0; --i) {
          if (otherSolution.get(i) == current) break;
          route.add(0, (T)otherSolution.get(i) );
//          Gdx.app.error("  ADD C - ",((Tile)otherSolution.get(i)).x + "," + ((Tile)otherSolution.get(i)).y);
        }

        // Add the remainder - unique to me
//        Gdx.app.error("pathFinding","partial pathfind. UNIQUE");
        while (current != null) {
          route.add(0, current);
//          Gdx.app.error("  ADD - U",((Tile)current).x + "," + ((Tile)current).y);
          current = fromMap.get(current);
        }
        cache.addToCache(start, goal, route);
        return route;
      }

      closed.add(current);

      for (T neighbour : current.getNeighbours()) {
        if (closed.contains(neighbour)) {
          continue;
        }

        double tentG = gScore.get(current) + current.getTraversalCost(neighbour);

        boolean contains = open.contains(neighbour);
        if (!contains || tentG < gScore.get(neighbour)) {
          gScore.put(neighbour, tentG);
          fScore.put(neighbour, tentG + neighbour.getHeuristic(goal));

          if (contains) {
            open.remove(neighbour);
          }

          open.offer(neighbour);
          fromMap.put(neighbour, current);
        }
      }
    }

    return null;
  }
}