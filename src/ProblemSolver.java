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
	private int 							cc;							//Holds the number of constraint checks
	private int								nv;							//Holds the number of nodes visited
	private int								bt;							//Holds the number backtracks
	private int 							fval;						//Holds the number of values removed from the domains of variables
	private float 							iSize;						//Holds the initial size of the CSP
	private float 							fSize;						//Holds the final size of the CSP
	private String							varOrdering;				//Holds the variable ordering heuristic
	private String							varDynamic;					//Holds the static or dynamic status of the variable ordering
	private String 							name;						//Holds the name of the problem
	private ArrayList<Variable> 			variables;					//Holds all of the variables of the problem
	private ArrayList<Variable>				unassigned;					//Holds all of the unassigned variables
	private ArrayList<Variable>				assigned;					//Holds all of the assigned variables
	private ArrayList<Constraint> 			constraints;				//Holds all of the constraints of the problem
	private ArrayList<ArrayList<Variable>> 	confSet;					//Holds the conflict set of the problem

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
		this.confSet = new ArrayList<ArrayList<Variable>>();

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
		//Collections.sort(this.variables);
		//Collections.sort(this.constraints);

		int length = variables.size();
		this.unassigned = new ArrayList<Variable>();
		for(int i = 0; i < length; i++)
		{
			this.unassigned.add(this.variables.get(i));
		}//End unassigned copy

		Collections.sort(this.unassigned);
		
		//Computes the initial size
		//Calculate initial size
		iSize = 1;
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
	 * Returns the next variable to instantiate 
	 * for search
	 *====================*/
	public ArrayList<Variable> requestNextVariable()
	{
		//TODO
		if(this.unassigned.size() > 0)
		{
			Variable var = null;
			int removeIndex = - 1;
			if(this.varOrdering.equals("LX") || this.varOrdering.equals("LD") || this.varOrdering.equals("DEG") || this.varOrdering.equals("DD"))
			{
				var = this.unassigned.get(0);
				removeIndex = 0;
			}//End static ordering
			else if(this.varOrdering.equals("dLX"))
			{
				var = this.unassigned.get(0);
				removeIndex = 0;				
				for(int i = 1; i < unassigned.size(); i++)
				{					
					Variable temp = this.unassigned.get(i);

					//Domino Effect
					if(temp.getCurrDomain().size() == 1)
					{
						this.assigned.add(temp);
						this.unassigned.remove(i);
						this.confSet.add(new ArrayList<Variable>());
						return this.assigned;
					}//End domino effect

					if(temp.getName().compareTo(var.getName()) < 0)
					{
						var = temp;
						removeIndex = i;
					}//End check for less
				}//End look at all variables
			}//End dynamic lexiographic ordering
			else if(this.varOrdering.equals("dLD"))
			{
				var = this.unassigned.get(0);
				int value = var.getCurrDomain().size();
				removeIndex = 0;
				for(int i = 1; i < unassigned.size(); i++)
				{
					Variable temp = this.unassigned.get(i);
					int size = temp.getCurrDomain().size();
					if(size == value)
					{
						if(temp.getName().compareTo(var.getName()) < 0)
						{
							var = temp;
							removeIndex = i;
						}//End lexiographic check
					}//End lexiographic order check
					if(size < value)
					{
						value = size;
						var = temp;
						removeIndex = i;						
					}//End less than check
				}//End look at all variables
			}//End dynamic least domain ordering
			else if(this.varOrdering.equals("dDEG"))
			{
				var = this.unassigned.get(0);
				int value = 0;
				ArrayList<Variable> neighbor = var.getNeighbors();
				for(int j = 0; j < neighbor.size(); j++)
				{
					if(this.unassigned.contains(neighbor.get(j)))
					{
						value++;
					}
				}
				removeIndex = 0;
				for(int i = 1; i < unassigned.size(); i++)
				{
					Variable temp = this.unassigned.get(i);
					int size = 0;
					ArrayList<Variable> newNeighbor = temp.getNeighbors();
					for(int j = 0; j < newNeighbor.size(); j++)
					{
						if(this.unassigned.contains(newNeighbor.get(j)))
						{
							size++;
						}
					}

					//Domino Effect
					if(temp.getCurrDomain().size() == 1)
					{
						this.assigned.add(temp);
						this.unassigned.remove(i);
						this.confSet.add(new ArrayList<Variable>());
						return this.assigned;
					}//end domino effect 

					if(size == value)
					{
						if(temp.getName().compareTo(var.getName()) < 0)
						{
							var = temp;
							removeIndex = i;
						}//End lexiographic check
					}//End lexiographic order check
					if(size < value)
					{
						value = size;
						var = temp;
						removeIndex = i;						
					}//End less than check
				}//End look at all variables
			}//End degree domain check
			else if(this.varOrdering.equals("dDD"))
			{
				var = this.unassigned.get(0);
				int degree = 0;
				ArrayList<Variable> neighbor = var.getNeighbors();
				for(int j = 0; j < neighbor.size(); j++)
				{
					if(this.unassigned.contains(neighbor.get(j)))
					{
						degree++;
					}
				}
				double value = (double)var.getCurrDomain().size() / (double)degree;
				removeIndex = 0;
				for(int i = 1; i < unassigned.size(); i++)
				{
					Variable temp = this.unassigned.get(i);
					int newDegree = 0;
					ArrayList<Variable> newNeighbor = var.getNeighbors();
					for(int j = 0; j < newNeighbor.size(); j++)
					{
						if(this.unassigned.contains(newNeighbor.get(j)))
						{
							newDegree++;
						}
					}
					double size = (double)temp.getCurrDomain().size() / (double)newDegree;


					//Domino Effect
					if(temp.getCurrDomain().size() == 1)
					{
						this.assigned.add(temp);
						this.unassigned.remove(i);
						this.confSet.add(new ArrayList<Variable>());
						return this.assigned;
					}//End domino effect

					if(size == value)
					{
						if(temp.getName().compareTo(var.getName()) < 0)
						{
							var = temp;
							removeIndex = i;
						}//End lexiographic check
					}//End lexiographic order check
					if(size < value)
					{
						value = size;
						var = temp;
						removeIndex = i;						
					}//End less than check
				}//End look at all variables
			}//End domain degree domain check

			this.assigned.add(var);
			this.confSet.add(new ArrayList<Variable>());
			this.unassigned.remove(removeIndex);
			return this.assigned;
		}//end if there are variables to assign
		return null;
	}//end label

	//back jumping
	//====================================================================================================================
	//====================================================================================================================
	//====================================================================================================================

	/*====================
	 * Searches for a solution using
	 * simple backtrack search
	 *====================*/
	public ArrayList<Integer[]> backtrackSearch(boolean all)
	{
		String status = null;
		Variable temp = null;
		ArrayList<Integer[]> solutionSet = new ArrayList<Integer[]>();
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
				Integer[] solution = new Integer[size];
				for(int i = 0; i < size; i++)
				{
					Integer entry = this.variables.get(i).getAssignment();
					solution[i] = entry;
				}//End solution iteration
				solutionSet.add(solution);
				if(all)
				{
					Variable tempVar = this.assigned.get(this.assigned.size() -1);
					tempVar.removeFromDomain(tempVar.getAssignment());
					tempVar.removeAssignment();
					currPath = this.assigned;
					int length = this.assigned.size() - 1;
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
		//Find the deepest conflict in the conflict list
		int jumpTo = this.assigned.size() - 2;
		this.bt++;
		if(jumpTo < 0)
			return null;
		
		for(int i = this.assigned.size() - 1; i > jumpTo; i--)
		{
			Variable temp = this.assigned.get(i);
			temp.restore();
			this.unassigned.add(0, temp);
			this.assigned.remove(i);
		}
		
		Variable current = this.assigned.get(jumpTo);
		current.removeFromDomain(current.getAssignment());
		current.removeAssignment();
		
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
				boolean check = this.check(currVar, currVar.getAssignment(), vars.get(j), vars.get(j).getAssignment());
				consistent = consistent && check;
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

	//====================================================================================================================
	//====================================================================================================================
	//====================================================================================================================

	//Conflict directed back jumping
	//====================================================================================================================
	//====================================================================================================================
	//====================================================================================================================

	/*====================
	 * Searches for a solution using
	 * simple backtrack search
	 *====================*/
	public ArrayList<Integer[]> cbjSearch(boolean all)
	{
		String status = null;
		Variable temp = null;
		ArrayList<Integer[]> solutionSet = new ArrayList<Integer[]>();
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
			currPath = this.cbjLabel(currPath);
			if(currPath == null)
			{
				int size = this.assigned.size();
				Integer[] solution = new Integer[size];
				for(int i = 0; i < size; i++)
				{
					Integer entry = this.variables.get(i).getAssignment();
					solution[i] = entry;
				}//End solution iteration
				solutionSet.add(solution);
				if(all)
				{
					Variable tempVar = this.assigned.get(this.assigned.size() -1);
					tempVar.removeFromDomain(tempVar.getAssignment());
					tempVar.removeAssignment();
					currPath = this.assigned;
					ArrayList<Variable> set = this.confSet.get(this.confSet.size() - 1);
					int length = this.assigned.size() - 1;
					for(int i = 0; i < length; i++)
					{
						if(!(confSet.contains(this.assigned.get(i))))
							set.add(this.assigned.get(i));
					}
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
				currPath = this.cbjUnlabel();
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
	public ArrayList<Variable> cbjUnlabel()
	{
		//Find the deepest conflict in the conflict list
		ArrayList<Variable> set = this.confSet.get(this.confSet.size() - 1);
		int jumpTo = this.assigned.size() - 2;
		this.bt++;
		for(int i = jumpTo; i >= 0; i--)
		{
			if(set.contains(this.assigned.get(i)))
			{
				break;
			}//End check to see if this var is in the conflict list
		}//End finding deepest conflictS

		if(jumpTo < 0)
			return null;

		//Adds conflict sets together
		ArrayList<Variable> newSet = this.confSet.get(jumpTo);
		for(int i = 0; i < set.size(); i++)
		{
			if(!(newSet.contains(set.get(i))) || !(set.get(i).equals(this.assigned.get(jumpTo))))
			{
				newSet.add(set.get(i));
			}//End filter check
		}//end set combination


		for(int i = this.assigned.size() - 1; i > jumpTo; i--)
		{
			Variable temp = this.assigned.get(i);
			temp.restore();
			temp.removeAssignment();
			this.unassigned.add(0, temp);
			this.assigned.remove(i);
			this.confSet.remove(i);
		}//End unassigning of variables

		Variable temp = this.assigned.get(jumpTo);
		temp.removeFromDomain(temp.getAssignment());
		temp.removeAssignment();
		return this.assigned;
	}//end unlable

	/*====================
	 * Labels the given variable
	 *====================*/
	public ArrayList<Variable> cbjLabel(ArrayList<Variable> vars)
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
				boolean check = this.check(currVar, currVar.getAssignment(), vars.get(j), vars.get(j).getAssignment());
				consistent = consistent && check;

				//Update conflict set
				if(!check)
				{
					if(!(this.confSet.get(confSet.size() -1).contains(vars.get(j))))
					{
						this.confSet.get(confSet.size() - 1).add(vars.get(j));
						break;
					}
				}//End update of conflict set
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

	//====================================================================================================================
	//====================================================================================================================
	//====================================================================================================================

	//Forward Checking
	//====================================================================================================================
	//====================================================================================================================
	//====================================================================================================================
	
	/*
	 * Prunes the domain of future to make it consistant with past
	 */
	public boolean forwardCheck(Variable past, Variable future)
	{
		ArrayList<Integer> reduction = new ArrayList<Integer>();
		ArrayList<Integer> domain = future.getCurrDomain();
		for(int i = 0; i < domain.size(); i++)
		{
			Integer value = domain.get(i);
			boolean checkVal = this.check(past, past.getAssignment(), future, value);
			if(!checkVal)
			{
				reduction.add(value);
				future.removeFromDomain(value);
				i--;
			}//End check for not valid values
		}//End domain iteration
		future.pushReduction(reduction);
		past.pushFuture(future);
		future.pushPast(past);
		if(domain.size() <= 0)
		{
			return false;
		}//End domain wipout check		
		return true;
	}//End forwardCheck

	/*
	 * Undoes the pruning done by variable i
	 */
	public void undoReductions(Variable i)
	{
		ArrayList<Variable> future = i.getFuture();
		int size = future.size();
		for(int j = 0; j < size; j++)
		{
			Variable var = future.get(j);
			ArrayList<Integer> reduction = var.popReductions();
			int reducSize = reduction.size();
			for(int k = 0; k < reducSize; k++)
			{
				var.returnDomain(reduction.get(k));
			}//End domain union
			var.popPast();			
		}//End iteration through all future variables 
		for(int j = size - 1; j >= 0; j--)
		{
			future.remove(j);
		}//End clear of future
	}//End undoReductions

	/*
	 * updates the current domain of i
	 */
	public void updateCurrDomain(Variable i)
	{
		i.restore();
		ArrayList<ArrayList<Integer>> reductions = i.getReductions();
		int size = reductions.size();
		for(int j = 0; j < size; j++)
		{
			ArrayList<Integer> reduction = reductions.get(j);
			int reducSize = reduction.size();
			for(int k = 0; k < reducSize; k++)
			{
				i.removeFromDomain(reduction.get(k));
			}//End loop through reduction
		}//ENd loop through all reductions
	}//End updateCurrDomain

	/*
	 * Labels the given variable can handle CBJ
	 */
	public ArrayList<Variable> fcLabel(ArrayList<Variable> vars, boolean CBJ)
	{
		boolean consistent = false;
		int varSize = vars.size() - 1;
		Variable currVar = vars.get(varSize);
		ArrayList<Integer> domain = currVar.getCurrDomain();
		//for each value in the domain i 
		for(int i = 0; i < domain.size(); i++)
		{
			consistent = true;
			currVar.setAssignment(domain.get(i));
			this.nv++;
			
			//For each future variable
			int futureSize = this.unassigned.size();
			for(int j = 0; j < futureSize; j++)
			{
				boolean check = this.forwardCheck(currVar, this.unassigned.get(j));
				consistent = check && consistent;
				
				//TODO
				if(CBJ && !check)
				{
					//conf-set[i] < union(conf-set[i], past-fc(j))
					ArrayList<Variable> conf = this.confSet.get(confSet.size() - 1);
					ArrayList<Variable> past = this.unassigned.get(j).getPast();
					int size = past.size();
					for(int k = 0; k < size; k++)
					{
						if(!(conf.contains(past.get(i))))
						{
							conf.add(past.get(i));
						}//End existance check
					}//End iteration through past-fc
				}
			}//End iteration over future variables
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
				this.undoReductions(currVar);
				i--;
			}//End remov
		}//End iteration through domain
		return vars;
	}//End fcLable

	/*
	 * Unlables the given variable
	 */
	public ArrayList<Variable> fcUnlabel(ArrayList<Variable> vars, boolean CBJ)
	{
		//Find the deepest conflict in the conflict list
		ArrayList<Variable> set = null;
		ArrayList<Variable> pastfc = null;
		int jumpTo = this.assigned.size() - 2;
		this.bt++;
		if(CBJ)
		{
			set = this.confSet.get(this.confSet.size() - 1);
			pastfc = this.assigned.get(jumpTo + 1).getPast();
			for(int i = jumpTo; i >= 0; i--)
			{
				Variable temp = this.assigned.get(i);
				if(set.contains(temp) || pastfc.contains(temp))
				{
					break;
				}//End check to see if this var is in the conflict list
			}//End finding deepest conflictS
		}// end cbj check

		if(jumpTo < 0)
			return null;

		if(CBJ)
		{
			//Adds conflict sets together
			ArrayList<Variable> newSet = this.confSet.get(jumpTo);
			for(int i = 0; i < set.size(); i++)
			{
				Variable temp = set.get(i);
				if(!(newSet.contains(temp)) || !(temp.equals(this.assigned.get(jumpTo))))
				{
					newSet.add(temp);
				}//End filter check
			}//end set combination
			for(int i = 0; i < pastfc.size(); i++)
			{
				Variable temp = pastfc.get(i);
				if(!(newSet.contains(temp)) || !(temp.equals(this.assigned.get(jumpTo))))
				{
					newSet.add(temp);
				}//End filter check
			}//end pastfc combination
		}//end cbj check


		for(int i = this.assigned.size() - 1; i > jumpTo; i--)
		{
			Variable temp = this.assigned.get(i);
			temp.removeAssignment();
			this.undoReductions(temp);
			this.updateCurrDomain(temp);
			this.unassigned.add(0, temp);
			this.assigned.remove(i);
			if(CBJ)
			{
				this.confSet.remove(i);
			}
			
		}//End unassigning of variables
		Variable jump = this.assigned.get(jumpTo);
		this.undoReductions(jump);
		jump.removeFromDomain(jump.getAssignment());
		jump.removeAssignment();
		return this.assigned;
	}//End fcUnlable

	/*
	 * Searches for a solution
	 */
	public ArrayList<Integer[]> fcSearch(boolean all, boolean CBJ)
	{
		String status = null;
		Variable temp = null;
		ArrayList<Integer[]> solutionSet = new ArrayList<Integer[]>();
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
			currPath = this.fcLabel(currPath, CBJ);
			if(currPath == null)
			{
				int size = this.assigned.size();
				Integer[] solution = new Integer[size];
				for(int i = 0; i < size; i++)
				{
					Integer entry = this.variables.get(i).getAssignment();
					solution[i] = entry;
				}//End solution iteration
				solutionSet.add(solution);
				if(all)
				{	

					Variable tempVar = this.assigned.get(this.assigned.size() -1);
					tempVar.removeFromDomain(tempVar.getAssignment());
					tempVar.removeAssignment();
					currPath = this.assigned;

					if(CBJ)
					{
						ArrayList<Variable> set = this.confSet.get(this.confSet.size() - 1);
						int length = this.assigned.size() - 1;
						for(int i = 0; i < length; i++)
						{
							if(!(confSet.contains(this.assigned.get(i))))
								set.add(this.assigned.get(i));
						}//End itteration through variables 
					}//End cbj check
					
					 
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
				currPath = this.fcUnlabel(currPath, CBJ);
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

	//====================================================================================================================
	//====================================================================================================================
	//====================================================================================================================
}//End Class
