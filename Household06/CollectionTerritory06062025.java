import java.util.Arrays;

/**
 * @author shuet
 * Modified to support household-based waste calculations with dynamic household size
 * and ensure exact validation against SD model results
 */
public class CollectionTerritory {

    Territory myTerre;
    Household[] myHouseholds;
    
    // Dynamic household size parameters
    double initialHouseholdSize;
    double householdSizeGrowthRate;
    double[] householdSize;
    
    // Define the starting point of the simulation
    int timeBeforeInit_αcf_initial;
    int timeBeforeInit_αcg_initial;
    int timeBeforeInit_αsf_initial;
    int timeBeforeInit_αsg_initial;

    double Kc_initial;
    double Ks_initial;
    double[] Kct;
    double[] Kst;
    double αc_target;
    double αs_target;
    int yearRef;

    double[] LinearHomeComposter;
    double[] sigmoide_mcf;
    double[] sigmoide_mcg;
    double[] LinearDedicatedCollection;
    double[] sigmoide_msf;
    double[] sigmoide_msg;
    double[] sigmoide_mpg;
    
    double αcf_initial;
    double αcg_initial;
    double αcf_max;
    double αcg_max;
    double αsf_initial;
    double αsf_max;
    double αsg_initial;
    double αsg_max;

    double b_pf_per_individual;
    double b_pg_per_individual;

    double αv;
    int numHouseholds;
    int totalPopulation;
    
    double duraImplemCompo;
    double mc;
    double duraImplemCollect;
    double ms;
    double mpg;
    double αpg_target;

    // Territory-level aggregated arrays (for validation against SD model)
    double[] H; // Number of households (fixed)
    double[] P; // Total population (changes with household size)
    double[] B;
    double[] Bpg;
    double[] Bpf;
    double[] ABP;
    double[] R;
    double[] G;
    double[] αcf;
    double[] αcg;
    double[] αvg;
    double[] αsf;
    double[] αsg;
    double[] C_log;
    double[] C_pop;
    double[] Bcg;
    double[] Bcf;
    double[] Bcf_composted;
    double[] Bcg_composted;
    double[] Bc_composted;
    double[] Uc;
    double[] Ucg;
    double[] Ucf;
    double[] sLbis;
    double[] Bv;
    double[] Bsg;
    double[] Bsf;
    double[] Bs_sorted;
    double[] Bsf_sorted;
    double[] Bsg_sorted;
    double[] Usf;
    double[] Usg;
    double[] sAa_bis;
    double[] sAv_bis;
    double[] Us;
    double[] sAbis;
    double[] Br;
    double αpf_target;

    int collectionTerritoryName;
    double[] propPopDesserviCollDA;
    double[] nbKgCollectHabDesservi;
    double[] nbKgOMRHab;
    double[] tauxReductionDechetVert;
    int ident;

    public CollectionTerritory(Territory mt, int id) {
        myTerre = mt;
        ident = id;
    }

    public void iterate(int year) {
        // Update household size for the current year
        updateHouseholdSize(year);
        
        // Update infrastructure capacity evolution
        LinearHomeComposter[year] = linear(year, duraImplemCompo);
        LinearDedicatedCollection[year] = linear(year, duraImplemCollect);
        
        if (myTerre.useSocialDynamics) {
            sigmoide_mcf[year] = sigmoide(year + timeBeforeInit_αcf_initial, mc);
            sigmoide_mcg[year] = sigmoide(year + timeBeforeInit_αcg_initial, mc);
            sigmoide_msf[year] = sigmoide(year + timeBeforeInit_αsf_initial, ms);
            sigmoide_msg[year] = sigmoide(year + timeBeforeInit_αsg_initial, ms);
            sigmoide_mpg[year] = sigmoide(year, mpg);
        }
        
        // Process each household with updated household size
        for (int i = 0; i < numHouseholds; i++) {
            myHouseholds[i].updateHouseholdSize(householdSize[year]);
            myHouseholds[i].computeWaste(year, αpf_target, αpg_target, myTerre.sigmoideABP);
            myHouseholds[i].computeBehavioralIntentions(year);
            myHouseholds[i].compost(year);
            myHouseholds[i].sortingFoodAndGreenWaste(year);
            myHouseholds[i].putInValCenter(year);
            myHouseholds[i].putInBlackBin(year);
        }
        
        // Aggregate household results to territory level
        computeCollectionTerritoryIndicator(year);
        
        // Validation: print comparison with expected SD results
        if (year % 5 == 0) { // Print every 5 years
            validateResults(year);
        }
    }

