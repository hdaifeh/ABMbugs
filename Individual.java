// Import the Arrays utility class to use methods like Arrays.fill()

import java.util.Arrays;

/**
 * This class represents an individual entity in a waste management simulation
 * model. It tracks population growth, waste production, and behavioral
 * intentions related to waste management practices like composting and sorting.
 */
public class Individual {

    // Instance variable to track the amount of waste produced by this individual
    private double wasteProduction;

    // Intention variables that represent the individual's propensity to compost waste
    //private double compostingIntention;

    // Intention variable that represents the individual's propensity to sort waste for dedicated collection bin (eventually to methanisation)
   // private double sortingIntention;

    // Array to store population values over time (for each year in the simulation)
    //private double[] P;
    // Array to store food waste production values over time
    double Bpf;

    // Array to store green waste production values over time
    double Bpg;

    // Base food waste production coefficient per capita
    private double b_pf;

    // Base green waste production coefficient per capita
    private double b_pg;

    // Initial value for food composting intention parameter
    //private double αcf_initial;

    // Initial value for green composting intention parameter
    //private double αcg_initial;

    // Initial value for food sorting intention parameter
    //private double αsf_initial;

    // Initial value for green sorting intention parameter
   // private double αsg_initial;

    int yearRef; // year of departure for individual composting capacity non-limiting to departure
    int timeBeforeInit_αcf_initial; // here we consider the evolution of behavioural intention of food composting before the initial observation
    int timeBeforeInit_αcg_initial; // here we consider the evolution of behavioural intention of green composting before the initial observation
    int timeBeforeInit_αsf_initial; // here we consider the evolution of behavioural intention of food sorting for dedicated collection before the initial observation (it's 2 for CAM)
    int timeBeforeInit_αsg_initial; // here we consider the evolution of behavioural intention of green sorting for dedicated collection before the initial observation

    double Kc_initial; // (parameter) initial capacity of home composting 
    double Ks_initial; // (parameter) initial dedicated collection capacity 
    double Kct; // (variable) linear evolution of home composter until planned capacity  
    double Kst; // (variable) linear evolution of dedicated collection until planned capacity 
    double αc_target; // (parameter) the planned maximum capacity of home composter 
    double αs_target; // (parameter) the planned maximum capacity of dedicated collection
    
    double[] LinearHomeComposter; // linear function for planned capacity evolution of home composter 
    double[] sigmoide_mcf; // innovation diffusion function of food composting behavioural intention evolution 
    double[] sigmoide_mcg; // innovation diffusion function of green composting behavioural intention evolution 
    double[] LinearDedicatedCollection; // linear function for planned capacity evolution of dedicated collection
    double[] sigmoide_msf; // innovation diffusion function of evolution of food sorting for dedicated collection behavioural intention  
    double[] sigmoide_msg; // innovation diffusion function of evolution of green sorting for dedicated collection behavioural intention 
    
    double αcf_initial; // initial behavioural intention of food composting 
    double αcg_initial; // initial behavioural intention of green composting
    double αcf_max; // maximum evolution of food composting behavioural intention (here we consider it as one)
    double αcg_max; // maximum evolution of green composting behavioural intention (here we consider it as one)
    double αsf_initial; // initial behavioural intention of food sorting for dedicated collection
    double αsf_max; // maximum evolution of food sorting for dedicated collection behavioural intention (here we consider it as one)
    double αsg_initial; // initial behavioural intention of green sorting for dedicated collection
    double αsg_max; // maximum evolution of green sorting for dedicated collection behavioural intention (here we consider it as one)
    double αv; // volume of green waste sent to the valorisation centre
    
    double mc; // inflexion point of the practical sigmoid curve of home composting
    double duraImplemCollect; // linear function for dedicated collection capacity development 
    double ms; // inflexion point of the practical sigmoid curve of sorting for dedicated collection
    
