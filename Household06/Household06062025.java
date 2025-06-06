import java.util.Arrays;

/**
 * This class represents a household entity in a waste management simulation
 * model. It tracks waste production and behavioral intentions related to 
 * waste management practices like composting and sorting.
 * Modified to handle dynamic household size over time and ensure SD validation.
 */
public class Household {

    private CollectionTerritory territory;
    
    // Dynamic household size
    private double currentHouseholdSize;
    
    // Per capita waste production coefficients (read from input file)
    private double b_pf_per_capita; // Base food waste production coefficient per capita
    private double b_pg_per_capita; // Base green waste production coefficient per capita

    // Current waste production (calculated based on household size)
    double Bpf; // Food waste production for this household
    double Bpg; // Green waste production for this household

    // Behavioral intentions
    double αcf; // Food composting intention
    double αcg; // Green composting intention
    double αvg; // Green to valorization center intention
    double αsf; // Food sorting for dedicated collection intention
    double αsg; // Green sorting for dedicated collection intention
    
    // Infrastructure capacities (per household)
    double Kct; // Home composter capacity for this household
    double Kst; // Dedicated collection capacity for this household
    
    // Waste flows
    double Bcg; // Green waste for composting
    double Bcf; // Food waste for composting
    double Bcf_composted; // Actually composted food waste
    double Bcg_composted; // Actually composted green waste
    double Bc_composted; // Total composted waste
    
    // Surplus amounts
    double Uc; // Total composting surplus
    double Ucg; // Green composting surplus
    double Ucf; // Food composting surplus
    double sLbis; // Intermediate surplus calculation
    
    // Collection flows
    double Bsg; // Green waste for sorting
    double Bsf; // Food waste for sorting
    double Bsg_sorted; // Actually sorted green waste
    double Bsf_sorted; // Actually sorted food waste
    double Bs_sorted; // Total sorted waste
    
    // Collection surplus
    double Us; // Total sorting surplus
    double Usg; // Green sorting surplus
    double Usf; // Food sorting surplus
    double sAbis; // Intermediate surplus calculation for sorting
    
    // Final destinations
    double Bv; // Green waste to valorization center
    double Br; // Food waste to residual bin

    /**
     * Constructor that initializes a Household with specified parameters.
     *
     * @param sizeData The number of time periods (years) to simulate
     * @param bpf_per_capita Base food waste production per capita
     * @param bpg_per_capita Base green waste production per capita
     * @param territory Reference to the collection territory
     * @param initialHouseholdSize Initial household size
     */
    public Household(int sizeData, double bpf_per_capita, double bpg_per_capita, 
                     CollectionTerritory territory, double initialHouseholdSize) {
        
        this.territory = territory;
        this.b_pf_per_capita = bpf_per_capita;
        this.b_pg_per_capita = bpg_per_capita;
        this.currentHouseholdSize = initialHouseholdSize;
        
        // Initialize all flow variables
        initializeVariables();
        
        // Calculate initial waste production based on household size
        updateWasteProduction();
    }

    /**
     * Initialize all variables to zero
     */
    private void initializeVariables() {
        Bpf = 0.0; Bpg = 0.0;
        αcf = 0.0; αcg = 0.0; αvg = 0.0; αsf = 0.0; αsg = 0.0;
        Kct = 0.0; Kst = 0.0;
        Bcg = 0.0; Bcf = 0.0; Bcf_composted = 0.0; Bcg_composted = 0.0; Bc_composted = 0.0;
        Uc = 0.0; Ucg = 0.0; Ucf = 0.0; sLbis = 0.0;
        Bsg = 0.0; Bsf = 0.0; Bsg_sorted = 0.0; Bsf_sorted = 0.0; Bs_sorted = 0.0;
        Us = 0.0; Usg = 0.0; Usf = 0.0; sAbis = 0.0;
        Bv = 0.0; Br = 0.0;
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
        // Base waste production is per capita * household size
        Bpf = b_pf_per_capita * currentHouseholdSize;
        Bpg = b_pg_per_capita * currentHouseholdSize;
    }

    /**
     * Calculates and updates the waste production for a specific year based on
     * household size and waste reduction targets.
     *
     * @param year The current year in the simulation
     * @param αpf_target Target reduction for food waste production
     * @param αpg_target Target reduction for green waste production  
     * @param sigmoideABP Array of sigmoid function values modeling behavioral change over time
     */
    public void computeWaste(int year, double αpf_target, double αpg_target, double[] sigmoideABP) {
        // Check if sigmoideABP is available (Territory might not be fully initialized)
        if (sigmoideABP == null || year >= sigmoideABP.length) {
            // Fallback to base production without ABP reduction
            Bpf = b_pf_per_capita * currentHouseholdSize;
            Bpg = b_pg_per_capita * currentHouseholdSize;
            return;
        }
        
        // Apply ABP reduction factors to the base waste production
        double foodReductionFactor = αpf_target * sigmoideABP[year] * territory.myTerre.einit;
        double greenReductionFactor = αpg_target * sigmoideABP[year];
        
        Bpf = (b_pf_per_capita * currentHouseholdSize) * (1 - foodReductionFactor);
        Bpg = (b_pg_per_capita * currentHouseholdSize) * (1 - greenReductionFactor);
    }

