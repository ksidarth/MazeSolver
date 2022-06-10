import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents walls of the maze
class Wall {
  Edge edge;
  Color color;

  Wall(Edge edge) {
    this.edge = edge;
    this.color = Color.RED;
  }

  Wall(Edge edge, Color color) {
    this.edge = edge;
    this.color = color;
  }

  // Draws walls of the maze
  WorldImage drawWall() {
    if (this.edge.beginning.x == this.edge.end.x) {
      Edge tempEdge = new Edge(this.edge.beginning, this.edge.end, this.color);
      return new RotateImage(tempEdge.drawEdge(), 90);
    }
    else {
      Edge tempEdges = new Edge(this.edge.beginning, this.edge.end, this.color);
      return new RotateImage(tempEdges.drawEdge(), -90);
    }
  }
}

// Represents a vertex of the maze
class Vertex {
  int x;
  int y;
  // Vertex fields
  Vertex top;
  Vertex bottom;
  Vertex right;
  Vertex left;
  ArrayList<Edge> allConnectedEdges;

  Vertex(int x, int y) {
    this.x = x;
    this.y = y;
    this.allConnectedEdges = new ArrayList<Edge>();
    this.bottom = null;
    this.top = null;
    this.right = null;
    this.left = null;
  }

  // Checks if two vertices are equal
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Vertex)) {
      return false;
    }
    Vertex temp = (Vertex) o;
    return this.x == temp.x && this.y == temp.y;
  }

  // Override hashcode
  @Override
  public int hashCode() {
    return this.x + this.y * 1008;
  }
}

// Represents the edges of a maze
class Edge {
  Vertex beginning;
  Vertex end;
  int weight;
  Color path;
  boolean solved;

  Edge(Vertex beginning, Vertex end) {
    this.solved = true;
    this.beginning = beginning;
    this.end = end;
    this.weight = (int) (Math.random() * 55);
    this.path = Color.BLACK;
  }

  Edge(Vertex beginning, Vertex end, Color path) {
    this.solved = true;
    this.beginning = beginning;
    this.end = end;
    this.weight = (int) (Math.random() * 55);
    this.path = path;
  }

  Edge(Vertex beginning, Vertex end, Color path, int weight) {
    this.solved = true;
    this.beginning = beginning;
    this.end = end;
    this.weight = weight;
    this.path = path;
  }

  Edge(Vertex beginning, Vertex end, Color path, int weight, boolean solved) {
    this.beginning = beginning;
    this.end = end;
    this.weight = weight;
    this.path = path;
    this.solved = solved;

  }

  // Draws the edges to the maze
  WorldImage drawEdge() {
    if (this.beginning.x == this.end.x) {
      WorldImage lineV = new LineImage(new Posn(0, 30), this.path);
      WorldImage newLineV = lineV.movePinhole(0, -15);
      return newLineV;
    }
    else {
      WorldImage lineH = new LineImage(new Posn(30, 0), this.path);
      WorldImage newLineH = lineH.movePinhole(-15, 0);
      return newLineH;
    }
  }
}

// Sorts edges by weights
class EdgesByWeights implements Comparator<Edge> {

  EdgesByWeights() {
  }

  // compares two edges
  @Override
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }

}

// Represents the maze
class Maze extends World {
  static int HEIGHT = 10;
  static int WIDTH = 10;
  ArrayList<ArrayList<Vertex>> vertexRow;
  ArrayList<Vertex> vertexColumn;
  ArrayList<Edge> edges;
  ArrayList<Edge> sortedEdges;
  ArrayList<ArrayList<Vertex>> grid;
  ArrayList<Vertex> subgrid;
  ArrayList<Wall> walls;
  ArrayList<Edge> connectedList;
  boolean isSearching;
  int timer;