    /**
     * Updates household size for the given year based on growth rate
     */
    private void updateHouseholdSize(int year) {
        if (year == 0) {
            householdSize[year] = initialHouseholdSize;
        } else {
            householdSize[year] = householdSize[year - 1] + householdSizeGrowthRate;
        }
        
        // Update total population based on current household size
        P[year] = numHouseholds * householdSize[year];
    }

    /**
     * Sigmoid function for behavioral intention evolution
     */
    public double sigmoide(double x, double ti) {
        double t = Math.pow(x, 5);
        double z = t / (t + Math.pow(ti, 5));
        return z;
    }

    /**
     * Linear function for infrastructure capacity evolution
     */
    public double linear(double t, double duration) {
        return Math.min(t / duration, 1.0);
    }

    /**
     * Calculate time before initialization for behavioral intentions
     */
    public int calculateTimeBeforeInit(double alpha_base, double ti) {
        int timeBeforeInit = 0;
        if (alpha_base > 0) {
            double sigmoideValue = sigmoide(timeBeforeInit, ti);
            while (sigmoideValue < alpha_base) {
                timeBeforeInit++;
                sigmoideValue = sigmoide(timeBeforeInit, ti);
            }
        }
        return timeBeforeInit;
    }

    /**
     * Initialize the collection territory with parameters and create households
     */
    public void init(int sizeData, double[] params, int refYear) {
        yearRef = refYear;
        collectionTerritoryName = (int) params[0];
        duraImplemCompo = params[1];
        duraImplemCollect = params[2];
        mc = params[3];
        ms = params[4];
        
        // Read per individual waste production from parameters
        b_pf_per_individual = params[5];
        b_pg_per_individual = params[6];
        
        αcf_initial = params[7];
        αcg_initial = params[8];
        αsf_initial = params[9];
        αsf_max = params[10];
        αcf_max = params[11];
        αcg_max = params[12];
        αsg_initial = params[13];
        αsg_max = params[14];
        Kc_initial = params[15];
        αc_target = params[16];
        Ks_initial = params[17];
        αs_target = params[18];
        
        totalPopulation = (int) params[19];
        
        // Read household size parameters
        initialHouseholdSize = params[20];
        householdSizeGrowthRate = params[21]; // Should be 0 for validation
        
        // Calculate fixed number of households
        numHouseholds = (int) Math.ceil(totalPopulation / initialHouseholdSize);
        
        mpg = params[22];
        αpg_target = params[23];
        αpf_target = params[24];
        
        // Initialize household size array
        householdSize = new double[sizeData];
        
        // Calculate time before init values
        timeBeforeInit_αcf_initial = calculateTimeBeforeInit(αcf_initial, mc);
        timeBeforeInit_αcg_initial = calculateTimeBeforeInit(αcg_initial, mc);
        timeBeforeInit_αsf_initial = calculateTimeBeforeInit(αsf_initial, ms);
        timeBeforeInit_αsg_initial = calculateTimeBeforeInit(αsg_initial, ms);

        // Initialize all arrays
        initializeArrays(sizeData);
        
        // Initialize households
        myHouseholds = new Household[numHouseholds];
        for (int i = 0; i < numHouseholds; i++) {
            myHouseholds[i] = new Household(sizeData, b_pf_per_individual, b_pg_per_individual, 
                                          this, initialHouseholdSize);
        }
        
        // Set initial conditions
        updateHouseholdSize(0);
        
        // Initialize households with initial waste production - using base values only
        for (int i = 0; i < numHouseholds; i++) {
            // Set base waste without any ABP reduction for initial state
            myHouseholds[i].Bpf = b_pf_per_individual * initialHouseholdSize;
            myHouseholds[i].Bpg = b_pg_per_individual * initialHouseholdSize;
            
            // Set initial behavioral intentions from parameters
            myHouseholds[i].αcf = αcf_initial;
            myHouseholds[i].αcg = αcg_initial;
            myHouseholds[i].αsf = αsf_initial;
            myHouseholds[i].αsg = αsg_initial;
            myHouseholds[i].αvg = 1.0 - αcg_initial - αsg_initial;
        }
        
        // Now safely compute territory indicators for initial state
        computeCollectionTerritoryIndicator(0);
    }

