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
		ArrayList<String[]> fin = new ArrayList<String[]>();
		String[] staticOrder = /*{"LX", "LD", "DEG", "DD", "dLX", "dLD", "dDEG", "dDD"};*/{"LX", "LD", "DEG", "DD"}; /*{"dLX", "dLD", "dDEG", "dDD"}*/;
		String[] search = /*{"BT", "CBJ", "FC", "FCCBJ"};*/ {"FCCBJ"};
		File dir = new File("TestFiles\\RunOn");
		File[] listing = dir.listFiles();
		for(int i = 0; i < listing.length; i++)
		{
			String path = listing[i].getPath();
			for(int j = 0; j < staticOrder.length; j ++)
			{
				String order = staticOrder[j];
				for(int k = 0; k < search.length; k++)
				{
					String searchString = search[k];
					String[] args = {"-f", path, "-s", searchString, "-u", order};
					String[] val = run(args);
					fin.add(val);
				}
			}
		}
		
		PrintWriter writer = new PrintWriter("TestOutput.csv");
		for(int i = 0; i < fin.size(); i++)
		{
			for(int j = 0; j < 11; j++)
			{
				writer.print(fin.get(i)[j] + ", ");
			}
			writer.println();
		}
		writer.close();
	}//End testing

	public static String[] run(String[] args) throws FileNotFoundException
	{
		String[] returnStr = new String[11];
		long setupTime;
		long cpuTime = 0;
		String filename = "";
		String acAlg = "";
		String search = "";
		String order = "";
		String dynamic = "static";
		String[] flags = {"-f","-a", "-s", "-u"};
		ArrayList<Integer[]> solutionSet = new ArrayList<Integer[]>();
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
		
		//filename = "TestFiles\\RunOn\\6queens-conflicts.xml";
		//search = "FCCBJ";
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
			else if(search.equalsIgnoreCase("CBJ"))
			{
				solutionSet = problem.cbjSearch(false);
			}
			else if(search.equalsIgnoreCase("FC"))
			{
				solutionSet = problem.fcSearch(false, false);
			}
			else if(search.equalsIgnoreCase("FCCBJ"))
			{
				solutionSet = problem.fcSearch(false, true);
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
			System.out.println("Search: " + search);
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
					System.out.print(solutionSet.get(0)[i] + " ");
				}
				System.out.println();
			}//End check for solutions
			else
			{
				System.out.println("No solution");
			}
			returnStr[0] = problem.getName();
			returnStr[1] = search;
			returnStr[2] = Integer.toString(problem.getCC());
			returnStr[3] = Integer.toString(problem.getNV());
			returnStr[4] = Integer.toString(problem.getBT());
			returnStr[5] = Long.toString(cpuTime);

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
			else if(search.equalsIgnoreCase("CBJ"))
			{
				solutionSet = problem.cbjSearch(true);
			}
			else if(search.equalsIgnoreCase("FC"))
			{
				solutionSet = problem.fcSearch(true, false);
			}
			else if(search.equalsIgnoreCase("FCCBJ"))
			{
				solutionSet = problem.fcSearch(true, true);
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
			
			returnStr[6] = Integer.toString(problem.getCC());
			returnStr[7] = Integer.toString(problem.getNV());
			returnStr[8] = Integer.toString(problem.getBT());
			returnStr[9] = Long.toString(cpuTime);
			returnStr[10] = Integer.toString(solutionSet.size());
			

		}//End cleanup
		
		return returnStr;
	}//end run
}//end class
