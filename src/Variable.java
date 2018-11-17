/*==============================
 * Name:   Ryan Biggs
 * Date:   10/15/14
 * Course: CSE421
 *==============================*/

import java.util.*;

public class Variable implements Comparable<Variable>
{
	Integer assignment;									//Holds the current assignemtn of the variable
	String orderHeuristic;							//Holds the ordering heuristic for the variable
	private String name;							//Holds the name of the Variable
	private int[]  initDomain;						//Holds the initial domain of the variable
	private ArrayList<Integer>	  	currDomain;		//Holds the current domain of the variable
	private ArrayList<Constraint>	constraints;	//Holds all constraints that affect this variable
	private ArrayList<NeighborPair> neighbors;		//Holds all neighbors of the variable
	
	/*====================
	 *Nested class to keep track of constraits linking neighbors 
	 *====================*/
	private class NeighborPair	
	{
		private Variable neighbor;
		private Constraint link;
		
		//Constructor
		public NeighborPair(Variable neighbor, Constraint link)
		{
			this.neighbor = neighbor;
			this.link = link;
		}//End Constructor
		
		public Variable getNeighbor()
		{
			return this.neighbor;
		}//End getNeighbor
		
		public Constraint getLink()
		{
			return this.link;
		}//End getLink
	}//End neighborPair
	
	/*====================
	 * Constructor
	 *====================*/
	public Variable(String name, int[] initDomain, String order)
	{
		this.name = name;
		this.initDomain = initDomain;
		this.constraints = new ArrayList<Constraint>();
		this.neighbors = new ArrayList<NeighborPair>();
		this.orderHeuristic = order;
		
		int length = this.initDomain.length;
		this.currDomain = new ArrayList<Integer>();
		for(int i = 0; i < length; i++)
		{
			this.currDomain.add((Integer)initDomain[i]);
		}//End domain copy
	}//End Constructor
	
	/*====================
	 * Adds given constraint to constraint list of the variable
	 *====================*/
	public void addConstraint(Constraint constraint)
	{
		this.constraints.add(constraint);
	}//End addConstraint
	
	/*====================
	 * Adds given variable to the neighbor list of the variable
	 *====================*/
	public void addNeighbor(Variable variable, Constraint link)
	{
		//TODO fix this to index reference? figure it out
		NeighborPair temp = new NeighborPair(variable, link);
		this.neighbors.add(temp);
	}//End addNeighbor
	
	/*====================
	 * Returns variable name
	 *====================*/
	public String getName()
	{
		return this.name;
	}//End getName
	
	/*====================
	 * Returns variable constraints
	 *====================*/
	public ArrayList<Constraint> getConstraints()
	{
		return this.constraints;
	}
	
	/*====================
	 * Returns current domain
	 *====================*/
	public ArrayList<Integer> getCurrDomain()
	{
		return this.currDomain;
	}//End getCurrDomain
	
	/*====================
	 * Returns the list of neighbors
	 *====================*/
	public ArrayList<Variable> getNeighbors()
	{
		ArrayList<Variable> neighborList = new ArrayList<Variable>();
		int length = neighbors.size();
		for(int i = 0; i < length; i++)
		{
			Variable variable = neighbors.get(i).getNeighbor();
			neighborList.add(variable);
		}//End loop
		return neighborList;
	}//End getNeighbors
	
	/*====================
	 * Returns the variables current assignment
	 *====================*/
	public Integer getAssignment()
	{
		return this.assignment;
	}//End getAssignment
	
	/*====================
	 * Sets the variables current assignment
	 *====================*/
	public void setAssignment(int a)
	{
		this.assignment = a;
	}//End setAssignment
	
	/*====================
	 *Removes the current assignemtn
	 *====================*/
	public void removeAssignment()
	{
		this.assignment = (Integer)null;
	}//End setAssignment
	
	/*====================
	 * Removes the given value from the current domain
	 *====================*/
	public void removeFromDomain(Integer val)
	{
		currDomain.remove(val);
	}
	
	/*====================
	 * Determines if the given variable is a neighbor
	 *====================*/
	public boolean hasNeighbor(Variable neighbor)
	{
		int length = this.neighbors.size();
		for(int i = 0; i < length; i++)
		{
			if(this.neighbors.get(i).getNeighbor().equals(neighbor))
			{
				return true;
			}//End check
		}//End loop through neighbors
		return false;
	}//End has neighbor
	