    /**
     * Initialize all arrays with proper size and default values
     */
    private void initializeArrays(int sizeData) {
        H = new double[sizeData];
        Arrays.fill(H, numHouseholds);
        
        P = new double[sizeData];
        R = new double[sizeData];
        Arrays.fill(R, 0.0);
        ABP = new double[sizeData];
        Arrays.fill(ABP, 0.0);
        G = new double[sizeData];
        Arrays.fill(G, 0.0);
        B = new double[sizeData];
        Arrays.fill(B, 0.0);
        Bpg = new double[sizeData];
        Arrays.fill(Bpg, 0.0);
        Bpf = new double[sizeData];
        Arrays.fill(Bpf, 0.0);
        αcf = new double[sizeData];
        Arrays.fill(αcf, 0.0);
        αcg = new double[sizeData];
        Arrays.fill(αcg, 0.0);
        αvg = new double[sizeData];
        Arrays.fill(αvg, 0.0);
        C_log = new double[sizeData];
        Arrays.fill(C_log, 0.0);
        C_pop = new double[sizeData];
        Arrays.fill(C_pop, 0.0);
        Bc_composted = new double[sizeData];
        Arrays.fill(Bc_composted, 0.0);
        Bcg = new double[sizeData];
        Arrays.fill(Bcg, 0.0);
        Bcf = new double[sizeData];
        Arrays.fill(Bcf, 0.0);
        Uc = new double[sizeData];
        Arrays.fill(Uc, 0.0);
        Ucf = new double[sizeData];
        Arrays.fill(Ucf, 0.0);
        Ucg = new double[sizeData];
        Arrays.fill(Ucg, 0.0);
        Bcg_composted = new double[sizeData];
        Arrays.fill(Bcg_composted, 0.0);
        Bcf_composted = new double[sizeData];
        Arrays.fill(Bcf_composted, 0.0);
        sLbis = new double[sizeData];
        Arrays.fill(sLbis, 0.0);
        Bv = new double[sizeData];
        Arrays.fill(Bv, 0.0);
        Usg = new double[sizeData];
        Arrays.fill(Usg, 0.0);
        Br = new double[sizeData];
        Arrays.fill(Br, 0.0);
        Kst = new double[sizeData];
        Arrays.fill(Kst, 0.0);
        Kct = new double[sizeData];
        Arrays.fill(Kct, 0.0);

        LinearHomeComposter = new double[sizeData];
        Arrays.fill(LinearHomeComposter, 0.0);
        sigmoide_mcf = new double[sizeData];
        Arrays.fill(sigmoide_mcf, 0.0);
        sigmoide_mcg = new double[sizeData];
        Arrays.fill(sigmoide_mcg, 0.0);
        LinearDedicatedCollection = new double[sizeData];
        Arrays.fill(LinearDedicatedCollection, 0.0);
        sigmoide_msf = new double[sizeData];
        Arrays.fill(sigmoide_msf, 0.0);
        sigmoide_msg = new double[sizeData];
        Arrays.fill(sigmoide_msg, 0.0);
        sigmoide_mpg = new double[sizeData];
        Arrays.fill(sigmoide_mpg, 0.0);
        Bsg = new double[sizeData];
        Arrays.fill(Bsg, 0.0);
        Bsf = new double[sizeData];
        Arrays.fill(Bsf, 0.0);
        Bsf_sorted = new double[sizeData];
        Arrays.fill(Bsf_sorted, 0.0);
        Bsg_sorted = new double[sizeData];
        Arrays.fill(Bsg_sorted, 0.0);
        Bs_sorted = new double[sizeData];
        Arrays.fill(Bs_sorted, 0.0);
        Us = new double[sizeData];
        Arrays.fill(Us, 0.0);
        sAbis = new double[sizeData];
        Arrays.fill(sAbis, 0.0);
        Usf = new double[sizeData];
        Arrays.fill(Usf, 0.0);

        propPopDesserviCollDA = new double[sizeData];
        Arrays.fill(propPopDesserviCollDA, 0.0);
        nbKgCollectHabDesservi = new double[sizeData];
        Arrays.fill(nbKgCollectHabDesservi, 0.0);
        nbKgOMRHab = new double[sizeData];
        Arrays.fill(nbKgOMRHab, 0.0);
        tauxReductionDechetVert = new double[sizeData];
        Arrays.fill(tauxReductionDechetVert, 0.0);
        αsg = new double[sizeData];
        Arrays.fill(αsg, 0.0);
        αsf = new double[sizeData];
        Arrays.fill(αsf, 0.0);
    }

