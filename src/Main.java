/*==============================
 * Name:   Ryan Biggs
 * Date:   10/15/14
 * Course: CSE421
 *==============================*/

import abscon.instance.tools.InstanceParser;

import java.io.*;
import java.util.ArrayList;

public class Main 
{
	public static void main(String[] args) throws FileNotFoundException
	{
		run(args);
		//testing();
	}//end main
	
	public static void testing() throws FileNotFoundException
	{
		PrintWriter writer = new PrintWriter("TestOutput.csv");
		writer.println("Name, Algorithm, Ordering,  #cc, #nv, #bt, #cpuTime, #cc, #nv, #bt, cpuTime, #solutions");
		
		File dir = new File("TestFiles");
		File[] testFiles = dir.listFiles();
		for(File test : testFiles)
		{
			long cpuTime; 
			String fileName = "TestFiles/" + test.getName();
			InstanceParser parser = new InstanceParser();
			System.out.println(fileName + "on lx");
			parser.loadInstance(fileName);
			parser.parse(false);
			String order = "LX";
			ProblemSolver problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			problem.nodeConsistency();
			try
			{
				problem.backtrackSearch(false);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getName() + ", ");
			writer.print("BT, ");
			writer.print(order + ", ");
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			
			problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			ArrayList<String[]> sol = null;
			problem.nodeConsistency();
			try
			{
				sol = problem.backtrackSearch(true);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			writer.print(sol.size());
			writer.println();
			
			//=========================================================================================
			System.out.println(fileName + "on ld");
			 order = "LD";
			 problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			problem.nodeConsistency();
			try
			{
				problem.backtrackSearch(false);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getName() + ", ");
			writer.print("BT, ");
			writer.print(order + ", ");
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			
			problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			 sol = null;
			problem.nodeConsistency();
			try
			{
				sol = problem.backtrackSearch(true);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			writer.print(sol.size());
			writer.println();
			
			//=========================================================================================
			System.out.println(fileName + "on deg");
			 order = "DEG";
			 problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			problem.nodeConsistency();
			try
			{
				problem.backtrackSearch(false);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getName() + ", ");
			writer.print("BT, ");
			writer.print(order + ", ");
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			
			problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			 sol = null;
			problem.nodeConsistency();
			try
			{
				sol = problem.backtrackSearch(true);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			writer.print(sol.size());
			writer.println();
			
			//=========================================================================================
			System.out.println(fileName + "on dd");
			 order = "DD";
			 problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			problem.nodeConsistency();
			try
			{
				problem.backtrackSearch(false);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getName() + ", ");
			writer.print("BT, ");
			writer.print(order + ", ");
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			
			problem = new ProblemSolver(parser, order, "Static");			
			cpuTime = System.currentTimeMillis();
			sol = null;
			problem.nodeConsistency();
			try
			{
				sol = problem.backtrackSearch(true);
			}
			catch(DomainWipeoutException e)
			{
				System.out.println("Domain Wipeout");
			}
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			writer.print(problem.getCC() + ", ");
			writer.print(problem.getNV()  + ", ");
			writer.print(problem.getBT() + ", ");
			writer.print(cpuTime + ", ");
			writer.print(sol.size());
			writer.println();
			
			//=========================================================================================
			
			
			
		}//End loop through the files
		
		writer.close();		
		
		
		
	}//End testing

	public static void run(String[] args) throws FileNotFoundException
	{
		long setupTime;
		long cpuTime = 0;
		String filename = "";
		String acAlg = "";
		String search = "";
		String order = "";
		String dynamic = "static";
		String[] flags = {"-f","-a", "-s", "-u"};
		ArrayList<String[]> solutionSet = new ArrayList<String[]>();
		InstanceParser parser = new InstanceParser();

		//****************************************
		//Check Command Line Flags
		//****************************************
		//Works only in the follow input format
		//{flag} {argument} {flag} {argument} ... etc
		int argsLength = args.length;

		//Iterates through all of the given arguments
		for(int i = 0; i < argsLength; i+=2)
		{
			//Checks for file flag "-f"
			if(args[i].equals(flags[0]))
			{
				filename = args[i+1];
			}//End file check
			//Checks for AC flag "-a"
			else if(args[i].equals(flags[1]))
			{
				acAlg = args[i+1];
			}//End AC check
			else if(args[i].equals(flags[2]))
			{
				search = args[i+1];
			}//End search algorithm check
			else if(args[i].equals(flags[3]))
			{
				order = args[i+1];
			}//end order check
			//Flags given are incorrect or in the incorrect format End program
			else
			{
				System.out.println("Incorrect Argument Structure...Exiting");
				System.exit(0);
			}//End error case
		}//End args iteration
		
		//****************************************
		//Populate Problem
		//****************************************
		setupTime = System.currentTimeMillis();
		
		//filename = "TestFiles\\queens-conflicts.xml";
		//search = "BT";
		//order = "LX";
		//acAlg = "AC3";
		parser.loadInstance(filename);
		parser.parse(false);		
		ProblemSolver problem = new ProblemSolver(parser, order, "Static");

		setupTime = System.currentTimeMillis() - setupTime;
		//****************************************
		try
		{
			cpuTime = System.currentTimeMillis();
			problem.nodeConsistency();

			//****************************************
			//Run chosen arc consistency algorithm
			//****************************************
			if(acAlg.equalsIgnoreCase("AC1"))
			{
				problem.ac1();
			}//End AC1 check
			else if (acAlg.equalsIgnoreCase("AC3"))
			{
				problem.ac3();
			}//End AC3 check
			//****************************************

			if(search.equalsIgnoreCase("BT"))
			{
				solutionSet = problem.backtrackSearch(false);
			}

		}//End look for domain wipeout
		catch(DomainWipeoutException e)
		{
			System.out.println("Domain Wipeout");
		}//end handle domain wipeout
		finally
		{
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			//****************************************
			//Prints Output
			//****************************************
			System.out.println("Instance name: " + problem.getName());
			System.out.println("variable-order-heuristic: " + order);
			System.out.println("var-static-dynamic: " + dynamic);
			System.out.println("value-ordering-heuristic: LX");
			System.out.println("val-static-dynamic: static");
			System.out.println("cc: "            + problem.getCC());
			System.out.println("nv: " + problem.getNV());
			System.out.println("bt: " + problem.getBT());
			System.out.println("cpu: "           + cpuTime);
			if(solutionSet.size() > 0)
			{
				System.out.print("First solution: ");
				for(int i = 0; i < solutionSet.get(0).length; i++)
				{
					System.out.print(solutionSet.get(0)[i]);
				}
				System.out.println();
			}//End check for solutions
			else
			{
				System.out.println("No solution");
			}

		}//End cleanup
		
		problem = new ProblemSolver(parser, order, "Static");
		
		try
		{
			cpuTime = System.currentTimeMillis();
			problem.nodeConsistency();

			//****************************************
			//Run chosen arc consistency algorithm
			//****************************************
			if(acAlg.equalsIgnoreCase("AC1"))
			{
				problem.ac1();
			}//End AC1 check
			else if (acAlg.equalsIgnoreCase("AC3"))
			{
				problem.ac3();
			}//End AC3 check
			//****************************************

			if(search.equalsIgnoreCase("BT"))
			{
				solutionSet = problem.backtrackSearch(true);
			}

		}//End look for domain wipeout
		catch(DomainWipeoutException e)
		{
			System.out.println("Domain Wipeout");
		}//end handle domain wipeout
		finally
		{
			cpuTime = System.currentTimeMillis() - cpuTime;
			
			//****************************************
			//Prints Output
			//****************************************
			System.out.println("all-sol cc: "            + problem.getCC());
			System.out.println("all-sol nv: " + problem.getNV());
			System.out.println("all-sol bt: " + problem.getBT());
			System.out.println("all-sol cpu: "           + cpuTime);
			System.out.println("Number of Solutions: " + solutionSet.size());

		}//End cleanup

	}//end run
}//end class