    /**
     * Computes behavioral intentions for waste management practices using territory-level sigmoid functions
     * This mirrors the SD model equations (6) and (7) and constraints (14-17)
     */
    public void computeBehavioralIntentions(int y) {
        // Composting intentions - using territory sigmoid functions
        αcf = Math.min(territory.αcf_initial + 
                      (1 - territory.αcf_initial) * territory.sigmoide_mcf[Math.max(0, y-1)], 1.0);
        αcg = Math.min(territory.αcg_initial + 
                      (1 - territory.αcg_initial) * territory.sigmoide_mcg[Math.max(0, y-1)], 1.0);
        
        // Sorting intentions - using territory sigmoid functions  
        αsf = territory.αsf_initial + 
              (1 - territory.αsf_initial) * territory.sigmoide_msf[y];
        αsg = territory.αsg_initial + 
              (territory.αsg_max - territory.αsg_initial) * territory.sigmoide_msg[y];
        
        // Apply constraints: composting + sorting ≤ 1 (prioritize composting)
        double trucDa = αcf + αsf;
        if (trucDa > 1.0) {
            αsf = 1.0 - αcf;
        }
        
        double trucDv = αcg + αsg;
        if (trucDv > 1.0) {
            αsg = 1.0 - αcg;
        }
        
        // Valorization center intention (remaining green waste)
        αvg = 1.0 - αcg - αsg;
    }

    /**
     * Computes composting behavior and capacity constraints
     * This mirrors the SD model equations (8-11)
     */
    public void compost(int y) {
        // Calculate intended composting amounts
        Bcg = αcg * Bpg;
        Bcf = αcf * Bpf;
        
        // Calculate household-level composter capacity
        // Distribute territorial capacity equally among households
        double territorialKc = territory.Kc_initial + 
                              (territory.αc_target - territory.Kc_initial) * 
                              territory.LinearHomeComposter[y];
        Kct = territorialKc / territory.numHouseholds;
        
        // Handle capacity constraints with surplus (mirroring SD equations 8-11)
        double totalIntendedCompost = Bcg + Bcf;
        if (totalIntendedCompost > Kct) {
            Uc = totalIntendedCompost - Kct;
            
            // Priority: Food first, then green (following SD model logic)
            Bcf_composted = Math.min(Bcf, Kct);
            double remainingCapacity = Kct - Bcf_composted;
            Bcg_composted = Math.min(Bcg, remainingCapacity);
            
            // Calculate surpluses
            Ucf = Bcf - Bcf_composted;
            Ucg = Bcg - Bcg_composted;
            
            // Update composted amounts
            Bcf = Bcf_composted;
            Bcg = Bcg_composted;
        } else {
            // No capacity constraint
            Ucf = 0.0;
            Ucg = 0.0;
            Uc = 0.0;
        }
        
        Bc_composted = Bcf + Bcg;
    }

    /**
     * Handles sorting for dedicated collection with capacity constraints
     * This mirrors the SD model equations (18-21)
     */
    public void sortingFoodAndGreenWaste(int y) {
        // Calculate intended sorting amounts (including composting surplus)
        Bsg = αsg * Bpg;
        Bsf = αsf * Bpf + Ucf; // Add food composting surplus
        
        // Calculate household-level collection capacity
        // Distribute territorial capacity equally among households
        double territorialKs = territory.Ks_initial + 
                              (territory.αs_target - territory.Ks_initial) * 
                              territory.LinearDedicatedCollection[y];
        Kst = territorialKs / territory.numHouseholds;
        
        // Handle capacity constraints with surplus (mirroring SD equations 18-21)
        double totalIntendedSorting = Bsg + Bsf;
        if (totalIntendedSorting > Kst) {
            Us = totalIntendedSorting - Kst;
            
            // Priority: Food first, then green (following SD model logic)
            Bsf_sorted = Math.min(Bsf, Kst);
            double remainingCapacity = Kst - Bsf_sorted;
            Bsg_sorted = Math.min(Bsg, remainingCapacity);
            
            // Calculate surpluses
            Usf = Bsf - Bsf_sorted;
            Usg = Bsg - Bsg_sorted;
            
            // Update sorted amounts
            Bsf = Bsf_sorted;
            Bsg = Bsg_sorted;
        } else {
            // No capacity constraint
            Usf = 0.0;
            Usg = 0.0;
            Us = 0.0;
        }
        
        Bs_sorted = Bsf + Bsg;
    }

    /**
     * Calculates green waste directed to valorization center
     * This mirrors the SD model equation (22)
     */
    public void putInValCenter(int y) {
        // Green waste to valorization center = direct intention + composting surplus + sorting surplus
        Bv = αvg * Bpg + Ucg + Usg;
    }

    /**
     * Calculates food waste directed to residual household waste (black bin)
     * This mirrors the SD model equation (24)
     */
    public void putInBlackBin(int y) {
        // Food waste to residual bin = direct intention + sorting surplus
        Br = (1 - αcf - αsf) * Bpf + Usf;
        
        if (Br < 0) {
            System.err.println("Negative Br detected in household. αcf=" + αcf + 
                             ", αsf=" + αsf + ", Bpf=" + Bpf + ", Usf=" + Usf);
        }
    }

    /**
     * Getter methods for accessing household data
     */
    public double getCurrentHouseholdSize() { return currentHouseholdSize; }
    // Note: These getters return the current values, not arrays
    // The household stores current state as single double values
}