  Maze() {
    this.edges = new ArrayList<Edge>();
    this.sortedEdges = new ArrayList<Edge>();
    this.connectedList = new ArrayList<Edge>();
    this.isSearching = false;
    this.timer = 0;
    addVertexes();
    createEdges();
    this.sortedEdges.sort(new EdgesByWeights());
    createGrid();
    createWalls();
    removeCycles();
  }

  // Convenience constructor for testing
  Maze(ArrayList<Edge> edges) {
    this.edges = edges;
    this.sortedEdges = edges;
    this.connectedList = new ArrayList<Edge>();
    this.sortedEdges.sort(new EdgesByWeights());
  }

  // Called on every tick
  public void onTick() {
    if (this.isSearching) {
      timer++;
    }
    pathFinder();
  }

  // Checks if certain keys are pressed
  public void onKeyEvent(String key) {
    // Restart the maze
    if (key.equals("r")) {
      this.edges = new ArrayList<Edge>();
      this.sortedEdges = new ArrayList<Edge>();
      this.connectedList = new ArrayList<Edge>();
      this.isSearching = false;
      this.timer = 0;
      addVertexes();
      createEdges();
      this.sortedEdges.sort(new EdgesByWeights());
      createGrid();
      createWalls();
      removeCycles();
    }
    // Solve the maze
    // TODO: implement breadth first search
    else if (key.equals("b")) {
      this.timer = 0;
      this.isSearching = true;
      correctEdges();
      solveMaze();
    }

    // Solve the maze
    else if (key.equals("d")) {
      this.isSearching = true;
      this.timer = 0;
      correctEdges();
      solveMaze();
    }
  }

  // Initializes the Vertexs list
  public void addVertexes() {
    this.vertexRow = new ArrayList<ArrayList<Vertex>>();
    this.vertexColumn = new ArrayList<Vertex>();
    for (int i = 0; i < Maze.WIDTH; i++) {
      for (int j = 0; j < Maze.HEIGHT; j++) {
        this.vertexColumn.add(new Vertex(i, j));
      }
      this.vertexRow.add(vertexColumn);
      this.vertexColumn = new ArrayList<Vertex>();
    }
  }

  // Initializes the edges list
  void createEdges() {
    this.edges = new ArrayList<Edge>();
    for (int i = 0; i < Maze.WIDTH; i++) {
      for (int j = 0; j < Maze.HEIGHT; j++) {
        if (i + 1 < Maze.WIDTH) {
          this.edges.add(new Edge(this.vertexRow.get(i).get(j), this.vertexRow.get(i + 1).get(j)));
        }
        if (j + 1 < Maze.HEIGHT) {
          this.edges.add(new Edge(this.vertexRow.get(i).get(j), this.vertexRow.get(i).get(j + 1)));
        }
      }
    }
    this.sortedEdges = this.edges;
  }

  // Creates walls to the maze
  public void createWalls() {
    this.walls = new ArrayList<Wall>();
    for (int i = 0; i < this.edges.size(); i++) {
      this.walls.add(new Wall(this.edges.get(i)));
    }
  }

  // creates subgrids of all grids
  public void createGrid() {
    this.grid = new ArrayList<ArrayList<Vertex>>();
    this.subgrid = new ArrayList<Vertex>();
    for (int i = 0; i < Maze.WIDTH; i++) {
      for (int j = 0; j < Maze.HEIGHT; j++) {
        this.subgrid.add(this.vertexRow.get(i).get(j));
        this.grid.add(subgrid);
        this.subgrid = new ArrayList<Vertex>();
      }
    }
  }

  // Removes all Edges that create cycles
  public void removeCycles() {
    int size = this.grid.size();
    while (this.grid.size() > 1) {
      for (int i = 0; i < this.sortedEdges.size(); i++) {
        this.removeCyclesHelper(this.sortedEdges.get(i));
        if (this.grid.size() < size) {
          this.sortedEdges.set(i,
              new Edge(this.sortedEdges.get(i).beginning, this.sortedEdges.get(i).end, Color.BLUE));
          this.walls.set(i, new Wall(this.walls.get(i).edge, Color.WHITE));
          size = this.grid.size();
        }
      }
    }
  }

