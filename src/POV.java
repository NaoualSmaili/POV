import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;

import static choco.Choco.*;

public class POV {

    public static void main(String[] args) {

        /* THE DATA */

        // Number of vehicles.
        int nbPositions = 10;

        // Number of options.
        int nbOptions = 5;

        // Number of categories.
        int nbCategories = 6;

        // Options properties.
        int[] StationSize = {1, 2, 1, 2, 1};
        int[] maxCarsPerStation = {2, 3, 3, 5, 5};

        // Categories properties.
        int[] nbCarsRequested = {1, 1, 2, 2, 2, 2};
        int[][] options =
                {
                        {1, 0, 1, 1, 0},
                        {0, 0, 0, 1, 0},
                        {0, 1, 0, 0, 1},
                        {0, 1, 0, 1, 0},
                        {1, 0, 1, 0, 0},
                        {1, 1, 0, 0, 0}
                };


        /* Model. */
        CPModel m = new CPModel();


        /* Program variables */

        // the class of car that is made in position[i] of a sequence
        IntegerVariable[] position;
        position = makeIntVarArray("position", nbPositions, 0, nbCategories - 1);

        // optOnPos[i][j] = 1 if option i is made on position j
        IntegerVariable[][] optOnPos;
        optOnPos = makeIntVarArray("optOnPos", nbOptions, nbPositions, 0, 1);


        /* Constraints  */

        //C1: to satisfy the demand
        int[] categories = new int[nbCategories];
        IntegerVariable[] categoryDemand = new IntegerVariable[nbCategories];
        for (int j = 0; j < nbCategories; j++) {
            categories[j] = j;
            categoryDemand[j] = makeIntVar("categoryDemand[" + j + "]", nbCarsRequested[j], nbCarsRequested[j]); // a constant: Create a constant variable equal to nbCarsRequested[j]
        }
        m.addConstraint(globalCardinality(position, categories, categoryDemand));


        //C2: to define the options that are used for each car in the sequence
        for (int i = 0; i < nbCategories; i++)
            for (int j = 0; j < nbPositions; j++) {
                Constraint[] C = new Constraint[nbOptions];
                for (int k = 0; k < nbOptions; k++)
                    C[k] = eq(optOnPos[k][j], options[i][k]);

                m.addConstraint(ifOnlyIf(and(C), eq(position[j], i)));
            }


        //C3: Option constraint to assign optOnPos variable in a feasible way
        for (int opt = 0; opt < nbOptions; opt++)
            for (int i = 0; i < nbPositions - maxCarsPerStation[opt]; i++) {
                IntegerVariable[] v = new IntegerVariable[maxCarsPerStation[opt]];
                System.arraycopy(optOnPos[opt], i + 0, v, 0, maxCarsPerStation[opt]);

                m.addConstraint(Choco.leq(sum(v), StationSize[opt]));
            }


        /* Solver.  */
        CPSolver s = new CPSolver();
        s.read(m);
        s.solve();


        /* Print Solution */

        int t = 1;
        //do {
            System.out.println("Solution : " + t);

            //View 1: page 5
            for (int p = 0; p < nbPositions; p++) {
                if (p == 0) {
                    System.out.print("\t \t  ");
                }
                System.out.print(s.getVar(position[p]).getVal() + "  ");
            }
            System.out.println("");

            int b = 0;
            while (b < nbOptions) {
                System.out.print((b + 1) + " , " + StationSize[b] + "/" + maxCarsPerStation[b] + "   ");
                for (int k = 0; k < nbPositions; k++) {
                    System.out.print(s.getVar(optOnPos[b][k]).getVal() + "  ");
                }
                System.out.println("");
                b++;
            }

            //View 2: page 6
            System.out.println("Classe || Options requises");
            for (int p = 0; p < nbPositions; p++) {
                System.out.print("  " + s.getVar(position[p]).getVal() + "\t   ||\t");
                for (int c = 0; c < nbOptions; c++) {  //nbCategories
                    System.out.print(s.getVar(optOnPos[c][p]).getVal() + " ");
                }
                System.out.println("");
            }

            t++;
        //} while (s.nextSolution());

    }

}