    double αcf; // the sorting of food waste for home composting intentions 
    double αcg; // the sorting of green waste for home composting intentions 
    double αvg; // the volume of green waste sent to the valorisation centre in each collection territory as consequences of other intentions
    double αsf; // the intentions of practical sorting for dedicated collection behaviour for food waste at time 
    double αsg; // the intentions of practical sorting for dedicated collection behaviour for green waste at time 
    double C_log; // From sigmoid for evolution of individual composting logistics
    double C_pop; // From sigmoid giving speed of individual evolution for composting practice
    double Bcg; // The biomass of home compostable green waste 
    double Bcf; // The biomass of home compostable food waste 
    double Bcf_composted; // the biomass of home composted food waste
    double Bcg_composted; // the biomass of home composted green waste
    double Bc_composted; // the biomass of home composted biowaste
    double Uc; // home composting-part surplus 
    double Ucg; // Quantity of green biowaste removed from local composting due to surplus
    double Ucf; // Quantity of food biowaste removed from local composting due to surplus
    double sLbis; // Intermediate management of local composting surpluses (adjusted composted)
    double Bv; // Green waste directed to the green valorisation centres 
    double Bsg; // sortable green waste for dedicated collection
    double Bsf; // sortable food waste for dedicated collection
    double Bs_sorted; // biomass of sorted green and food waste in dedicated collection 
    double Bsf_sorted; // biomass of sorted food waste in dedicated collection 
    double Bsg_sorted; // biomass of sorted green waste in dedicated collection 
    double Usf; // sorting-part food waste surplus 
    double Usg; // sorting-part green waste surplus 
    double sAa_bis; // Quantity of food waste removed from collection due to surplus
    double sAv_bis; // Quantity of green biowaste removed from collection due to surplus
    double Us; // Surplus from collection #1
    double sAbis; // Surplus from collection #2
    double Br; // food waste directed to the residual household waste 
    
    
    
    
    

    /**
     * Constructor that initializes an Individual with specified parameters.
     *
     * @param sizeData The number of time periods (years) to simulate
     */
    public Individual(int sizeData, double bpf, double bpg) {
        // Initialize base coefficients and parameters from the params array
        b_pf = bpf;        // Base food waste production coefficient
        b_pg = bpg;        // Base green waste production coefficient
                // Initialize arrays to track values over simulation time periods
        Bpf = bpf ;  // Food waste array
        Bpg = bpg ;  // Green waste array

// Initialize composter arrays
/*
    Bcf = new double[sizeData];  // Food waste for composting array
    Bcg = new double[sizeData];  // Green waste for composting array
    Bcf_composted = new double[sizeData];  // Composted food waste array
    Bcg_composted = new double[sizeData];  // Composted green waste array
    Bc_composted = new double[sizeData];   // Total composted waste
    
    // Initialize intention arrays
    αcf = new double[sizeData];  // Food composting intention array
    αcg = new double[sizeData];  // Green composting intention array
    αvg = new double[sizeData];  // Green waste to valorisation center intention array
    αsf = new double[sizeData];  // Food sorting intention array for dedicated collection
    αsg = new double[sizeData];  // Green sorting intention array for dedicated collection
    
    // Initialize capacity arrays
    Kct = new double[sizeData];  // Home composting capacity evolution array
    Kst = new double[sizeData];  // Dedicated collection capacity evolution array
    
    // Innovation diffusion function arrays
*/
    LinearHomeComposter = new double[sizeData];  // Linear function for home composter capacity
    sigmoide_mcf = new double[sizeData];  // Sigmoid for food composting intention
    sigmoide_mcg = new double[sizeData];  // Sigmoid for green composting intention
    LinearDedicatedCollection = new double[sizeData];  // Linear function for dedicated collection
    sigmoide_msf = new double[sizeData];  // Sigmoid for food sorting intention
    sigmoide_msg = new double[sizeData];  // Sigmoid for green sorting intention
 /*   
    // Surplus arrays
    Uc = new double[sizeData];  // Home composting surplus
    Ucf = new double[sizeData];  // Food waste surplus from composting
    Ucg = new double[sizeData];  // Green waste surplus from composting
    sLbis = new double[sizeData];  // Intermediate management of composting surpluses
    
    // Waste destination arrays
    Bv = new double[sizeData];  // Green waste to valorisation center
    Bsg = new double[sizeData];  // Sortable green waste for dedicated collection
    Bsf = new double[sizeData];  // Sortable food waste for dedicated collection
    Bs_sorted = new double[sizeData];  // Total waste sorted in dedicated collection
    Bsf_sorted = new double[sizeData];  // Food waste sorted in dedicated collection
    Bsg_sorted = new double[sizeData];  // Green waste sorted in dedicated collection
    
    // Sorting surplus arrays
    Usf = new double[sizeData];  // Food waste surplus from sorting
    Usg = new double[sizeData];  // Green waste surplus from sorting
    sAa_bis = new double[sizeData];  // Food waste removed from collection due to surplus
    sAv_bis = new double[sizeData];  // Green waste removed from collection due to surplus
    Us = new double[sizeData];  // Surplus from collection #1
    sAbis = new double[sizeData];  // Surplus from collection #2
    
    // Residual waste array
    Br = new double[sizeData];  // Food waste to residual household waste
    
    // Innovation diffusion model arrays
    C_log = new double[sizeData];  // From sigmoid for composting logistics evolution
    C_pop = new double[sizeData];  // From sigmoid for composting practice population evolution
    
    // Initialize default values for all arrays
    Arrays.fill(Bpf, b_pf);  // Initialize food waste production with base coefficient
    Arrays.fill(Bpg, b_pg);  // Initialize green waste production with base coefficient
  */  
     
    }