  // If there are no similarities change if not dont
  public void removeCyclesHelper(Edge e) {
    Vertex currBeginning = e.beginning;
    Vertex currEnd = e.end;
    int repBeg = 0;
    int repEnd = 0;
    for (int i = 0; i < this.grid.size(); i++) {
      for (int j = 0; j < this.grid.get(i).size(); j++) {
        if (this.grid.get(i).get(j).equals(currBeginning)) {
          repBeg = i;
        }
        if (this.grid.get(i).get(j) == currEnd) {
          repEnd = i;
        }
      }
    }
    if (repBeg != repEnd) {
      this.grid.get(repBeg).addAll(this.grid.get(repEnd));
      this.grid.remove(repEnd);
    }
  }

  // Removes all edges with walls between
  public void correctEdges() {
    this.connectedList = new ArrayList<Edge>();
    for (int i = 0; i < this.sortedEdges.size(); i++) {
      if (this.sortedEdges.get(i).path.equals(Color.BLUE)) {
        this.connectedList.add(this.sortedEdges.get(i));
      }
    }
  }

  // Returns edges if it solves the maze
  public Edge solveMazeHelper(Edge edge) {
    int c1 = 0;
    int c2 = 0;
    for (int i = 0; i < this.connectedList.size(); i++) {
      if (edge.beginning == this.connectedList.get(i).beginning) {
        c1++;
      }
      if (edge.beginning == this.connectedList.get(i).end) {
        c1++;
      }
      if (edge.end == this.connectedList.get(i).beginning) {
        c2++;
      }
      if (edge.end == this.connectedList.get(i).end) {
        c2++;
      }
    }
    if ((c1 == 1 || c2 == 1) && edge.end != this.vertexRow.get(Maze.HEIGHT - 1).get(Maze.WIDTH - 1)
        && edge.beginning != this.vertexRow.get(0).get(0)) {
      edge = new Edge(edge.beginning, edge.end, Color.white, edge.weight, false);
      return edge;
    }
    else {
      return edge;
    }
  }

  // Sets connected Edges list to all edges that solve the maze
  public void solveMaze() {
    for (int i = 0; i < this.connectedList.size(); i++) {
      this.connectedList.set(i, this.solveMazeHelper(this.connectedList.get(i)));
    }
  }

  // Checks if all edges are connected
  public boolean allConnected() {
    boolean ans = true;
    for (int i = 0; i < this.connectedList.size(); i++) {
      ans = ans && this.connectedList.get(i).solved;
    }
    return ans;
  }

  // Removes all edges that dont make a solution
  public void removeWrong() {
    for (int i = this.connectedList.size() - 1; i >= 0; i--) {
      if (!this.connectedList.get(i).solved) {
        this.connectedList.remove(i);
      }
    }
  }

  // If all are not connected remove the ones that dont connect
  public void pathFinder() {
    if (!this.allConnected()) {
      this.removeWrong();
      this.solveMaze();
    }
    if (this.allConnected()) {
      this.isSearching = false;
    }
  }