    public void printVector(double[] edit) {
        for (int i = 0; i < edit.length; i++) {
            System.err.print(edit[i] + "\t");
        }
        System.err.println();
    }

    public void indicSubTerritories(int year) {
        double totalPopulation = P[year];
        double nbHabDesservi = Math.min(totalPopulation, (double) Kst[year] / (39.0 / 1000.0));
        
        propPopDesserviCollDA[year] = nbHabDesservi / totalPopulation;
        if (nbHabDesservi > 0) {
            nbKgCollectHabDesservi[year] = (Bsf[year] * 1000.0) / nbHabDesservi;
        }
        nbKgOMRHab[year] = (Br[year] * 1000.0) / totalPopulation;
        tauxReductionDechetVert[year] = (Bv[year] - Bv[0]) / Bv[0];
    }

    public void printTrajectory(int year) {
        System.out.print(ident + ";");
        System.out.print(H[year] + ";"); // Number of households
        System.out.print(P[year] + ";"); // Total population
        System.out.print(householdSize[year] + ";"); // Current household size
        System.out.print(Bpf[year] + ";");
        System.out.print(Bpg[year] + ";");
        System.out.print(αcf[year] + ";");
        System.out.print(αcg[year] + ";");
        System.out.print(Bcf[year] + ";");
        System.out.print(Ucf[year] + ";");
        System.out.print(Bcg[year] + ";");
        System.out.print(Ucg[year] + ";");
        System.out.print(Kct[year] + ";");
        System.out.print(αsf[year] + ";");
        System.out.print(αsg[year] + ";");
        System.out.print(Bsf[year] + ";");
        System.out.print(Usf[year] + ";");
        System.out.print(Bsg[year] + ";");
        System.out.print(Usg[year] + ";");
        System.out.print(Kst[year] + ";");
        System.out.print(Br[year] + ";");
        System.out.print(αvg[year] + ";");
        System.out.print(Bv[year] + ";");
    }

