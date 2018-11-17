/*==============================
 * Name:   Ryan Biggs
 * Date:   10/15/14
 * Course: CSE421
 *==============================*/

import abscon.instance.components.PConstraint;

public class ExtensionConstraint extends Constraint
{
	private boolean support;	//Holds information as to whether the tuples are supports or no goods
	private int[][] tuples;		//HOlds the list of tuples for the constraint
	
	/*====================
	 * Constructor
	 *====================*/
	public ExtensionConstraint(String name, PConstraint constraint, int[][] tuples, String symantics)
	{
		super(name, constraint);
		
		this.tuples = tuples;
		if(symantics.equalsIgnoreCase("conflicts"))
			support = false;
		else if(symantics.equalsIgnoreCase("supports"))
			support = true;
	}//End Constructor
	
	/*====================
	 * Prints the Extension Constraint
	 *====================*/
	public void Print()
	{
		//TODO make easier to read
		System.out.print("Name: " + this.name + ", variables: {");
		int scopeLength = scope.size();
		for(int i = 0; i < scopeLength - 1; i++)
		{
			System.out.print(scope.get(i).getName() + ", ");
		}//End Scope iteration
		System.out.print(scope.get(scopeLength - 1).getName() + "}, definition: ");
		if(support == true)
			System.out.print("supports {");
		else
			System.out.print("Conflicts {");
		int toupleRow = this.tuples.length;
		int toupleCol = this.tuples[0].length;
		for(int i = 0; i < toupleRow - 1; i++)
		{
			System.out.print("(");
			for(int j = 0; j < toupleCol - 1; j++)
			{
				System.out.print(tuples[i][j] + ",");
			}//End tuple column
			System.out.print(tuples[i][toupleCol - 1] + "),");
		}//End tuple row iteration
		System.out.print("(");
		for(int j = 0; j < toupleCol - 1; j++)
		{
			System.out.print(tuples[toupleRow - 1][j] + ",");
		}//end printing last tuple
		System.out.print(tuples[toupleRow - 1][toupleCol - 1] + ")}");
		System.out.println();
		
		
	}//End Print
	
	/*====================
	 * Checks if this is equal 
	 * to the given object
	 *====================*/
	public boolean equals(Object obj)
	{
		//TODO Check name
		return true;
	}
}//End Class