	/*====================
	 * Returns the name of the constraint shared with the given variable
	 * if variable is not a neighbor return null
	 *====================*/
	public Constraint getSharedConstraint(Variable neighbor)
	{
		int length = this.neighbors.size();
		for(int i = 0; i < length; i++)
		{
			if(this.neighbors.get(i).getNeighbor().equals(neighbor))
			{
				return this.neighbors.get(i).getLink();
			}//End check
		}//End loop through neighbors
		return null;
	}//End getSharedConstraint
	
	/*===================
	 * Moves the currentArc consistent domain
	 * to the unchanging initial domain
	 *===================*/
	public void arcConsistent()
	{
		int size = this.currDomain.size();
		int[] domain = new int[size];
		for(int i = 0; i < size; i++)
		{
			domain[i] = this.currDomain.get(i);
		}//End domain iteration
		
		this.initDomain = domain;
	}//End arcConsistent
	
	/*===================
	 * Restores the current domain of the variable
	 *===================*/
	public void restore()
	{
		int size = this.initDomain.length;
		this.currDomain = new ArrayList<Integer>();
		for(int i = 0; i < size; i++)
		{
			this.currDomain.add(this.initDomain[i]);
		}//End domain iteration
	}//End arcConsistent
 	
	/*====================
	 * Prints the variable
	 *====================*/
	public void Print()
	{
		//TODO make easier to read
		System.out.print("Name: " + this.name + ", initial-domain: {");
		int domainLength = this.initDomain.length;
		for(int i = 0; i < domainLength - 1; i++)
			System.out.print(this.initDomain[i] + ",");
		if(domainLength > 0)
			System.out.print(initDomain[domainLength - 1]);
		System.out.print("}, constraints: {");
		int constLength = this.constraints.size();
		for(int i = 0; i < constLength - 1; i++)
			System.out.print(this.constraints.get(i).getName() + ",");
		if(constLength > 0)
			System.out.print(this.constraints.get(constLength - 1).getName());
		System.out.print("}, neighbors: {");
		int neighLength = this.neighbors.size();
		for(int i = 0; i < neighLength - 1; i++)
			System.out.print(this.neighbors.get(i).getNeighbor().getName() + ",");
		if(neighLength > 0)
			System.out.print(this.neighbors.get(neighLength - 1).getNeighbor().getName());
		System.out.print("}");
		System.out.println();
			
	}//End Print
	
	/*====================
	 * Checks if this is equal 
	 * to the given object
	 *====================*/
	public boolean equals(Object obj)
	{
		if(obj instanceof Variable)
		{
			String objName = ((Variable)obj).getName();
			if(this.name.equals(objName))
				return true;
		}//End class check
		return false;
	}//End equals

	/*====================
	 * Compares this to the given object
	 *====================*/
	public int compareTo(Variable object) 
	{
		if(this.orderHeuristic.equalsIgnoreCase("LX"))
		{
			return this.name.compareTo(((Variable)object).getName());
		}//End lexiographic check
		else if(this.orderHeuristic.equalsIgnoreCase("LD"))
		{
			if(this.currDomain.size() > object.getCurrDomain().size())
			{
				return 1;
			}//end greater
			else if(this.currDomain.size() < object.getCurrDomain().size())
			{
				return -1;
			}//end less
			else
			{
				return this.name.compareTo(((Variable)object).getName());
			}//end equal
		}//end least domain check
		else if(this.orderHeuristic.equalsIgnoreCase("DEG"))
		{
			if(this.constraints.size() > object.getConstraints().size())
			{
				return 1;
			}//end greater
			else if(this.constraints.size() < object.getConstraints().size())
			{
				return -1;
			}//end less
			else
			{
				return this.name.compareTo(((Variable)object).getName());
			}//end equal
		}//end degree domain check
		else if(this.orderHeuristic.equalsIgnoreCase("DD"))
		{
			if((double)this.currDomain.size()/this.constraints.size() > (double)object.getCurrDomain().size()/object.getConstraints().size())
			{
				return 1;
			}//end greater
			else if((double)this.currDomain.size()/this.constraints.size() < (double)object.getCurrDomain().size()/object.getConstraints().size())
			{
				return -1;
			}//end less
			else
			{
				return this.name.compareTo(((Variable)object).getName());
			}//end equal
		}//end domain degree domain check
		
		return this.name.compareTo(((Variable)object).getName());
	}//End compareTo
}//End Class