    /**
     * Calculates and updates the waste production for a specific year based on
     * population growth and waste reduction targets.
     *
     * @param year The current year in the simulation
     * @param αpf_target Target reduction for waste production
     * @param sigmoideABP Array of sigmoid function values modeling behavioral
     * change over time
     */
    public void computeWaste(int year, double αpf_target, double[] sigmoideABP) {
        //ABP[year] = αpf_target * sigmoideABP[year] ; 
        // Calculate food waste, reduced by target value and sigmoid-modeled behavior change
        Bpf = Bpf * (1 - αpf_target * sigmoideABP[year]);
        // Calculate green waste, using same reduction model as food waste
        Bpg = Bpg * (1 - αpf_target * sigmoideABP[year]);
    }

    
    public void computeBehavioralIntentions(int y) {// social behaviour
        
        double trucDa;
        double trucDv;
        αcf = Math.min((αcf_initial + ((1 - αcf_initial) * sigmoide_mcf[y - 1])), 1.0);
        αcg = Math.min((αcg_initial + ((1 - αcg_initial) * sigmoide_mcg[y - 1])), 1.0); // Proportion of biowaste going to local composting taking into account actors   
        αsf = αsf_initial + ((1 - αsf_initial) * sigmoide_msf[y]); // we prioritise the desire to compost and assume that people compost or participate in collection
        trucDa = αcf + αsf;
        if (trucDa > 1.0) {
            αsf = (1 - αcf);
        }
        αsg = αsg_initial + ((αsg_max - αsg_initial) * sigmoide_msg[y]);
        // System.err.println(y+" "+αsg_initial+" "+αsg[y]+" "+sigmoide_msg[y]);
        trucDv = αcg + αsg;
        if (trucDv > 1.0) {
            αsg = 1.0 - αcg;
        }
        αvg = 1 - αcg - αsg; // concerns only green waste which goes to the recycling centre

        // System.err.println("αsf_initial: " + αsf_initial);
        // System.err.println("αsg_initial: " + αsg_initial);
        // System.err.println("αsf_max: " + αsf_max);
        // System.err.println("αsg_max: " + αsg_max);
    }