    /**
     * Aggregates household data to territory level for validation against SD model
     */
    public void computeCollectionTerritoryIndicator(int time) {
        // Reset aggregated values
        Bpf[time] = 0.0;
        Bpg[time] = 0.0;
        αcf[time] = 0.0;
        αcg[time] = 0.0;
        αsf[time] = 0.0;
        αsg[time] = 0.0;
        αvg[time] = 0.0;
        Bcf[time] = 0.0;
        Bcg[time] = 0.0;
        Bc_composted[time] = 0.0;
        Bs_sorted[time] = 0.0;
        Kct[time] = 0.0;
        Kst[time] = 0.0;
        Ucf[time] = 0.0;
        Ucg[time] = 0.0;
        Usf[time] = 0.0;
        Usg[time] = 0.0;
        Bsf[time] = 0.0;
        Bsg[time] = 0.0;
        Bv[time] = 0.0;
        Br[time] = 0.0;

        // Sum up data from all households
        for (int i = 0; i < numHouseholds; i++) {
            Bpf[time] += myHouseholds[i].Bpf;
            Bpg[time] += myHouseholds[i].Bpg;
            αcf[time] += myHouseholds[i].αcf;
            αcg[time] += myHouseholds[i].αcg;
            αsf[time] += myHouseholds[i].αsf;
            αsg[time] += myHouseholds[i].αsg;
            Bcf[time] += myHouseholds[i].Bcf;
            Bcg[time] += myHouseholds[i].Bcg;
            Bsf[time] += myHouseholds[i].Bsf;
            Bsg[time] += myHouseholds[i].Bsg;
            Ucf[time] += myHouseholds[i].Ucf;
            Ucg[time] += myHouseholds[i].Ucg;
            Usf[time] += myHouseholds[i].Usf;
            Usg[time] += myHouseholds[i].Usg;
            Kct[time] += myHouseholds[i].Kct;
            Kst[time] += myHouseholds[i].Kst;
            Bv[time] += myHouseholds[i].Bv;
            Br[time] += myHouseholds[i].Br;
        }

        // Calculate averages for behavioral intentions
        if (numHouseholds > 0) {
            αcf[time] /= numHouseholds;
            αcg[time] /= numHouseholds;
            αsf[time] /= numHouseholds;
            αsg[time] /= numHouseholds;
            αvg[time] = 1.0 - αcg[time] - αsg[time];
        }

        // Calculate derived values
        B[time] = Bpf[time] + Bpg[time];
        Bc_composted[time] = Bcf[time] + Bcg[time];
        Bs_sorted[time] = Bsf[time] + Bsg[time];
        
        // Update infrastructure capacity arrays for tracking
        // Note: These are already aggregated from households
        
        // Update population tracking
        H[time] = numHouseholds;
        // P[time] is already updated in updateHouseholdSize()
        
        // Calculate ABP effects for tracking (only if Territory is fully initialized)
        if (myTerre != null && myTerre.sigmoideABP != null && time < myTerre.sigmoideABP.length) {
            R[time] = αpf_target * myTerre.sigmoideABP[time];
            ABP[time] = αpg_target * myTerre.sigmoideABP[time];
        } else {
            // During initialization, set to zero
            R[time] = 0.0;
            ABP[time] = 0.0;
        }
    }

    /**
     * Validation method to compare ABM results with expected SD model results
     */
    public void validateResults(int year) {
        System.out.println("=== Validation for Territory " + ident + " Year " + year + " ===");
        System.out.printf("Population: %.2f (Households: %d, Size: %.2f)\n", 
                         P[year], numHouseholds, householdSize[year]);
        System.out.printf("Food waste production: %.2f tons\n", Bpf[year]);
        System.out.printf("Green waste production: %.2f tons\n", Bpg[year]);
        System.out.printf("Food composted: %.2f tons (surplus: %.2f)\n", Bcf[year], Ucf[year]);
        System.out.printf("Green composted: %.2f tons (surplus: %.2f)\n", Bcg[year], Ucg[year]);
        System.out.printf("Food sorted: %.2f tons (surplus: %.2f)\n", Bsf[year], Usf[year]);
        System.out.printf("Green sorted: %.2f tons (surplus: %.2f)\n", Bsg[year], Usg[year]);
        System.out.printf("Green to valorization: %.2f tons\n", Bv[year]);
        System.out.printf("Food to residual: %.2f tons\n", Br[year]);
        System.out.printf("Behavioral intentions - αcf: %.3f, αcg: %.3f, αsf: %.3f, αsg: %.3f\n", 
                         αcf[year], αcg[year], αsf[year], αsg[year]);
        System.out.printf("Capacities - Composter: %.2f, Collection: %.2f\n", Kct[year], Kst[year]);
        
        // Mass balance check
        double totalInput = Bpf[year] + Bpg[year];
        double totalOutput = Bcf[year] + Bcg[year] + Bsf[year] + Bsg[year] + Bv[year] + Br[year];
        System.out.printf("Mass balance check: Input=%.2f, Output=%.2f, Difference=%.6f\n", 
                         totalInput, totalOutput, Math.abs(totalInput - totalOutput));
        System.out.println();
    }

