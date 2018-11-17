/*==============================
 * Name:   Ryan Biggs
 * Date:   10/15/14
 * Course: CSE421
 *==============================*/

import java.util.*;

import abscon.instance.components.PConstraint;

public abstract class Constraint implements Comparable<Constraint>
{
	protected int arity;						//Holds the arity of the constraint
	protected String name;						//Holds the name of the constraint
	protected ArrayList<Variable> scope;		//Holds the scope of the constraint
	protected PConstraint originalConstraint;	//Holds a link to the orignal constraint
	
	/*====================
	 * Constructor
	 *====================*/
	public Constraint(String name, PConstraint constraint)
	{
		this.name = name;
		this.originalConstraint = constraint;
		this.scope = new ArrayList<Variable>();
	}//End constructor
	
	/*====================
	 * Returns the name of the constraint
	 *====================*/
	public String getName()
	{
		return this.name;
	}//End getName
	
	/*====================
	 * Returns the arity of the constraint
	 *====================*/
	public int getArity()
	{
		return this.arity;
	}
	
	/*====================
	 * Returns the scope of the constraint
	 *====================*/
	public ArrayList<Variable> getScope()
	{
		return this.scope;
	}
	
	/*====================
	 * Adds given variable to the scope of the constraint
	 *====================*/
	public void addScope(Variable variable)
	{
		scope.add(variable);
		arity++;
	}//End addScope
	
	/*====================
	 * Computes the cost of the constraint ie if it is satisfied
	 *====================*/
	public long computeCostOf(int[] tuple)
	{
		return this.originalConstraint.computeCostOf(tuple);
	}//End computeCostOf
	
	/*====================
	 *Returns the originalconstraint
	 *====================*/
	public PConstraint getOriginalConstraint()
	{
		return this.originalConstraint;
	}//End getOriginalConstraint
	
	/*====================
	 * Compares this to the given object
	 *====================*/
	public int compareTo(Constraint object) 
	{
			return this.name.compareTo(((Constraint)object).getName());
	}
	
	/*====================
	 * Prints the Constraint
	 *====================*/
	public abstract void Print();
	
	/*====================
	 * Checks if this is equal 
	 * to the given object
	 *====================*/
	public abstract boolean equals(Object obj);
	
}//End Class
