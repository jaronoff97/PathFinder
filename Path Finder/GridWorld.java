import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class GridWorld extends Applet implements Runnable, MouseListener, KeyListener, MouseMotionListener
{
   public int worldx;
   public int worldy;
   public int columns;
   public int rows;
   public int destX, destY, sX, sY;
   public int cost, lowestCost, lowestSteps, totalPaths;
   public boolean sizeD;
   public boolean waiting = true;
   public boolean changeStart=false;
   public boolean changeEnd=false;
   public boolean changeBlock=false;
   public int[][] grid;
   public ArrayList<Node> openList=new ArrayList<Node>();
   public ArrayList<Node> closedList=new ArrayList<Node>();
   public boolean[][] blocked;
   public int[][] prevBest;
	
   Graphics bufferGraphics; //Set up double buffer
   Image offscreen;
   Thread thread;//Sets up a Thread called thread

   public void init()
   {
      worldx=1400;//Sets the world size
      worldy=1000;//Sets the world size
      cost=0;//Sets the cost
      lowestCost=5000;//Arbitrary number
      totalPaths=0;//Sets the paths
      sizeD=false;//Makes it so there are no NPEs
      columns=5;//Set the columns and rows to an arbitrary common number
      rows=5;//Set the columns and rows to an arbitrary common number
      destX=0;//Set the destinations and the starts
      destY=0;//Set the destinations and the starts
      sX=0;//Set the destinations and the starts
      sY=0;//Set the destinations and the starts
      grid= new int[rows][columns];//Construct the MDA
      prevBest= new int[rows][columns];//Construct the MDA
      blocked= new boolean[rows][columns];//Construct the MDA
   
      offscreen = createImage(worldx,worldy); //create a new image that's the size of the applet DOUBLE BUFFER SET UP
      bufferGraphics = offscreen.getGraphics(); //set bufferGraphics to the graphics of the offscreen image. DOUBLE BUFFER SET UP
   
      addKeyListener(this);//setup all the listeners
      addMouseListener(this);//setup all the listeners
      addMouseMotionListener(this);//setup all the listeners
      thread = new Thread(this);  //constructs a new thread
      thread.start();             //starts the thread
   }//init()
   public class Node
   {
      private int row=0;
      private int column=0;
      private int fScore = 0;
      private int gScore = 0;
      private int hScore = 0;
      private Node parent;
      public Node(int x, int y)
      {
         this.row=x;
         this.column=y;
      }
      public Node(Node n)
      {
         this.row=n.row;
         this.column=n.column;
      }
      public Node(int x, int y, Node p)
      {
         this.row=x;
         this.column=y;
         parent=p;
         fScore=findFCost(parent);
      }
      public void makeParent(int x1, int y1)
      {
         parent = new Node(x1, y1);
      }
      public void makeParent(Node n)
      {
         parent = new Node(n);
      }
      public int findGCost(Node a)
      {
         if(sameNode(this,a))
         {
            return(1);
         }
         return(1+parent.findGCost(a));
      }
      public int findHCost(Node a)
      {
         return((int) (Math.abs(this.row-a.row) + Math.abs(this.column-a.column)));
      }
      public int findFCost(Node a)
      {
         return(findGCost(a)+findHCost(a));
      }
      public String toString()
      {
         return("( "+row+" , "+column+" )");
      }
   }
   public boolean sameNode(Node a, Node b)
   {
      if(!(a.row==b.row && a.column==b.column))
         return false;
   
      return true;
   }

   public void fillGrid()
   {
      grid= new int[rows][columns];//Constructs all the arrays
      prevBest= new int[rows][columns];//Constructs all the arrays
      blocked= new boolean[rows][columns];//Constructs all the arrays
      lowestCost=0;
      for(int ro = 0;ro<rows;ro++)
      {
         for(int col = 0;col<columns;col++)
         {
            grid[ro][col]=(int)(Math.random()*100);//randomize the cost of each space
            prevBest[ro][col]=0;//construct the path
            blocked[ro][col]=false;//constructs the blocked array
            if(ro==col)
            {
               prevBest[ro][col]=1;//Sets the previous best path as the diagonal
               lowestCost+=grid[ro][col];//Sets the lowest cost
            }
         }
         destX=(rows-1);//Sets the destination for the farthest point
         destY=(columns-1); //Sets the destination for the farthest point
      }
     
   }
   

   public int[][] replace(int[][] p1, int[][] p2)
   {
      for(int col=0;col<columns;col++)
      {
         for(int ro=0;ro<rows;ro++)
         {
            p1[ro][col]=p2[ro][col]; //Replace the rows and columns
         }
      }
      return(p1);
   }
   public void travel(int[][] newBest, int cX, int cY, int eX, int eY, int cost, int steps){ //Method was created after much discussion on StackOverFlow from JosEDU, Two Methods were created both are at the bottom of the applet, the only big change was from a public int travel to public void travel, the previous trouble arose from NPE's because of the way I was calling the method.
      if (cX == eX && cY == eY){ 
        //printPath(newBest);
         totalPaths++; //adds one to the total amount of paths
         if (cost < lowestCost){
         
            lowestCost = cost; //Resets the lowest cost
            lowestSteps = steps; //Resets the lowest steps
            newBest[cX][cY] = 1; //Marks the final spot on the path
         
            replace(prevBest, newBest); //Replaces the previous best path with the new best path
         }
      } 
      else {
         newBest[cX][cY] = 1;//mark newBest
      
         if(isValid(cX + 1, cY + 1, newBest, eX, eY)){//Check if the following move is valid
            if(blocked[cX+1][cY+1]==false){//Make sure the path isn't blocked
               if(cost+grid[cX+1][cY+1]<lowestCost) {//Avoid unnecessary costs
                  travel(newBest, cX + 1, cY + 1, eX, eY, cost + grid[cX + 1][cY+1], steps+1);//move in diagonal (right-down)
               } 
            }
         }
         if (isValid(cX, cY + 1, newBest, eX, eY)){ //Check if the following move is valid       
            if(blocked[cX][cY+1]==false){//Make sure the path isn't blocked
               if(cost+grid[cX][cY+1]<lowestCost){//Avoid unnecessary costs
                  travel(newBest, cX, cY + 1, eX, eY, cost + grid[cX][cY+1], steps+1);//move right
               }
            }
         }
         if (isValid(cX + 1, cY, newBest, eX, eY)){//Check if the following move is valid        
            if(blocked[cX+1][cY]==false){//Make sure the path isn't blocked
               if(cost+grid[cX+1][cY]<lowestCost){//Avoid unnecessary costs
                  travel(newBest, cX + 1, cY, eX, eY, cost + grid[cX + 1][cY], steps+1);//move down
               }
            }
         }
      
         newBest[cX][cY] = 0; //unmark path
      }
   }
   public void aStar(Node start, Node current, Node end, int steps)
   {
   	
      int lowScore=0;
      for(Node now:closedList)
      {
         lowScore+=now.fScore;
      }
      if(current.row==end.row && current.column==end.column)
      {
         lowestCost=lowScore;
         lowestSteps=steps;
         return;
      }
      for(Node temp: openList)
      {
         if(temp.fScore<=openList.get(0).fScore)
         {
            openList.set(0,temp);
         }
      
      }
      closedList.add(openList.get(0));
      openList.remove(0);
      for(Node temp:adjacent(closedList.get(steps)))
      {
         if(isValid(temp,end))
         {
            if(isInClosedList(temp)==true)
            {
            //System.out.println(temp);
               openList.remove(temp);
            }
            if(isInOpenList(temp)==false)
            {
            //System.out.println(temp);
               openList.add(temp);
            }
            if(isInOpenList(temp)==true)
            {
            //System.out.println("Working! Steps is: "+steps+" closedList size is: "+closedList.size());
               if((lowScore-closedList.get(steps).fScore+temp.fScore)<lowScore)
               {
                  closedList.remove(closedList.size());
                  closedList.add(temp);
                  System.out.println(temp);
                  aStar(start,temp,end,steps+1);
               }
            }
         }
      
      }
   }
   public ArrayList<Node> adjacent(Node n)
   {
      ArrayList<Node> adj = new ArrayList<Node>();
      adj.add(new Node(n.row+1,n.column, n));//right
      adj.add(new Node(n.row-1,n.column, n));//left
      adj.add(new Node(n.row,n.column+1, n));//down
      adj.add(new Node(n.row,n.column-1, n));//up
      adj.add(new Node(n.row+1,n.column+1, n));//right-down
      adj.add(new Node(n.row+1,n.column-1, n));//right-up
      adj.add(new Node(n.row-1,n.column+1, n));//left-down
      adj.add(new Node(n.row-1,n.column-1, n));//left-up
      return(adj);
   }
   public boolean isInClosedList(Node t)
   {
      for(Node temp: closedList)
      {
         if(temp.row==t.row && temp.column==t.column)
         {
         	//System.out.println("temp in closedList is: "+temp);
         	//System.out.println("   t in closedList is: "+t);
            return true;
         }
      
      }
      return false;
   }
   public boolean isInOpenList(Node t)
   {
      for(Node temp: openList)
      {
         if(temp.row==t.row && temp.column==t.column)
         {
         	//System.out.println("temp in openList is: "+temp);
         	//System.out.println("t in openList is: "+t);
            return true;
         }
      
      }
      return false;
   }
 

   public void printPath(int[][] p) 
   {
      for (int col = 0; col < p.length; col++) 
      {
         for (int ro = 0; ro < p.length; ro++) 
         {
            System.out.print(p[ro][col] + " ");//Print the paths
         }
         System.out.println();
      }
      System.out.println();
   }
   public void printPath(ArrayList<Node> n) 
   {
      for(Node temp: n)
      {
         System.out.println("("+temp.row+" , "+temp.column+")");
      }
   }
   public boolean isValid(int x, int y, int[][] path, int eX, int eY) {
   
      if (!((x >= 0 && x < grid.length) && (y >= 0 && y < grid.length)))
         return false;  //not valid if: cordinates are into grid dimensions
      if(blocked[x][y]==true){
         return false; // Can't make the move if it's blocked
      }
      if (path[x][y] == 0 || (x == eX && y == eY))
         return true;//valid if: not visited yet, or is destiny
   
      return true;
   }
   public boolean isValid(Node a, Node end) {
   
      if (!((a.row >= 0 && a.row < grid.length) && (a.column >= 0 && a.column < grid.length)))
         return false;  //not valid if: cordinates are into grid dimensions
      if(blocked[a.row][a.column]==true){
         return false; // Can't make the move if it's blocked
      }
      if ((a.row == end.row && a.column == end.column))
         return true;//valid if: not visited yet, or is destiny
   
      return true;
   }
   
   public void paint(Graphics g) 
   {// paint() is used to display things on the screen
      setSize(worldx,worldy);
      bufferGraphics.clearRect(0,0,worldx,worldy); //clear the offscreen image
      bufferGraphics.setColor(Color.black);
      for(int ro = 0;ro<rows;ro++)
      {
         for(int col = 0;col<columns;col++)
         {
            if(sizeD==true)
            {
               if(blocked[ro][col]==true)
               {
                  bufferGraphics.fillRect((50*ro),(50*col),50,50);//Fill a blocked tile
               }
               if(prevBest[ro][col]==1)
               {
                  bufferGraphics.drawString(""+grid[ro][col],(50*ro)+25,(50*col)+25);//Show the path that has been found
                  bufferGraphics.setColor(Color.gray);//Show the path that has been found
                  bufferGraphics.fillRect((50*ro),(50*col),50,50);//Show the path that has been found
                  bufferGraphics.setColor(Color.black);//Show the path that has been found
               }
               bufferGraphics.drawString(""+grid[ro][col],(50*ro)+25,(50*col)+25);
               bufferGraphics.drawLine(0,(50*ro),50*columns,(50*ro));
               bufferGraphics.drawLine((50*col),0,(50*col),50*rows);
            
               bufferGraphics.drawString("The lowest cost is: "+lowestCost, 1050,50);//Show the lowest cost
               bufferGraphics.drawString("The lowest amount of steps is: "+lowestSteps, 1050,75);//show the lowest steps
               bufferGraphics.drawString("The total amount of paths is: "+totalPaths, 1050,100);//Show the total amount of paths
               bufferGraphics.drawString("Change Start position ", 1075,250);//Change start position button
               bufferGraphics.drawRect(1050,200,200,100);
               if(changeStart==true)
               {
                  bufferGraphics.setColor(Color.gray);//Fill the button
                  bufferGraphics.fillRect(1050,200,200,100);//Fill the button
                  bufferGraphics.setColor(Color.black);//Fill the button
               }
               bufferGraphics.drawString("Change end position ", 1075,450);//Change end position button
               bufferGraphics.drawRect(1050,400,200,100);
               if(changeEnd==true)
               {
                  bufferGraphics.setColor(Color.gray);//Fill the button
                  bufferGraphics.fillRect(1050,400,200,100);//Fill the button
                  bufferGraphics.setColor(Color.black);//Fill the button
               }
               bufferGraphics.drawString("Block a space ", 1075,650);//Block a space button
               bufferGraphics.drawRect(1050,600,200,100);
               if(changeBlock==true)
               {
                  bufferGraphics.setColor(Color.gray);//Fill the button
                  bufferGraphics.fillRect(1050,600,200,100);//Fill the button
                  bufferGraphics.setColor(Color.black);//Fill the button
               }
            }
         }
      }
      if(sizeD==false)
      {
         bufferGraphics.drawString("How Big a Grid Do you want?", worldx/2,200);
         for(int i=3;i<=20;i++)
         {
            bufferGraphics.drawRect(((i-1)*50),((i-1)*50),50,50);//Display the start screen
            bufferGraphics.drawString(""+i,((i-1)*50)+25,((i-1)*50)+25);//Display the numbers
         }
      }
      g.drawImage(offscreen,0,0,worldx,worldy,this);//Draw the screen
   }// paint()
   public void mouseDragged(MouseEvent e) {
   	
   }
   public void mouseMoved(MouseEvent e){
   
   }
   public void mousePressed(MouseEvent e) 
   {
   
   }
   public void mouseReleased(MouseEvent e) 
   {
   
   }
   public void mouseEntered(MouseEvent e) 
   {
      System.out.println("Mouse entered");
   }
   public void mouseExited(MouseEvent e) 
   {
      System.out.println("Mouse exited");
   }
   public void mouseClicked(MouseEvent e) 
   {
      System.out.println("Mouse clicked (# of clicks: "+ e.getClickCount() + ")");
      int mX=e.getX();
      int mY=e.getY();
      for(int i=3;i<=20;i++)
      {
         if(new Rectangle(((i-1)*50),((i-1)*50),50,50).contains(mX,mY) && sizeD==false)
         {
            makeGrid(i);//Construct a grid based on the chosen number
         }
      }
      for(int ro = 0;ro<rows;ro++)
      {
         for(int col = 0;col<columns;col++)
         {
         /*if(new Rectangle(ro*50,col*50,50,50).contains(mX,mY) && sizeD==true && waiting==false && changeStart==false && changeEnd==false)
         {
         	//System.out.println("Row: "+ro+" is blocked, Column: "+col+" is blocked");
         	blocked[ro][col]=true;
         	grid[ro][col]=999;
         	//travel(new int[rows][columns],sX,sY,destX,destY, cost, 0);
         
         }*/
            if(new Rectangle(ro*50,col*50,50,50).contains(mX,mY) && sizeD==true && waiting==false && changeStart==true && changeEnd==false && blocked[ro][col]==false)
            {
               sY=col;
               sX=ro;
               changeStart=false;
            }
            if(new Rectangle(ro*50,col*50,50,50).contains(mX,mY) && sizeD==true && waiting==false && changeEnd==true && changeStart==false && blocked[ro][col]==false)
            {
               destY=col;
               destX=ro;
               changeEnd=false;
            }
            if(new Rectangle(ro*50,col*50,50,50).contains(mX,mY) && sizeD==true && waiting==false && changeEnd==false && changeStart==false && blocked[ro][col]==false && changeBlock==true)
            {
               blocked[ro][col]=true;
            //grid[ro][col]=999;
               lowestCost+=5000000;
               changeBlock=false;
            }
            if(blocked[ro][col]==true)
            {
               System.out.println("Row: "+ro+" is blocked, Column: "+col+" is blocked");
            }
         }
      
      }
      if(new Rectangle(1050,200,200,100).contains(mX,mY) && sizeD==true && changeStart==false && changeBlock==false && changeEnd==false)
      {
         changeStart=true;
      }//Change Start Rect
      if(new Rectangle(1050,400,200,100).contains(mX,mY) && sizeD==true && changeEnd==false && changeBlock==false && changeStart==false)
      {
         changeEnd=true;
      }//Change End Rect
      if(new Rectangle(1050,600,200,100).contains(mX,mY) && sizeD==true && changeBlock==false && changeStart==false && changeEnd==false)
      {
         changeBlock=true;
      }//Change End Rect
   
   }
   public void keyPressed( KeyEvent event ) 
   {
      String keyin; // define a non‐public variable to hold the string representing the key input
      keyin = ""+event.getKeyText( event.getKeyCode()); 
      System.out.println("Key pressed "+keyin);
   }//keyPressed()
   public void keyReleased( KeyEvent event ) 
   {
      String keyin;
      keyin = ""+event.getKeyText( event.getKeyCode()); 
      System.out.println ("Key released: "+ keyin);
   }//keyReleased()
   public void keyTyped( KeyEvent event ) 
   {
      char keyin;
      keyin = event.getKeyChar(); //getKeyChar() returns the character of the printable key pressed. 
      System.out.println ("Key Typed: "+ keyin);
   }//keyTyped()
   public void makeGrid(int a)
   {
      columns=a;
      rows=a;
      fillGrid();
      sizeD=true;
   }
   public void update (Graphics g) 
   {
      paint(g); 
   }//Update the graphics
   public void run() 
   {
      while(true) // this thread loop forever and runs the paint method and then sleeps.
      {
         if(sizeD==true)
         {
            travel(new int[rows][columns],sX,sY,destX,destY, cost, 0);
            closedList.add(0,new Node(0,0));
            openList.add(0,new Node(0,0));
            aStar(new Node(sX,sY),new Node(0,0),new Node(destX,destY),0);
         //printPath(closedList);
         	//System.out.println("Lowest cost: " + lowestCost);
         	//System.out.println("Lowest steps: " + lowestSteps);
         //printPath(prevBest);  //print prevBest
            waiting=false;
         }
         repaint();
         try {
            thread.sleep(50);
         }
         catch (Exception e){ }
      }//while
   }// run()

}//Applet
/*public int traverse(int steps, int destR, int destC, int curX, int curY)
	{
		int direction = 0;
		if(cost>=lowestCost)
		{
			//System.out.println("Greater cost Base Case");
			direction=4;
		}
		if(curX+1>=destR && curY+1<destC)
		{
			System.out.println("Reached the farthest row Base Case");
			direction=1;
		}
		if(curY+1>=destC && curY+1<destR)
		{
			System.out.println("Reached the farthest columns Base Case");
			direction=2;
		}
		if(curX+1>=destR && curY+1>=destC)
		{
			System.out.println("At destination Base Case");
			direction=4;    
		}
		switch(direction)
		{
			case 0: prevBest[curX][curY]=1;
					cost+=grid[curX][curY];
					System.out.println("the current X position on the GRID is: "+curX+"the current y position on the GRID is: "+curY);
					return(traverse(steps+1,destR,destC,curX+1,curY+1)); //diag

			case 1: prevBest[curX][curY]=1;
					cost+=grid[curX][curY];
					return(traverse(steps+1,destR,destC,curX,curY+1)); //right

			case 2: prevBest[curX][curY]=1;
					cost+=grid[curX][curY];
					return(traverse(steps+1,destR,destC,curX+1,curY));//down

			case 3: 
					return(5000);

			case 4: System.out.println("the Grid's cost is: "+cost);
					return(cost);
			default: return(0);

		}
	}*/ // Previous Trial 1