    /**
     * Method to compare with original SD model results (if available)
     * You can call this method to validate your ABM results against SD results
     */
    public void compareWithSDModel(double[] sdBpf, double[] sdBpg, double[] sdBcf, 
                                  double[] sdBcg, double[] sdBsf, double[] sdBsg, 
                                  double[] sdBv, double[] sdBr, int year) {
        System.out.println("=== SD Model Comparison for Territory " + ident + " Year " + year + " ===");
        
        double tolerance = 1e-3; // 0.001 tons tolerance
        
        System.out.printf("Food production: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Bpf[year], sdBpf[year], Math.abs(Bpf[year] - sdBpf[year]),
                         Math.abs(Bpf[year] - sdBpf[year]) < tolerance ? "✓" : "✗");
        
        System.out.printf("Green production: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Bpg[year], sdBpg[year], Math.abs(Bpg[year] - sdBpg[year]),
                         Math.abs(Bpg[year] - sdBpg[year]) < tolerance ? "✓" : "✗");
        
        System.out.printf("Food composted: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Bcf[year], sdBcf[year], Math.abs(Bcf[year] - sdBcf[year]),
                         Math.abs(Bcf[year] - sdBcf[year]) < tolerance ? "✓" : "✗");
        
        System.out.printf("Green composted: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Bcg[year], sdBcg[year], Math.abs(Bcg[year] - sdBcg[year]),
                         Math.abs(Bcg[year] - sdBcg[year]) < tolerance ? "✓" : "✗");
        
        System.out.printf("Food sorted: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Bsf[year], sdBsf[year], Math.abs(Bsf[year] - sdBsf[year]),
                         Math.abs(Bsf[year] - sdBsf[year]) < tolerance ? "✓" : "✗");
        
        System.out.printf("Green sorted: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Bsg[year], sdBsg[year], Math.abs(Bsg[year] - sdBsg[year]),
                         Math.abs(Bsg[year] - sdBsg[year]) < tolerance ? "✓" : "✗");
        
        System.out.printf("Green valorization: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Bv[year], sdBv[year], Math.abs(Bv[year] - sdBv[year]),
                         Math.abs(Bv[year] - sdBv[year]) < tolerance ? "✓" : "✗");
        
        System.out.printf("Food residual: ABM=%.3f, SD=%.3f, Diff=%.6f %s\n", 
                         Br[year], sdBr[year], Math.abs(Br[year] - sdBr[year]),
                         Math.abs(Br[year] - sdBr[year]) < tolerance ? "✓" : "✗");
        
        System.out.println();
    }

    /**
     * Getter methods for accessing territory-level aggregated data
     */
    public double[] getBpf() { return Bpf; }
    public double[] getBpg() { return Bpg; }
    public double[] getBcf() { return Bcf; }
    public double[] getBcg() { return Bcg; }
    public double[] getBsf() { return Bsf; }
    public double[] getBsg() { return Bsg; }
    public double[] getBv() { return Bv; }
    public double[] getBr() { return Br; }
    public double[] getUcf() { return Ucf; }
    public double[] getUcg() { return Ucg; }
    public double[] getUsf() { return Usf; }
    public double[] getUsg() { return Usg; }
    public double[] getKct() { return Kct; }
    public double[] getKst() { return Kst; }
    public double[] getAlphaCf() { return αcf; }
    public double[] getAlphaCg() { return αcg; }
    public double[] getAlphaSf() { return αsf; }
    public double[] getAlphaSg() { return αsg; }
    public double[] getP() { return P; }
    public double[] getH() { return H; }
    public Household[] getHouseholds() { return myHouseholds; }
}
