/*==============================
 * Name:   Ryan Biggs
 * Date:   10/15/14
 * Course: CSE421
 *==============================*/

import java.util.*;

import abscon.instance.tools.InstanceParser;
import abscon.instance.components.PVariable;
import abscon.instance.components.PDomain;
import abscon.instance.components.PConstraint;
import abscon.instance.components.PIntensionConstraint;
import abscon.instance.components.PExtensionConstraint;

public class ProblemSolver 
{
	private int 					cc;							//Holds the number of constraint checks
	private int						nv;							//Holds the number of nodes visited
	private int						bt;							//Holds the number backtracks
	private int 					fval;						//Holds the number of values removed from the domains of variables
	private float 					iSize;						//Holds the initial size of the CSP
	private float 					fSize;						//Holds the final size of the CSP
	private String					varOrdering;				//Holds the variable ordering heuristic
	private String					varDynamic;					//Holds the static or dynamic status of the variable ordering
	private String 					name;						//Holds the name of the problem
	private ArrayList<Variable> 	variables;					//Holds all of the variables of the problem
	private ArrayList<Variable>		unassigned;					//Holds all of the unassigned variables
	private ArrayList<Variable>		assigned;					//Holds all of the assigned variables
	private ArrayList<Constraint> 	constraints;				//Holds all of the constraints of the problem

	/*====================
	 * Constructor
	 *====================*/
	public ProblemSolver(InstanceParser parser, String ordering, String dynamic)
	{
		//Sets initial value for variables
		this.cc = 0;
		this.nv = 0;
		this.fval = 0;
		this.fSize = 1;
		this.varOrdering = ordering;
		if(this.varOrdering == null || this.varOrdering.equalsIgnoreCase(""))
			this.varOrdering = "LX";
		this.varDynamic = dynamic;
		this.name = parser.getName();
		this.variables = new ArrayList<Variable>();
		this.assigned = new ArrayList<Variable>();
		this.constraints = new ArrayList<Constraint>();		

		//****************************************
		//Creates Variables
		//****************************************
		PVariable[] variableList = parser.getVariables();
		int numVariables = variableList.length;

		//Iterates over variables
		for(int i = 0; i < numVariables; i++)
		{
			//Populates needed information from CSP Parser
			String varName = variableList[i].getName();
			PDomain domain = variableList[i].getDomain();
			int[] values = domain.getValues();
			int domainLength = values.length;
			int[] varValues = new int[domainLength];

			//Copies domain values to a new array
			for(int j = 0; j < domainLength; j++)
			{
				varValues[j] = values[j];
			}//End domain copy

			//Creates the CSP Variable
			Variable temp = new Variable(varName, varValues, ordering);

			//Adds the variable to the list of variables
			this.variables.add(temp);

		}//End Variable iteration

		//****************************************
		//Creates Constraints
		//****************************************
		Map<String, PConstraint> constraintList = parser.getMapOfConstraints();
		//Iterates over every value in the constraint list
		for(Map.Entry<String, PConstraint> entry : constraintList.entrySet())
		{
			//Creates variables needed and get generic information
			Constraint temp = null;
			PConstraint constraint = entry.getValue();
			String constName = constraint.getName();

			//Gets information for intension constraints
			if(constraint instanceof PIntensionConstraint)
			{
				String[] universalExpress = ((PIntensionConstraint) constraint).getFunction().getUniversalPostfixExpression();
				String[] parameters = ((PIntensionConstraint) constraint).getParameters();
				temp = new IntensionConstraint(constName, constraint, universalExpress, parameters);
			}//End intension check

			//Gets information for Extension constraints
			else if(constraint instanceof PExtensionConstraint)
			{
				int[][] tuples = ((PExtensionConstraint) constraint).getRelation().getTuples();
				int[][] tuplesCopy = new int[tuples.length][tuples[0].length];

				//Copies the Tuple values to a new array for the extension constraint
				for(int i = 0; i < tuples.length; i++)
				{
					for(int j = 0; j < tuples[0].length; j++)
					{
						tuplesCopy[i][j] = tuples[i][j];
					}//End
				}//End Tuple Copying

				//Gets semantic information
				String semantics = ((PExtensionConstraint) constraint).getRelation().getSemantics();
				temp = new ExtensionConstraint(constName, constraint, tuplesCopy, semantics);
			}//End extension check

			//Processes any remaining constraints
			else
			{
				System.out.println("Unhandled Constraint within the CSP");
				temp = new GlobalConstraint(constName, constraint);
			}//End default case	
			this.constraints.add(temp);			

			//Parse Scope
			PVariable[] constScope = constraint.getScope();

			//Iterate over all variables in the scope
			int scopeLength = constScope.length;
			for(int i = 0; i < scopeLength; i++)
			{
				//Grabs the variable from the list of variables
				Variable tempVariable = null;
				String tempName = constScope[i].getName();
				int varSize = this.variables.size();
				for(int j = 0; j < varSize; j++)
				{
					if(this.variables.get(j).getName().equals(tempName))
					{
						tempVariable = this.variables.get(j);
					}//End equivilance check
				}//looks through all of the variables to find the correct one

				//Adds the variable to the scope of the current constraint
				temp.addScope(tempVariable);

				//Adds the constraint to the variables constraint list
				tempVariable.addConstraint(temp);

				//Adds all other variables associated with this constraint to the neighbor list of the variable
				for(int j = 0; j < scopeLength; j++)
				{
					if(j != i)
					{
						//Grab the variable from the list of variables
						Variable neighbor = null;
						String neighborName = constScope[j].getName();
						for(int w = 0; w < varSize; w++)
						{
							if(this.variables.get(w).getName().equals(neighborName))
							{
								neighbor = this.variables.get(w);
							}//End equal check
						}//End variable look through

						//Add the variable to the list of neighbors
						tempVariable.addNeighbor(neighbor, temp);

					}//End check to ensure not adding itself to the neighborhood
				}//End iteration through scope				
			}//End Scope iteration			
		}//End constraint iteration

		//Sorts the lists by lexiographic order
		Collections.sort(this.variables);
		Collections.sort(this.constraints);
		
		this.unassigned = this.variables;

		//Computes the initial size
		//Calculate initial size
		iSize = 1;
		int length = variables.size();
		for(int i = 0; i < length; i++)
		{
			iSize *= Math.log(variables.get(i).getCurrDomain().size());
		}
	}//End Constructor
	