/*public boolean travel(int[][] path, int cX, int cY, int eX, int eY)
	{
		boolean returned = false;
		System.out.println("cX: "+ cX+" , cY: "+ cY+", eX: "+eX+", eY: " +eY+" Path 1 length: "+path[0].length+" Path 2 length: "+path[1].length);
		path[cX][cY]=1;
		if(cost>lowestCost - grid[cX][cY]){ 
			System.out.println("1");
			return false;
		}
		cost += grid[cX][cY];
		if(cX>=eX && cY>=eY){
			System.out.println("2");
			return true;
		}
		if(cX+1>=eX && cY+1<eY){
			System.out.println("3");
			return false;
		}
		if(cY+1>=eY && cX+1<eX){
			System.out.println("4");
			return false;
		}
		else{
		if(travel(path,cX+1,cY+1,eX,eY)==true && cX+1<=eX && cY+1<=eY){
			System.out.println("the current X position on the GRID is: "+cX+"the current y position on the GRID is: "+cY);
			returned=true;
			replace(prevBest, path);
		}
		if(travel(path,cX,cY+1,eX,eY)==true && cX+1<=eX && cY+1<=eY){
			System.out.println("the current X position on the GRID is: "+cX+"the current y position on the GRID is: "+cY);
		
			returned=true;
			replace(prevBest, path);
		}
		if(travel(path,cX+1,cY,eX,eY)==true && cX+1<=eX && cY+1<=eY){
			System.out.println("the current X position on the GRID is: "+cX+"the current y position on the GRID is: "+cY);
		
			returned=true;
			replace(prevBest, path);
		}
	

		return(returned);

	}*/ // Previous Trial 2