  // Called on every tick
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(Maze.WIDTH * 30, Maze.HEIGHT * 30);
    WorldImage background = new RectangleImage(Maze.WIDTH * 30, Maze.HEIGHT * 30,
        OutlineMode.OUTLINE, Color.GRAY);
    scene.placeImageXY(background, Maze.WIDTH * 15, Maze.HEIGHT * 15);
    scene.placeImageXY(new RectangleImage(30, 30, OutlineMode.SOLID, Color.GREEN), 15, 15);
    scene.placeImageXY(new RectangleImage(30, 30, OutlineMode.SOLID, Color.PINK),
        (Maze.WIDTH * 30) - 15, (Maze.HEIGHT * 30) - 15);
    WorldImage textD = new TextImage("Press key d to solve the maze with DFS", Color.BLACK);
    WorldImage textR = new TextImage("Press key r to restart the maze", Color.BLACK);
    WorldImage textB = new TextImage("Press key b to solve the maze with BFS", Color.BLACK);
    WorldImage timer = new TextImage("Time to search: " + this.timer + " ticks", Color.RED);
    scene.placeImageXY(background, Maze.HEIGHT * 15, Maze.HEIGHT * 15);
    scene.placeImageXY(textD, (Maze.WIDTH * 40) + 20, (Maze.HEIGHT * 15) + 20);
    scene.placeImageXY(textR, (Maze.HEIGHT * 40) + 20, (Maze.HEIGHT * 15) - 20);
    scene.placeImageXY(textB, (Maze.HEIGHT * 40) + 20, (Maze.HEIGHT * 15) - 60);
    scene.placeImageXY(timer, (Maze.HEIGHT * 40) + 20, (Maze.HEIGHT * 15) - 80);
    // Draw all walls
    for (int j = 0; j < this.walls.size(); j++) {
      scene.placeImageXY(this.walls.get(j).drawWall(),
          (this.walls.get(j).edge.beginning.x * 30) + 30,
          (this.walls.get(j).edge.beginning.y * 30) + 30);
    }
    // Draw all edges
    for (int i = 0; i < this.connectedList.size(); i++) {
      scene.placeImageXY(this.connectedList.get(i).drawEdge(),
          (this.connectedList.get(i).beginning.x * 30) + 15,
          (this.connectedList.get(i).beginning.y * 30) + 15);
    }
    return scene;
  }
}

// Examples Class
class ExamplesMaze {
  // initializes init conditnions
  Vertex ver1;
  Vertex ver2;
  Vertex ver3;
  Vertex ver4;
  Vertex ver5;
  Vertex ver6;
  Edge edge1;
  Edge edge2;
  Edge edge3;
  Edge edge4;
  Edge edge5;
  Edge edge6;
  Wall wall1;
  Wall wall2;
  Wall wall3;
  Wall wall4;
  ArrayList<Edge> edges;
  ArrayList<Edge> unconnectedEdges;
  Maze maze;
  Maze Maze;
  Maze maze3;

  // INitializes variables
  public void init() {
    ver1 = new Vertex(1, 1);
    ver2 = new Vertex(2, 2);
    ver3 = new Vertex(3, 3);
    ver4 = new Vertex(4, 4);
    ver5 = new Vertex(5, 5);
    ver6 = new Vertex(6, 6);
    edge1 = new Edge(this.ver1, this.ver3, Color.black, 30);
    edge2 = new Edge(this.ver3, this.ver4, Color.white, 0);
    edge3 = new Edge(this.ver6, this.ver4, Color.black, 55);
    edge4 = new Edge(this.ver5, this.ver4, Color.CYAN, 15);
    edge5 = new Edge(this.ver4, this.ver6, Color.LIGHT_GRAY, 60);
    edge6 = new Edge(this.ver1, this.ver6, Color.BLUE, 40);
    edge6.solved = false;
    wall1 = new Wall(edge1);
    wall2 = new Wall(edge2, Color.black);
    wall3 = new Wall(edge4, Color.red);
    wall4 = new Wall(edge3, Color.BLUE);
    edges = new ArrayList<Edge>(
        Arrays.asList(this.edge1, this.edge2, this.edge3, this.edge4, this.edge5));
    unconnectedEdges = new ArrayList<Edge>(Arrays.asList(this.edge1, this.edge3, this.edge6));
    maze = new Maze();
    Maze = new Maze();
    maze3 = new Maze();
  }

  // plays the game
  void testMaze(Tester t) {
    init();
    this.maze.bigBang(Maze.WIDTH * 60, Maze.HEIGHT * 60, 1 / 8.0);
  }