	/*====================
	 * Returns the name of the problem
	 *====================*/
	public String getName()
	{
		return this.name;
	}//End getName
	
	/*====================
	 *Returns the number of constraint checks
	 *====================*/
	public int getCC()
	{
		return this.cc;
	}//End getCC
	
	/*====================
	 *Returns the number of nodes visited
	 *====================*/
	public int getNV()
	{
		return this.nv;
	}//End getCC
	
	/*====================
	 *Returns the number of backtrack occurences
	 *====================*/
	public int getBT()
	{
		return this.bt;
	}//End getCC
	
	/*====================
	 *Returns the number of removes values
	 *====================*/
	public int getFval()
	{
		return this.fval;
	}//End getFval
	
	/*====================
	 *Returns the initial size of the CSP
	 *====================*/
	public float getISize()
	{
		return this.iSize;
	}//End getIsize
	
	/*====================
	 *Returns the final size of the CSP
	 *====================*/
	public float getFSize()
	{
		//Calculate final size
		fSize = 1;
		int length = variables.size();
		for(int i = 0; i < length; i++)
		{
			fSize *= Math.log(variables.get(i).getCurrDomain().size());
		}//End variable iteration
		return this.fSize;
	}//End getFSize
	
	/*====================
	 * Prints the problem
	 *====================*/
	public void Print()
	{
		System.out.println("Instance Name: " + this.name);
		
		//Iterate through variables
		System.out.println("Variables");		
		int length = this.variables.size();
		for(int i = 0; i < length; i++)
		{
			this.variables.get(i).Print();
		}//End variable printing
		
		//Iterate over constraints
		System.out.println("Constraints:");
		length = this.constraints.size();
		for(int i = 0; i < length; i++)
		{
			this.constraints.get(i).Print();
		}//End constraint printing
	}//End Print
	