    public void compost(int y) { // y stands for year
        Bcg = αcg * Bpg; // Quantity of green waste going towards local composting
        Bcf = αcf * Bpf; // Quantity of food waste going towards local composting
        // → HYPOTHESIS: If L[y] > K1: We have a surplus, then we will: First put green biowaste Bcg[y] in the recycling centre then if Bcg[y] is empty and there is still a surplus and L[y] is still greater than K1 then we put food biowaste Bcf[y] in the collection.
        if (y == yearRef) { // Calibration for the SBA case
            Kc_initial = Bcg + Bcf;
        }
        Kct = Kc_initial + ((αc_target - Kc_initial) * LinearHomeComposter[y]); // here sigmoid becomes linear see iteration
        // System.err.println("sigmoid " + LinearHomeComposter[y] + " Kct " + Kct + " Bcg " + Bcg[y] + " Bcf " + Bcf[y]+" Py "+P[y-1]+" "+αcg[y]+" Bpg "+Bpg[y]+" αcg "+αcg[y]+" αvg "+αvg[y]);
        if ((Bcg + Bcf) > Kct) {
            Uc = Bcg + Bcf - Kct; // First calculation of surplus
            Bcg_composted = Math.max(Bcg - Uc, 0.0); // Quantity of green biowaste after applying the surplus
            sLbis = Math.max(0.0, (Bcg_composted + Bcf - Kct)); // Second calculation of surplus to see if there is still surplus after removing green biowaste
            Bcf_composted = Math.max(Bcf - sLbis, 0.0); // Quantity of food biowaste after applying the surplus
            Ucf = Math.min(sLbis, Bcf); // Quantity of food biowaste removed due to surplus
            Ucg = Math.min(Uc, Bcg); // Quantity of green biowaste removed due to surplus
            Bcg = Bcg_composted;
            Bcf = Bcf_composted;
        }
        Bc_composted = Bcf + Bcg; // Values of L after removing the surplus 
    }

    public void sortingFoodAndGreenWaste(int y) { // y stands for year
        Bsg = αsg * Bpg; // Quantity of green waste going towards collection
        Bsf = (αsf * Bpf) + Ucf; // → Quantity of food biowaste going towards collection
        // if (y == 1) {
        // System.err.println(αsg[y] + " ka " + Ks_initial + " Bsg " + Bsg[y] + " Bsf " + Bsf[y] + " a3dv " + αvg[y] + " a1dv " + αcg[y]);
        // }
        // → HYPOTHESIS: if A[y] > KA: We have a surplus, then we will: First put green biowaste Bsg[y] in the recycling centre then if Bsg[y] is empty and there is still a surplus and A[y] is still greater than KA then we put food biowaste Bsf[y] in the household residual waste.
        Kst = Ks_initial + ((αs_target - Ks_initial) * LinearDedicatedCollection[y]);
        // if (ident==1) System.err.println("year "+y+" ident terr "+ident+" Kacourant "+Kst);
        if ((Bsg + Bsf) > Kst) {
            Us = Bsf + Bsg - Kst; // → First calculation of surplus
            Bsg_sorted = Math.max(Bsg - Us, 0.0); // Quantity of green waste after applying the surplus
            sAbis = Math.max(0.0, (Bsf + Bsg_sorted - Kst)); // Second calculation of surplus to see if there is still surplus after removing green biowaste
            // Av_bis[y] = Math.max(Bsg[y] - Us[y], 0.0); // Quantity of green biowaste after applying the surplus
            Bsf_sorted = Math.max(Bsf - sAbis, 0.0); // → Quantity of food biowaste after applying the surplus
            Usg = Math.min(Us, Bsg); // Quantity of green biowaste removed due to surplus GOES TO THE RECYCLING CENTRE!!!!
            // if (Usg[y]<0.0) { System.err.println(" jfjqkdksdj "+Us[y]+" "+Bsg[y]); }
            // Dv[y]=Bv[y]+Usg[y]; // putting surplus back to the recycling centre
            Usf = Math.min(sAbis, Bsf); // → Quantity of food biowaste removed due to surplus
            Bsg = Bsg_sorted;
            Bsf = Bsf_sorted;
        }
        Bs_sorted = Bsg + Bsf; // → Value of A[y] after removing the surplus
    }

    public void putInValCenter(int y) {
        Bv = αvg * Bpg + Ucg + Usg; // Quantity of green biowaste going towards the recycling centre
    }

    public void putInBlackBin(int y) {
        Br = (1 - αcf - αsf) * Bpf + Usf; // Quantity of food biowaste going towards residual household waste
        if (Br < 0) {
            System.err.println(αsf + " alpha1 " + αcf + " Ba " + Bpf + " sAa " + Usf);
        }
    }

}