  // Test OnKeyEvent
  void testOnKeyEvent(Tester t) {
    init();
    Maze temp = this.maze;
    maze.onKeyEvent("r");
    t.checkExpect(maze.allConnected(), true);
    t.checkExpect(temp, this.maze);
    maze.onKeyEvent("d");
    t.checkExpect(maze.allConnected(), false);
    maze.onKeyEvent("b");
    t.checkExpect(maze.allConnected(), false);
  }

  // Tests method solveMazeHelper
  void testsolveMazeHelper(Tester t) {
    init();
    this.maze.connectedList = this.edges;
    Edge temp = new Edge(this.ver1, this.ver3, Color.WHITE, 30);
    temp.solved = false;
    t.checkExpect(maze.solveMazeHelper(this.maze.connectedList.get(0)), temp);
    t.checkExpect(maze.solveMazeHelper(temp), temp);
  }

  // tests Method solveMaze
  void testsolveMaze(Tester t) {
    init();
    maze.connectedList = this.edges;
    maze.solveMaze();
    t.checkExpect(this.edges, maze.connectedList);
    maze3.connectedList = this.edges;
    maze3.solveMaze();
    t.checkExpect(this.edges, maze3.connectedList);
  }

//
  // Tests method allConnected
  void testAllConnected(Tester t) {
    init();
    t.checkExpect(this.maze.allConnected(), true);
    this.maze.connectedList = this.unconnectedEdges;
    t.checkExpect(this.maze.allConnected(), false);
  }

//
  // Tests method removeWrong
  void testremoveWrong(Tester t) {
    init();
    ArrayList<Edge> temp = maze.connectedList;
    maze.removeWrong();
    t.checkExpect(maze.connectedList, temp);
    maze.connectedList = this.unconnectedEdges;
    maze.removeWrong();
    t.checkExpect(maze.connectedList, new ArrayList<Edge>(Arrays.asList(this.edge1, this.edge3)));
  }

  // Tests method pathfinder
  void testPathfinder(Tester t) {
    init();
    ArrayList<Edge> temp = this.maze.connectedList;
    this.maze.pathFinder();
    t.checkExpect(this.maze.connectedList, temp);
    this.maze.connectedList = this.unconnectedEdges;
    this.maze.pathFinder();
    t.checkExpect(this.maze.connectedList, this.unconnectedEdges);
    t.checkExpect(this.maze.isSearching, false);
  }

  // Tests correctEdges
  void testCorrectEdges(Tester t) {
    init();
    t.checkExpect(maze.connectedList, new ArrayList<Edge>());
    this.edges.sort(new EdgesByWeights());
    this.edges.get(0).path = Color.blue;
    this.maze.sortedEdges = this.edges;
    maze.correctEdges();
    t.checkExpect(maze.connectedList, new ArrayList<Edge>(Arrays.asList(this.edges.get(0))));
    this.maze.sortedEdges = this.edges;
    this.maze.correctEdges();
    t.checkExpect(this.maze.connectedList, new ArrayList<Edge>(Arrays.asList(this.edge2)));
  }

//
  // test drawWall
  void testDrawWall(Tester t) {
    init();
    edge1.path = Color.RED;
    edge2.path = Color.black;
    t.checkExpect(wall1.drawWall(), new RotateImage(this.edge1.drawEdge(), -90));
    t.checkExpect(wall2.drawWall(), new RotateImage(this.edge2.drawEdge(), -90));
  }

  // tests method equals
  void testEquals(Tester t) {
    init();
    t.checkExpect(ver1.equals(ver2), false);
    t.checkExpect(ver3.equals(ver3), true);
    t.checkExpect(ver1.equals(ver3), false);
  }

//
  // tests method HashCode
  void testHashCode(Tester t) {
    init();
    t.checkExpect(ver1.hashCode(), 1009);
    t.checkExpect(ver3.hashCode(), 3027);
    t.checkExpect(ver5.hashCode(), 5045);
  }