	/*====================
	 * Checks consistency for each node
	 *====================*/
	public void nodeConsistency()
	{
		int length = this.constraints.size();
		
		//Iterates over every constraint
		for(int i = 0; i < length; i++)
		{
			Constraint temp = this.constraints.get(i);
			int arity = temp.getArity();
			
			//If arity is 1, check each value in the constraint for consistancy
			if(arity == 1)
			{
				ArrayList<Variable> scope = temp.getScope();
				Variable variable = scope.get(0);
				ArrayList<Integer> domain = variable.getCurrDomain();
				int domainSize = domain.size();
				
				//Iterate over domain
				for(int j = domainSize - 1; j >= 0; j--)
				{
					int tuple[] = new int[1];
					tuple[0] = domain.get(j);
					long cost = temp.computeCostOf(tuple);
					this.cc++;
					
					//Removes failed checks
					if(cost == 1)
					{
						variable.removeFromDomain(tuple[0]);
						this.fval++;
					}//End remove check
					
					//Check for domain wipeout
					if(variable.getCurrDomain().size() == 0)
					{
						//Domain Wipeeout
						throw new DomainWipeoutException();
					}//End domain wipeout check
				}//End domain iteration
				
				variable.arcConsistent();
				
			}//End arity check
		}//End Constraint iteration
	}//End nodeConsistency
	
	/*====================
	 * Runs AC1 on the problem
	 *====================*/
	public void ac1()
	{			
		//Build the queue
		ArrayList<Variable[]> queue = new ArrayList<Variable[]>();
		int variableLength = this.variables.size();
		for(int i = 0; i < variableLength; i++)
		{
			for(int j = i + 1; j < variableLength; j++)
			{
				Variable[] entry = new Variable[2];
				entry[0] = this.variables.get(i);
				entry[1] = this.variables.get(j);
				queue.add(entry);
				entry = new Variable[2];
				entry[0] = this.variables.get(j);
				entry[1] = this.variables.get(i);
				queue.add(entry);
			}//End inner loop
		}//End queue builder
		
		boolean update;
		int queueLength = queue.size();
		
		//Iterate until nothing has changed
		do
		{
			update = false;
			//For each entry in the queue
			for(int i = 0; i < queueLength; i++)
			{
				Variable[] entry = queue.get(i);
				Boolean reviseVal = null;
				try
				{
					reviseVal = this.revise(entry[0], entry[1]);
					update = update || reviseVal;
				}//look for domain wipeout
				catch(DomainWipeoutException e)
				{
					throw new DomainWipeoutException();
				}//Process domain wipeout
			}//End queue iteration
			
		}while (update);//End iteration while things have changed  
		
		for(int i = 0; i < variableLength; i++)
		{
			this.variables.get(i).arcConsistent();
		}//End arc consistent loop
	}//End AC1
	
	/*====================
	 * Runs AC3 on the problem
	 *====================*/
	public void ac3()
	{
		//Build the initial queue
		ArrayList<Variable[]> queue = new ArrayList<Variable[]>();
		int variableLength = this.variables.size();
		for(int i = 0; i < variableLength; i++)
		{
			for(int j = i + 1; j < variableLength; j++)
			{
				Variable[] entry = new Variable[2];
				entry[0] = this.variables.get(i);
				entry[1] = this.variables.get(j);
				queue.add(entry);
				entry = new Variable[2];
				entry[0] = this.variables.get(j);
				entry[1] = this.variables.get(i);
				queue.add(entry);
			}//End inner loop
		}//End queue builder
		
		//Loop through AC 3 while there are still entries in the queue
		while(queue.size() != 0)
		{
			//Pop the first entry
			Variable[] entry = new Variable[2];
			entry = queue.get(0);
			queue.remove(0);
			Boolean reviseVal = null;
	
			try
			{
				//Revise the entry
				reviseVal = this.revise(entry[0], entry[1]);
				//check for changes
				if(reviseVal)
				{
					//add to queue vx, v1 where vx is in neighbor of v1 and is not equal to v2
					ArrayList<Variable> neighbor = entry[0].getNeighbors();
					int neighborLength = neighbor.size();
					for(int i = 0; i < neighborLength; i++)
					{
						if(!(neighbor.get(i).equals(entry[1])))
						{
							Variable[] newEntry = new Variable[2];
							newEntry[0] = neighbor.get(i);
							newEntry[1] = entry[0];
							//Ensure that vx, v1 is not already in the queue
							boolean queueContains = false;
							int queueLength = queue.size();
							for(int j = 0; j < queueLength; j++)
							{
								if(Arrays.equals(queue.get(j), newEntry))
								{
									queueContains = true;
								}//End equalCheck
							}//End queue iteration
							if(!queueContains)
							{
								queue.add(newEntry);
							}//End adding entry	
						}//End check for v2
					}//End neighbor iteration
				}//End change check				
			}//Look for errors
			catch(DomainWipeoutException e)
			{
				throw new DomainWipeoutException();
			}//Process domain wipeout
		}//End queue iteration
		
		for(int i = 0; i < variableLength; i++)
		{
			this.variables.get(i).arcConsistent();
		}//End arc consistent loop
	}//End ac3
	
