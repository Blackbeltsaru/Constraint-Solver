/*==============================
 * Name:   Ryan Biggs
 * Date:   10/15/14
 * Course: CSE421
 *==============================*/

import abscon.instance.Toolkit;
import abscon.instance.components.PConstraint;

public class IntensionConstraint extends Constraint
{
	private String[] predicateUniversalPostfixExpression;	//Holds the postfix expression for the Intension constraint
	private String[] parameters;							//Holds the parameters for the intension constraints
	
	/*====================
	 * Constructor
	 *====================*/
	public IntensionConstraint(String name, PConstraint constraint, String[] predicateUniversalPostfixExpression, String[] parameters)
	{
		super(name, constraint);
		
		this.predicateUniversalPostfixExpression = predicateUniversalPostfixExpression;
		this.parameters = parameters;
	}//End Constructor
	
	/*====================
	 * Prints the Intension Constraint
	 *====================*/
	public void Print()
	{
		//TODO Make easier to read
		System.out.print("Name: " + this.name + ", variables: {");
		int scopeLength = scope.size();
		for(int i = 0; i < scopeLength - 1; i++)
		{
			System.out.print(scope.get(i).getName() + ", ");
		}//End Scope iteration
		System.out.print(scope.get(scopeLength - 1).getName() + "}, definition: Intension, Function: \"" 
				+ Toolkit.buildStringFromTokens(this.predicateUniversalPostfixExpression) + "\", Parameters: {" );
		for(int i = 0; i < parameters.length - 1; i++)
		{
			System.out.print(parameters[i] + ", ");
		}//End parameter iteration
		System.out.print(parameters[parameters.length -1] + "}");
		System.out.println();
	}
	
	/*====================
	 * Checks if this is equal 
	 * to the given object
	 *====================*/
	public boolean equals(Object obj)
	{
		//TODO Check name
		return true;
	}
}
