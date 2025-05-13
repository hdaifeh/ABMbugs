// Import the Arrays utility class to use methods like Arrays.fill()

import java.util.Arrays;

/**
 * This class represents an individual entity in a waste management simulation
 * model. It tracks population growth, waste production, and behavioral
 * intentions related to waste management practices like composting and sorting.
 */
public class Individual {

    private CollectionTerritory territory;

    private double wasteProduction;

    double Bpf;

    double Bpg;

    private double b_pf;

    // Base green waste production coefficient per capita
    private double b_pg;



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
    //double C_pop; // From sigmoid giving speed of individual evolution for composting practice
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
    public Individual(int sizeData, double bpf, double bpg, CollectionTerritory territory) {
        // Initialize base coefficients and parameters from the params array
       
        this.territory = territory;
        this.b_pf = bpf;
        this.b_pg = bpg;
        b_pf = bpf;        // Base food waste production coefficient
        b_pg = bpg;        // Base green waste production coefficient
                // Initialize arrays to track values over simulation time periods
        Bpf = bpf ;  // Food waste array
        Bpg = bpg ;  // Green waste array
       
// Initialize composter arrays

    LinearHomeComposter = new double[sizeData];  // Linear function for home composter capacity
    sigmoide_mcf = new double[sizeData];  // Sigmoid for food composting intention
    sigmoide_mcg = new double[sizeData];  // Sigmoid for green composting intention
    LinearDedicatedCollection = new double[sizeData];  // Linear function for dedicated collection
    sigmoide_msf = new double[sizeData];  // Sigmoid for food sorting intention
    sigmoide_msg = new double[sizeData];  // Sigmoid for green sorting intention
   
    
        
     
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
     
        // Calculate food waste, reduced by target value and sigmoid-modeled behavior change
        
        Bpf = Bpf * (1 - αpf_target * sigmoideABP[year]);
        
       // Bpf *= (1 - αpf_target * sigmoideABP[year]);
        
        Bpg = Bpg *(1 - αpf_target * sigmoideABP[year]);
       //Bpg *= (1 - αpf_target * sigmoideABP[year]);
    }

    
public void computeBehavioralIntentions(int y) {
        double trucDa, trucDv;
        αcf = Math.min(αcf_initial + (1 - αcf_initial) * territory.sigmoide_mcf[y - 1], 1.0);
        αcg = Math.min(αcg_initial + (1 - αcg_initial) * territory.sigmoide_mcg[y - 1], 1.0);
        αsf = αsf_initial + (1 - αsf_initial) * territory.sigmoide_msf[y];
        
        if ((trucDa = αcf + αsf) > 1.0) αsf = 1 - αcf;
        αsg = αsg_initial + (αsg_max - αsg_initial) * territory.sigmoide_msg[y];
        
        if ((trucDv = αcg + αsg) > 1.0) αsg = 1 - αcg;
        αvg = 1 - αcg - αsg;
    }


     public void compost(int y) {
        
        Bcg = αcg * Bpg;
        Bcf = αcf * Bpf;
        
        if (y == territory.yearRef) Kc_initial = Bcg + Bcf;
        Kct = Kc_initial + (territory.αc_target - Kc_initial) * territory.LinearHomeComposter[y];
        
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

    public void sortingFoodAndGreenWaste(int y) {
        Bsg = αsg * Bpg;
        Bsf = αsf * Bpf + Ucf;
        
        Kst = territory.Ks_initial + (territory.αs_target - territory.Ks_initial) * territory.LinearDedicatedCollection[y];
        
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