	/*====================
	 * Checks whether the given 
	 * variable value pair is 
	 * valid
	 *====================*/
	public boolean check(Variable v1, int d1, Variable v2, int d2)
	{
		//Get shared constraint between v11 and v2
		Constraint share = v1.getSharedConstraint(v2);
		boolean returnVal = false;
		long costOf = -1;
		if(share != null)
		{
			//Check if the given tuple is valid in the constraint
			PVariable[] scope = share.getOriginalConstraint().getScope();
			
			//Check the order the variables need to go in
			if(scope[0].getName().equalsIgnoreCase(v1.getName()) && scope[1].getName().equalsIgnoreCase(v2.getName()))
			{
				int[] tuple = {d1, d2};
				costOf = share.computeCostOf(tuple);
				this.cc++;
			}//End case where v1 goes first
			else if(scope[0].getName().equalsIgnoreCase(v2.getName()) && scope[1].getName().equalsIgnoreCase(v1.getName()))
			{
				int[] tuple = {d2, d1};
				costOf = share.computeCostOf(tuple);
				this.cc++;
			}//End case where v2
			else
			{
				System.out.println("Could not determine order of scope in order to check");
				//TODO throw error
			}//end case where scope doesn't match
		}//end check for null shared constraint
		else
		{
			//System.out.println("Null Shared Constraint");
			//TODO throw error
		}//end error block
		
		if(costOf == 0)
		{
			returnVal = true;
		}//End supported case
		else if(costOf == 1)
		{
			returnVal = false;
		}//End not supported case
		else
		{
			//System.out.println("Tuple cost not correct");
			returnVal = true;
			//TODO throw error
		}//End default case
		
		return returnVal;
	}//End check
	
	/*====================
	 * Checks to see if the 
	 * given value in the 
	 * domain of a variable
	 * has a support in the 
	 * second variable
	 *====================*/
	public boolean supported(Variable v1, int d1, Variable v2)
	{
		//Checks the value of d1 against every value in the domain of v2 for support
		ArrayList<Integer> domain = v2.getCurrDomain();
		int length = domain.size();
		
		for(int i = 0; i < length; i++)
		{
			int val = domain.get(i);
			//Check the value for support
			if(this.check(v1, d1, v2, val))
			{
				return true;
			}//End check tuple
		}//End iteration through domain
		
		return false;
	}//End support
	
	/*====================
	 * Revises the domain of v1
	 * given the variable v2
	 *====================*/
	public Boolean revise(Variable v1, Variable v2)
	{
		boolean returnVal = false;
		//Checks to make sure there is a constraint between the two variables
		if(v1.hasNeighbor(v2))
		{
			Constraint tempCheck = v1.getSharedConstraint(v2);
			if(tempCheck.getArity() == 2)
			{
				ArrayList<Integer> domain = v1.getCurrDomain();
				int domainLength = domain.size();
				//Iterate over every value in the domain of v1
				for(int i = (domainLength - 1); i >= 0; i--)
				{
					//Checks the support status for the current value
					int val = domain.get(i);
					boolean support = this.supported(v1, val, v2);
					
					//Removes not supported values from domain
					if(!support)
					{
						v1.removeFromDomain(val);
						this.fval++;
						returnVal = true;
					}//End support check
					
					//Check for domain wipeout
					if(v1.getCurrDomain().size() == 0)
					{
						throw new DomainWipeoutException();
					}//End domain wipeout check
				}//End domain iteration
			}//End arity check
		}//End neighbor check
		
		return returnVal;
	}//End revise
	
