import java.util.Arrays;

/**
 * This class represents a household entity in a waste management simulation
 * model. It tracks waste production and behavioral intentions related to 
 * waste management practices like composting and sorting.
 * Modified to handle dynamic household size over time.
 */
public class Household {

    private CollectionTerritory territory;
    
    // Dynamic household size
    private double currentHouseholdSize;
    
    // Per capita waste production coefficients (read from input file)
    private double b_pf_per_individual; // Base food waste production coefficient per capita
    private double b_pg_per_individual; // Base green waste production coefficient per capita

    // Current waste production (calculated based on household size)
    double Bpf; // Food waste production for this household
    double Bpg; // Green waste production for this household
    
    
    
 // Per-household equipment capacities
private double composterCapacity; // kg per household for composting
private double collectionCapacity; // kg per household for collection
    
    

    int yearRef;
    int timeBeforeInit_αcf_initial;
    int timeBeforeInit_αcg_initial;
    int timeBeforeInit_αsf_initial;
    int timeBeforeInit_αsg_initial;

    double Kc_initial;
    double Ks_initial;
    double Kct;
    double Kst;
    double αc_target;
    double αs_target;
    
    double[] LinearHomeComposter;
    double[] sigmoide_mcf;
    double[] sigmoide_mcg;
    double[] LinearDedicatedCollection;
    double[] sigmoide_msf;
    double[] sigmoide_msg;
    
    double αcf_initial;
    double αcg_initial;
    double αcf_max;
    double αcg_max;
    double αsf_initial;
    double αsf_max;
    double αsg_initial;
    double αsg_max;
    double αv;
    
    double mc;
    double duraImplemCollect;
    double ms;
    
    double αcf;
    double αcg;
    double αvg;
    double αsf;
    double αsg;
    double C_log;
    double Bcg;
    double Bcf;
    double Bcf_composted;
    double Bcg_composted;
    double Bc_composted;
    double Uc;
    double Ucg;
    double Ucf;
    double sLbis;
    double Bv;
    double Bsg;
    double Bsf;
    double Bs_sorted;
    double Bsf_sorted;
    double Bsg_sorted;
    double Usf;
    double Usg;
    double sAa_bis;
    double sAv_bis;
    double Us;
    double sAbis;
    double Br;

    /**
     * Constructor that initializes a Household with specified parameters.
     *
     * @param sizeData The number of time periods (years) to simulate
     * @param bpf_per_individual Base food waste production per capita
     * @param bpg_per_individual Base green waste production per capita
     * @param territory Reference to the collection territory
     * @param initialHouseholdSize Initial household size
     */
    public Household(int sizeData, double bpf_per_individual, double bpg_per_individual, 
                     CollectionTerritory territory, double initialHouseholdSize) {
        
        this.territory = territory;
        this.b_pf_per_individual = bpf_per_individual;
        this.b_pg_per_individual = bpg_per_individual;
        this.currentHouseholdSize = initialHouseholdSize;
        
        // Calculate initial waste production based on household size
        updateWasteProduction();

        // Initialize arrays to track values over simulation time periods
        LinearHomeComposter = new double[sizeData];
        sigmoide_mcf = new double[sizeData];
        sigmoide_mcg = new double[sizeData];
        LinearDedicatedCollection = new double[sizeData];
        sigmoide_msf = new double[sizeData];
        sigmoide_msg = new double[sizeData];
    }

    /**
     * Updates the household size and recalculates waste production accordingly
     * 
     * @param newHouseholdSize The new household size for this time period
     */
    public void updateHouseholdSize(double newHouseholdSize) {
        this.currentHouseholdSize = newHouseholdSize;
        updateWasteProduction();
    }

    /**
     * Updates waste production based on current household size
     */
    private void updateWasteProduction() {
        // Base waste production is per individual * household size
        // This gives us the baseline waste production for this household
        double baseWasteProductionFood = b_pf_per_individual * currentHouseholdSize;
        double baseWasteProductionGreen = b_pg_per_individual * currentHouseholdSize;
        
        // Store as instance variables for use in computeWaste
        Bpf = baseWasteProductionFood;
        Bpg = baseWasteProductionGreen;
    }

