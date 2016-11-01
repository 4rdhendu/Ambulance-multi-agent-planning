# Ambulance Planning with Distance Optimization

Team 14

Federico Baldassarre fedbal@kth.se
Vladislav Polianskii vpol@kth.se
Ardhendu Shekhar Tripathi tripa@kth.se
Monica Villanueva Aylagas monicavi@kth.se

## Generating test cases
The Generator component can be run by the command line and takes as an argument the name of a .json file containing the parameters for the city to be generated.
The following is a valid .json file:

```json
{
	"nodes": 12,
	"roads": 30,
	"noise": 1,
	"demand": 50,

	"ambulances": 5,
	"hospitals": 2,

	"patients": 2,
	"severity1prob": 0.2,
	"severity2prob": 0.5,
	"severity3prob": 0.3,

	"output": "test.pddl"
}
```

To launch the Generator run:
```
java -jar Generator.jar ../input.json
```

This will generate a test.pddl file containing the description of the generated problem.

## Running the planner
The Planner component can be run from the command line and takes as arguments the name of the pddl file describing the problem and the name of the Planner class to use. The following are valid invocations of the Planner:

```
java -jar Planner.jar test.pddl planner.HungarianPlanner
java -jar Planner.jar test.pddl planner.PSOPlanner
```

This will initially print the description of the city and the initial state. Then will start interactive session that at every step allows the user to visualize the actions taken and decide whether to add new patients. An execution step in the plan will be presented like this:
```
Step: 0
Replanning...
Ambulances:
A0 @ N5 [] 
A1 @ N4 [] 
Patients:
P0 (3) @ N2
P1 (3) @ N3
P2 (3) @ N1
Actions for A0 @ N5 [] 
   move(A0 5 -> 4)
   move(A0 4 -> 1)
   pick(A0 P2 @ N1)
   move(A0 1 -> 0)
   drop(A0 P2 @ N0)
Actions for A1 @ N4 [] 
   move(A1 4 -> 1)
   move(A1 1 -> 0)
   move(A1 0 -> 3)
   pick(A1 P1 @ N3)
   move(A1 3 -> 0)
   drop(A1 P1 @ N0)
Executing: move(A0 5 -> 4)
Executing: move(A1 4 -> 1)
Want to add a patient?
(enter to continue, r for random, syntax: [node(0-7) severity(1-3)]* | r, ex: 5 3 7 1)
 > 
```
The Planner will terminate in a state such that all the patients have been brought to an hospital and all the ambulances have reached an location that offers optimal coverage. In other words, the Planner will terminate when there are no more actions to be performed.

All the information about the soved problem is stored on external files:
* __cityDump.txt__ contains a temporary representation of the adjacency matrix, to be used in conjunction with graph plotting tools to better visualize the problem, will be overwritten after each execution
* __\<problem name\>.\<timestamp\>.descr__ contains the full description of the problem solved, including the initial state and the patients that spawned later
* __\<problem name\>.\<timestamp\>.plan__ contains a step by step list of the actions performed

To bypass the interactivity feature and allow for the software to randomly spawn patients add two numeric arguments to the command line. The first representing the spawn probability of the patients and the second the maximum number of patients.
```
java -jar Planner.jar test.pddl planner.HungarianPlanner 0.6 10
```