  // tests method drawEdges
  void testDrawEdges(Tester t) {
    init();
    LineImage temp = new LineImage(new Posn(30, 0), this.edge5.path);
    temp.pinhole = new Posn(-15, 0);
    t.checkExpect(edge5.drawEdge(), temp);

    temp = new LineImage(new Posn(30, 0), this.edge3.path);
    temp.pinhole = new Posn(-15, 0);
    t.checkExpect(edge3.drawEdge(), temp);

    temp = new LineImage(new Posn(30, 0), this.edge4.path);
    temp.pinhole = new Posn(-15, 0);
    t.checkExpect(edge4.drawEdge(), temp);
  }

  // tests method compare
  void testCompare(Tester t) {
    init();
    EdgesByWeights e = new EdgesByWeights();
    t.checkExpect(e.compare(edge2, edge1), -30);
    t.checkExpect(e.compare(edge1, edge2), 30);
    t.checkExpect(e.compare(edge2, edge2), 0);
  }

  // Tests createEdges
  void createEdges(Tester t) {
    init();
    maze.edges = new ArrayList<Edge>();
    maze.createEdges();
    t.checkExpect(maze.edges, null);
    maze.createEdges();
    t.checkExpect(maze.edges, edges);
  }

  // Tests method createWalls
  void testCreateWalls(Tester t) {
    init();
    maze.walls = new ArrayList<Wall>();
    maze.createWalls();
    // assert randomness
    t.checkFail(this.maze.walls, this.maze3.walls);
    maze.createWalls();
    // all walls are different
    t.checkFail(maze.walls.get(2), maze.walls.get(4));
    t.checkFail(maze.walls.get(5), maze.walls.get(6));
    t.checkFail(maze.walls.get(10), maze.walls.get(20));
  }

  // Tests method create Grid
  void testCreateGrid(Tester t) {
    init();
    maze.grid = new ArrayList<ArrayList<Vertex>>();
    maze.createGrid();
    // assert randomness
    t.checkFail(maze.grid, maze3.grid);
    // all vertexes in the grid are different
    t.checkFail(maze.grid.get(3), maze.grid.get(54));
    t.checkFail(maze.grid.get(10), maze.grid.get(29));
    t.checkFail(maze.grid.get(15), maze.grid.get(45));
  }

  // Tests method addVertexs
  void testAddVertexs(Tester t) {
    init();
    t.checkExpect(maze, maze);
    maze.vertexColumn = new ArrayList<>();
    maze.vertexRow = new ArrayList<>();
    maze.addVertexes();
    t.checkFail(maze.vertexColumn, maze);
    t.checkFail(maze.vertexRow, maze);
  }

  // tests removeCyclesHelper
  // automatically called in the constructor
  void testRemoveCyclesHelper(Tester t) {
    init();
    t.checkExpect(maze, maze);
    maze.removeCyclesHelper(edge1);
    t.checkExpect(maze, maze);
    maze.removeCyclesHelper(edge5);
    t.checkExpect(maze, maze);
    maze.removeCyclesHelper(edge3);
    t.checkExpect(maze, maze);
  }

  // tests RemoveCycles
  // removeCycles is automatically called in the constructor
  void testRemoveCycles(Tester t) {
    init();
    t.checkExpect(maze, maze);

    maze.sortedEdges = this.edges;
    maze.removeCycles();
    t.checkExpect(maze.walls, maze.walls);
    t.checkExpect(maze.sortedEdges, this.edges);
    maze.sortedEdges = this.unconnectedEdges;
    t.checkExpect(maze.sortedEdges, this.unconnectedEdges);
    maze.removeCycles();
    t.checkExpect(maze.sortedEdges, this.unconnectedEdges);
  }

//  // Tests method makeScene
//  void testMakeScene(Tester t) {
//    init();
//    t.checkExpect(maze.makeScene(), null);
//    // Change constant size
//    t.checkExpect(Maze.makeScene(), null);
//    // Change constant size
//    t.checkExpect(maze3.makeScene(), null);
//  }
}