	/*====================
	 * Searches for a solution using
	 * simple backtrack search
	 *====================*/
	public ArrayList<String[]> backtrackSearch(boolean all)
	{
		//TODO
		Collections.sort(this.variables);
		String status = null;
		Variable temp = null;
		ArrayList<String[]> solutionSet = new ArrayList<String[]>();
		//Gets the starting variable to instantiate
		ArrayList<Variable> currPath = this.requestNextVariable();
		
		//Checks to ensure there was not an error with getting variables
		if(currPath == null)
		{
			status = "Impossible";
		}//End check
		else
		{
			temp = currPath.get(currPath.size() -1);
		}//End else check
		
		while(status == null)
		{
			//Labels the incoming variable
			currPath = this.label(currPath);
			if(currPath == null)
			{
				int size = this.assigned.size();
				String[] solution = new String[size];
				for(int i = 0; i < size; i++)
				{
					String entry = this.assigned.get(i).getName() + ": " + this.assigned.get(i).getAssignment() + "  ::  ";
					solution[i] = entry;
				}//End solution iteration
				solutionSet.add(solution);
				if(all)
				{
					Variable tempVar = this.assigned.get(this.assigned.size() -1);
					tempVar.removeFromDomain(tempVar.getAssignment());
					tempVar.removeAssignment();
					currPath = this.assigned;
				}//end look for all solutoins
				else
				{
					status = "Solved";
					return solutionSet;
				}//End changing
			}//End solution check
			temp = currPath.get(currPath.size() -1); 
			//If temp failed
			if(temp.getCurrDomain().size() == 0)
			{
				//Unlable current variable
				currPath = this.unlabel();
			}//End unlabel check
			
			//Check for base case
			if(currPath == null)
			{
				status = "Impossible";
			}//End impossible check
			else
			{
				temp = currPath.get(currPath.size() - 1);
			}
		}//End loop until a status is determined
		
		return solutionSet;
	}//end backtrackSearch
	
	/*====================
	 * Unlables the given variable
	 *====================*/
	public ArrayList<Variable> unlabel()
	{
		int jumpTo = this.assigned.size() - 2;
		this.bt++;
		if(jumpTo < 0)
			return null;
		for(int i = this.assigned.size() - 1; i >= jumpTo + 1; i--)
		{
			Variable temp = this.assigned.get(i);
			temp.restore();
			temp.removeAssignment();
			this.unassigned.add(0, temp);
			this.assigned.remove(i);
		}//End unassigning of variables
		
		Variable temp = this.assigned.get(jumpTo);
		temp.removeFromDomain(temp.getAssignment());
		temp.removeAssignment();
		return this.assigned;
	}//end unlable
	
	/*====================
	 * Labels the given variable
	 *====================*/
	public ArrayList<Variable> label(ArrayList<Variable> vars)
	{
		boolean consistent = false;
		int varSize = vars.size() - 1;
		Variable currVar = vars.get(varSize);
		ArrayList<Integer> domain = currVar.getCurrDomain();
		//for each value in the domain i 
		for(int i = 0; i < domain.size(); i++)
		{
			consistent = true;
			currVar.setAssignment(currVar.getCurrDomain().get(i));
			this.nv++;
			//for each previous assignment h
			for(int j = 0; j < varSize; j++)
			{
				//consistent = consistent && check i, h
				consistent = consistent && this.check(currVar, currVar.getAssignment(), vars.get(j), vars.get(j).getAssignment());
			}//End iteraion through previous 
			//if consistent
			if(consistent)
			{
				//initialize next variable
				return this.requestNextVariable();
			}//End consistent check
			//else
			else
			{
				//remove i from current domain
				currVar.removeFromDomain(currVar.getCurrDomain().get(i));
				currVar.removeAssignment();
				i--;
			}//End remov
		}//End domain iteration

		return vars;
	}//end label
	
	/*====================
	 * Returns the next variable to instantiate 
	 * for search
	 *====================*/
	public ArrayList<Variable> requestNextVariable()
	{
		if(this.unassigned.size() > 0)
		{
			this.assigned.add(this.unassigned.get(0));
			this.unassigned.remove(0);
			return this.assigned;
		}//end if there are variables to assign
		return null;
	}//end label
}//End Class