    /**
     * Calculates and updates the waste production for a specific year based on
     * household size and waste reduction targets.
     *
     * @param year The current year in the simulation
     * @param αpf_target Target reduction for waste production
     * @param sigmoideABP Array of sigmoid function values modeling behavioral change over time
     */
    public void computeWaste(int year, double αpf_target, double[] sigmoideABP) {
        // Apply reduction factor to the base waste production
        // Base production is already calculated based on current household size
        double reductionFactor = αpf_target * sigmoideABP[year];
        
        Bpf = (b_pf_per_individual * currentHouseholdSize) * (1 - reductionFactor);
        Bpg = (b_pg_per_individual * currentHouseholdSize) * (1 - reductionFactor);
    }

    
    /**
 * Updates the household's equipment capacities for composting and collection
 * 
 * @param composterCapacity Composter capacity in kg
 * @param collectionCapacity Collection capacity in kg
 */
public void updateEquipmentCapacities(double composterCapacity, double collectionCapacity) {
    this.composterCapacity = composterCapacity;
    this.collectionCapacity = collectionCapacity;
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Computes behavioral intentions for waste management practices
     */
    public void computeBehavioralIntentions(int y) {
        double trucDa, trucDv;
        
        // Calculate behavioral intentions using territory-level sigmoid functions
        αcf = Math.min(αcf_initial + (1 - αcf_initial) * territory.sigmoide_mcf[y - 1], 1.0);
        αcg = Math.min(αcg_initial + (1 - αcg_initial) * territory.sigmoide_mcg[y - 1], 1.0);
        αsf = αsf_initial + (1 - αsf_initial) * territory.sigmoide_msf[y];
        
        // Ensure constraints are respected
        if ((trucDa = αcf + αsf) > 1.0) αsf = 1 - αcf;
        αsg = αsg_initial + (αsg_max - αsg_initial) * territory.sigmoide_msg[y];
        
        if ((trucDv = αcg + αsg) > 1.0) αsg = 1 - αcg;
        αvg = 1 - αcg - αsg;
    }

    /**
     * Computes composting behavior and capacity constraints
     */

/**
 * Modified composting method to use per-household composter capacity
 */
public void compost(int y) {
    Bcg = αcg * Bpg;
    Bcf = αcf * Bpf;
    
    Kct = composterCapacity; // Use per-household composter capacity
    if (Bcg + Bcf > Kct) {
        Uc = Bcg + Bcf - Kct;
        Bcg_composted = Math.max(Bcg - Uc, 0.0);
        sLbis = Math.max(0.0, Bcg_composted + Bcf - Kct);
        Bcf_composted = Math.max(Bcf - sLbis, 0.0);
        Ucf = Math.min(sLbis, Bcf);
        Ucg = Math.min(Uc, Bcg);
        Bcg = Bcg_composted;
        Bcf = Bcf_composted;
    }
    Bc_composted = Bcf + Bcg;
}


    /**
 * Modified sorting method to use per-household collection capacity
 */
public void sortingFoodAndGreenWaste(int y) {
    Bsg = αsg * Bpg;
    Bsf = αsf * Bpf + Ucf;
    
    Kst = collectionCapacity; // Use per-household collection capacity
    if (Bsg + Bsf > Kst) {
        Us = Bsg + Bsf - Kst;
        Bsg_sorted = Math.max(Bsg - Us, 0.0);
        sAbis = Math.max(0.0, Bsf + Bsg_sorted - Kst);
        Bsf_sorted = Math.max(Bsf - sAbis, 0.0);
        Usg = Math.min(Us, Bsg);
        Usf = Math.min(sAbis, Bsf);
        Bsg = Bsg_sorted;
        Bsf = Bsf_sorted;
    }
    Bs_sorted = Bsg + Bsf;
}

    public void putInValCenter(int y) {
        Bv = αvg * Bpg + Ucg + Usg;
    }

    public void putInBlackBin(int y) {
        Br = (1 - αcf - αsf) * Bpf + Usf;
        if (Br < 0) System.err.println("Negative Br detected.");
    }

    

}

