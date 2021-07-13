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
        int[] nbCarsD = {1, 1, 2, 2, 2, 2};
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

        // classOnPos[i][j] = 1 if category i is on position j
        IntegerVariable[][] catOnPos;
        catOnPos = makeIntVarArray("catOnPos", nbOptions, nbPositions, 0, 1);


        /* Constraints  */

        //C1: to satisfy the demand: Cardinality of configurations
        for (int opt = 0; opt < nbOptions; opt++)
            for (int i = 0; i < nbPositions - StationSize[opt]; i++) {
                IntegerVariable[] v = new IntegerVariable[StationSize[opt]];
                for (int j = 0; j < StationSize[opt]; j++)
                    v[j] = catOnPos[opt][i + j];

                m.addConstraint(Choco.leq(sum(v),maxCarsPerStation[opt]));
            }


        int[] classes = new int[nbCategories];
        IntegerVariable[] classDemand = new IntegerVariable[nbCategories];
        for (int j = 0; j < nbCategories; j++) {
            classes[j] = j;
            classDemand[j] = makeIntVar("classDemand[" + j + "]", nbCarsD[j], nbCarsD[j]); // a constant: Create a constant variable equal to nbCarsD[j]
        }
        m.addConstraint(globalCardinality(position, classes, classDemand));


        //C2: to define the options that are used for each car in the sequence: Capacity of gliding windows
        for (int cat = 0; cat < nbCategories; cat++)
            for (int car = 0; car < nbPositions; car++) {
                Constraint[] C = new Constraint[nbOptions];
                for (int op = 0; op < nbOptions; op++)
                    C[op] = eq(catOnPos[op][car], options[cat][op]);

                m.addConstraint(ifOnlyIf(and(C),eq(position[car], cat)));
            }


        //C3: to define the length of the sequence: Last slot
        int[] vectorDemand = new int[nbOptions];
        IntegerVariable[][] optPosArray;
        for (int o = 0; o < nbOptions; o++) {
            for (int c = 0; c < nbCategories; c++) {
                vectorDemand[o] = nbCarsD[c] * options[c][o] + vectorDemand[o];
            }

            int i1 = nbPositions - StationSize[o] * (o + 1);
            optPosArray = new IntegerVariable[nbPositions][i1];
            for (int p = 0; p < i1; p++) {
                optPosArray[o][p] = catOnPos[o][p];
            }

            IntegerVariable c = Choco.makeIntVar("x", 1, 1);
            //m.addConstraint(Choco.geq(optPosArray[o], sum(vectorDemand[o]- maxCarsPerStation[o]*(o+1))));
            Choco.sum(Choco.minus(vectorDemand[o], Choco.mult(maxCarsPerStation[o], Choco.plus(o, c))));
        }


        /* Solver.  */
        CPSolver s = new CPSolver();
        s.read(m);
        s.solve();


        /* Print Solution */

        int t = 1;
        //do {
        System.out.println("Solution : "+t);

        //Affichage Page 6
        /*System.out.println("Classe || Options requises");
        for (int p = 0; p < nbPositions; p++) {
            System.out.print("  "+s.getVar(position[p]).getVal() +"\t   ||\t");
            for (int c = 0; c < nbOptions; c++) {  //nbCategories
                System.out.print(s.getVar(catOnPos[c][p]).getVal() +" ");
            }
            System.out.println("");
        }*/


        //Affichage page 5
        for (int p = 0; p < nbPositions; p++) {
            if(p==0){
                System.out.print("\t \t  ");
            }
            System.out.print(s.getVar(position[p]).getVal() +"  ");
        }
        System.out.println("");

        int b=0;
        while(b<nbOptions){
            System.out.print((b+1)+" , "+StationSize[s.getVar(position[b]).getVal()] +"/"+maxCarsPerStation[s.getVar(position[b]).getVal()]+"   ");
            for (int p = 0; p < nbPositions; p++) {
                System.out.print(s.getVar(catOnPos[b][p]).getVal() +"  ");
            }
            System.out.println("");
            b++;
        }

            t++;
        //} while (s.nextSolution());

    }

}
