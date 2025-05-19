import java.util.Arrays;

/**
 * @author shuet
 * Modified to support household-based waste calculations
 */
public class CollectionTerritory {

    Territory myTerre;
    Individual[] myHouseholds; // Changed from myIndividuals to myHouseholds
    // Define the starting point of the simulation
    int timeBeforeInit_αcf_initial; // here we consider the evolution of behavioural intention of food composting before the initial observation
    int timeBeforeInit_αcg_initial; // here we consider the evolution of behavioural intention of green composting before the initial observation
    int timeBeforeInit_αsf_initial; // here we consider the evolution of behavioural intention of food sorting for dedicated collection before the initial observation (it's 2 for CAM)
    int timeBeforeInit_αsg_initial; // here we consider the evolution of behavioural intention of green sorting for dedicated collection before the initial observation

    double Kc_initial; // (parameter) initial capacity of home composting 
    double Ks_initial; // (parameter) initial dedicated collection capacity 
    double[] Kct; // (variable) linear evolution of home composter until planned capacity  
    double[] Kst; // (variable) linear evolution of dedicated collection until planned capacity 
    double αc_target; // (parameter) the planned maximum capacity of home composter 
    double αs_target; // (parameter) the planned maximum capacity of dedicated collection
    int yearRef; // year of departure for individual composting capacity non-limiting to departure

    double[] LinearHomeComposter; // linear function for planned capacity evolution of home composter 

    double[] sigmoide_mcf; // innovation diffusion function of food composting behavioural intention evolution 
    double[] sigmoide_mcg; // innovation diffusion function of green composting behavioural intention evolution 

    double[] LinearDedicatedCollection; // linear function for planned capacity evolution of dedicated collection

    double[] sigmoide_msf; // innovation diffusion function of evolution of food sorting for dedicated collection behavioural intention  
    double[] sigmoide_msg; // innovation diffusion function of evolution of green sorting for dedicated collection behavioural intention 

    double[] sigmoide_mpg; // innovation diffusion function of evolution of reduction behaviour of green waste
    double αcf_initial; // initial behavioural intention of food composting 
    double αcg_initial; // initial behavioural intention of green composting
    double αcf_max; // maximum evolution of food composting behavioural intention (here we consider it as one)
    double αcg_max; // maximum evolution of green composting behavioural intention (here we consider it as one)
    double αsf_initial; // initial behavioural intention of food sorting for dedicated collection
    double αsf_max; // maximum evolution of food sorting for dedicated collection behavioural intention (here we consider it as one)
    double αsg_initial; // initial behavioural intention of green sorting for dedicated collection
    double αsg_max; // maximum evolution of green sorting for dedicated collection behavioural intention (here we consider it as one)

    // TODO HYPOTHESIS: NO CAPACITY FOR RECYCLING CENTRE + ONLY ONE RECYCLING CENTRE PER TERRITORY
    double b_pf; // baseline food waste production per household (which varies for each collection territory). per household/year in tonnes   
    double b_pg; // baseline green waste production per household (which varies for each collection territory). per household/year in tonnes            

    double αv; // volume of green waste sent to the valorisation centre 

    double householdSize = 2.407407; // Average number of individuals per household
    int numHouseholds; // Number of households in collection territory
    
    double duraImplemCompo; // linear function for home composter capacity development 
    double mc; // inflexion point of the practical sigmoid curve of home composting
    double duraImplemCollect; // linear function for dedicated collection capacity development 
    double ms; // inflexion point of the practical sigmoid curve of sorting for dedicated collection
    double mpg; // inflexion point of the green waste reduction sigmoid curve
    double αpg_target; // the ABP's target reduction for green waste to be achieved by the target year

    double[] H; // Number of households in collection territory at year t (now fixed, no growth)
    double[] B; // Quantity of biowaste produced per households
    double[] Bpg; // The household's green waste production from each collection territory 
    double[] Bpf; // The household's food waste production from each collection territory 
    double[] ABP; // Food waste to be removed as a result of reducing food waste
    double[] R; // rate of reduction of food waste at t as a function of objGaspi at term (term = 2xtiantigaspi of the sigmoid) AFTER THAT CONSTANT RATE
    double[] G; // Quantity reduction in food waste 
    double[] αcf; // the sorting of food waste for home composting intentions 
    double[] αcg; // the sorting of green waste for home composting intentions 
    double[] αvg; // the volume of green waste sent to the valorisation centre in each collection territory as consequences of other intentions
    double[] αsf; // the intentions of practical sorting for dedicated collection behaviour for food waste at time 
    double[] αsg; // the intentions of practical sorting for dedicated collection behaviour for green waste at time 
    double[] C_log; // From sigmoid for evolution of individual composting logistics
    double[] C_pop; // From sigmoid giving speed of individual evolution for composting practice
    double[] Bcg; // The biomass of home compostable green waste 
    double[] Bcf; // The biomass of home compostable food waste 
   double[] Bcf_composted; // the biomass of home composted food waste
    double[] Bcg_composted; // the biomass of home composted green waste
    double[] Bc_composted; // the biomass of home composted biowaste
    double[] Uc; // home composting-part surplus 
    double[] Ucg; // Quantity of green biowaste removed from local composting due to surplus
    double[] Ucf; // Quantity of food biowaste removed from local composting due to surplus
    double[] sLbis; // Intermediate management of local composting surpluses (adjusted composted)
    double[] Bv; // Green waste directed to the green valorisation centres 
    double[] Bsg; // sortable green waste for dedicated collection
    double[] Bsf; // sortable food waste for dedicated collection
    double[] Bs_sorted; // biomass of sorted green and food waste in dedicated collection 
    double[] Bsf_sorted; // biomass of sorted food waste in dedicated collection 
    double[] Bsg_sorted; // biomass of sorted green waste in dedicated collection 
    double[] Usf; // sorting-part food waste surplus 
    double[] Usg; // sorting-part green waste surplus 
    double[] sAa_bis; // Quantity of food waste removed from collection due to surplus
    double[] sAv_bis; // Quantity of green biowaste removed from collection due to surplus
    double[] Us; // Surplus from collection #1
    double[] sAbis; // Surplus from collection #2
    double[] Br; // food waste directed to the residual household waste 
    double αpf_target; // the ABP's target reduction for food waste to be achieved by the target

    int collectionTerritoryName;

    double[] propPopDesserviCollDA; // proportion of the population served by food waste collection in a given year
    double[] nbKgCollectHabDesservi; // number of kilograms of food waste collected per inhabitant served by the collection in a given year
    double[] nbKgOMRHab; // number of kilograms of food waste collected per inhabitant served by the collection in a given year
    // EquipmentValorisation myOwnEquip; // FOR NOW WE CONSIDER THAT SUBTERRITORIES DO NOT HAVE THEIR OWN EQUIPMENT OR THEIR CAPACITIES ARE SUMMED TO MAKE A COMMON EQUIPMENT
    double[] tauxReductionDechetVert; // rate of reduction of green waste entering the recycling centre
    int ident; // sub-territory number

    public CollectionTerritory(Territory mt, int id) {
        myTerre = mt;
        ident = id;
    }

    public void iterate(int year) {
        LinearHomeComposter[year] = linear(year, duraImplemCompo);
        LinearDedicatedCollection[year] = linear(year, duraImplemCollect); // logistic sorting capacity: duration = 7 
        if (myTerre.useSocialDynamics) {
            sigmoide_mcf[year] = sigmoide(year + timeBeforeInit_αcf_initial, mc); // Practical composting behaviour
            sigmoide_mcg[year] = sigmoide(year + timeBeforeInit_αcg_initial, mc); // Practical composting behaviour
            sigmoide_msf[year] = sigmoide(year + timeBeforeInit_αsf_initial, ms); // Practical sorting behaviour
            sigmoide_msg[year] = sigmoide(year + timeBeforeInit_αsg_initial, ms); // Practical sorting behaviour
            sigmoide_mpg[year] = sigmoide(year, mpg); // reduction green waste adoption 
        }
        
        // Process each household instead of each individual
        for (int i = 0; i < numHouseholds; i++) {
            myHouseholds[i].computeWaste(year, αpf_target, myTerre.sigmoideABP);
            myHouseholds[i].computeBehavioralIntentions(year);
            myHouseholds[i].compost(year);
            myHouseholds[i].sortingFoodAndGreenWaste(year);
            myHouseholds[i].putInValCenter(year);
            myHouseholds[i].putInBlackBin(year);
        }
        computeCollectionTerritoryIndicator(year);                
    }

   
    public void computeProducedBioWaste(int y) {
        // Fixed households (no growth)
        H[y] = numHouseholds; 

        R[y] = αpf_target * myTerre.sigmoideABP[y]; // rate of reduction of food waste at t as a function of objGaspi at term
        ABP[y] = αpg_target * myTerre.sigmoideABP[y]; // Amount of food waste to be removed due to reduction of food waste

        // Calculate per household, not per capita
        Bpg[y] = b_pg * (1 - ABP[y]) * H[y];
        Bpf[y] = b_pf * (1 - ABP[y] * myTerre.einit) * H[y]; 
        B[y] = Bpg[y] + Bpf[y]; // Total quantity of biowaste produced by all households
    }

    
    public double sigmoide(double x, double ti) {
        double t = Math.pow(x, 5);
        double z = t / (t + Math.pow(ti, 5)); // ti is the inflexion point of the sigmoid (the value 0.5 is returned in the ti-th year)
        return z;
    }

    public double linear(double t, double duration) {
        return Math.min(t / duration, 1.0);
    }

    public int calculateTimeBeforeInit(double alpha_base, double ti) { // time process
        int timeBeforeInit = 0;
        // Continuously calculate the sigmoid value at increasing time steps until it meets or exceeds alpha_base.
        if (alpha_base > 0) {
            double sigmoideValue = sigmoide(timeBeforeInit, ti);
            while (sigmoideValue < alpha_base) { // (the while-loop will not be entered if alpha_base is less than or equal to zero).
                timeBeforeInit++;
                sigmoideValue = sigmoide(timeBeforeInit, ti);
            }
        }
        return timeBeforeInit;
    }

    public void init(int sizeData, double[] params, int refYear) { // to give a value for the empty table
        yearRef = refYear;
        collectionTerritoryName = (int) params[0]; // numerical identifier of the sub-territory
        duraImplemCompo = params[1]; // inflexion point of the sigmoid curve
        duraImplemCollect = params[2]; // inflexion point of the sigmoid curve
        mc = params[3]; // inflexion point of the sigmoid curve
        ms = params[4]; // inflexion point of the sigmoid curve
        
        // Convert per capita waste to per household waste
        b_pf = params[5] * householdSize; // Quantity of food waste produced per household (originally per inhabitant)
        b_pg = params[6] * householdSize; // Quantity of green waste produced per household (originally per inhabitant)
        
        αcf_initial = params[7];
        αcg_initial = params[8];
        αsf_initial = params[9]; // practice of sorting for door-to-door collection init (from 70% to 95%)
        αsf_max = params[10]; // practice of sorting for door-to-door collection target (from 70% to 95%)
        αcf_max = params[11]; // desire to increase local composting practice for food waste
        αcg_max = params[12]; // same as above but for green waste
        αsg_initial = params[13]; // initial sorting of green waste
        αsg_max = params[14];
        Kc_initial = params[15]; // annual population growth according to national statistics
        αc_target = params[16]; // K(expected)^c
        Ks_initial = params[17]; // annual population growth according to national statistics
        αs_target = params[18];
        
        // Calculate number of households based on population and household size
        int totalPopulation = (int) params[19]; // population size of sub-territory
        numHouseholds = (int) Math.ceil(totalPopulation / householdSize); // Convert population to number of households
        
        // No longer using population growth rate
        mpg = params[21]; // tiActionsAvoidanceGreenWaste
        αpg_target = params[22]; // rateAvoidanceGreenWasteHorizon2024        
        αpf_target = params[23];
        
        // Calculating time before the simulation starts for each alpha value (calculating time process)
        timeBeforeInit_αcf_initial = calculateTimeBeforeInit(αcf_initial, mc);
        timeBeforeInit_αcg_initial = calculateTimeBeforeInit(αcg_initial, mc);
        timeBeforeInit_αsf_initial = calculateTimeBeforeInit(αsf_initial, ms);
        timeBeforeInit_αsg_initial = calculateTimeBeforeInit(αsg_initial, ms);

        H = new double[sizeData]; // Number of households (fixed)
        Arrays.fill(H, numHouseholds);
        
        R = new double[sizeData];
        Arrays.fill(R, 0.0);
        ABP = new double[sizeData];
        Arrays.fill(ABP, 0.0);
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
        Arrays.fill(Kct, 0.0); // to stock the result 

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
        
        // Initialize households instead of individuals
        myHouseholds = new Individual[numHouseholds];
for (int i = 0; i < numHouseholds; i++) {
    // Pass the per-household waste values directly
    myHouseholds[i] = new Individual(sizeData, b_pf, b_pg, this);
}
        computeCollectionTerritoryIndicator(0);
    }

    public void printVector(double[] edit) {
        for (int i = 0; i < edit.length; i++) {
            System.err.print(edit[i] + "\t");
        }
        System.err.println();
    }

    public void indicSubTerritories(int year) {
        // Calculate based on total number of households x household size for population estimates
        double totalPopulation = H[year] * householdSize;
        double nbHabDesservi = Math.min(totalPopulation, (double) Kst[year] / (39.0 / 1000.0)); 
        
        propPopDesserviCollDA[year] = nbHabDesservi / totalPopulation;
        if (nbHabDesservi > 0) {
            nbKgCollectHabDesservi[year] = (Bsf[year] * 1000.0) / nbHabDesservi;
        }
        nbKgOMRHab[year] = (Br[year] * 1000.0) / totalPopulation;
        tauxReductionDechetVert[year] = (Bv[year] - Bv[0]) / Bv[0]; // this evolution percentage of green waste in dechetre, negative value means reduction
    }

    public void printTrajectory(int year) {
        System.out.print(ident + ";");
        System.out.print(H[year] + ";"); // Print households instead of population
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

    public void computeCollectionTerritoryIndicator(int time) {
        // Reset aggregated values to zero before summing
        //Aggregated biowaste production
        Bpf[time] = 0;
        Bpg[time] = 0;
        
        //Aggregated composting behavioural intention 
        αcf[time] = 0;
        αcg[time] = 0;
        
        //Aggregated composted biowaste 
        Bcf[time] = 0;
        Bcg[time] = 0;
   
        Bc_composted[time] = 0;
        Bs_sorted[time] = 0;
        //Aggregated capacity of homecomposter 
        Kct[time] = 0;
        
        // Aggregated surplus (composting part surplus)
        Ucf[time] = 0;
        Ucg[time] = 0;
        
        //Aggregated sorting behavioural intention 
        αsf[time] = 0;
        αsg[time] = 0;
        
        //Aggregated sorted biowaste     
        Bsf[time] = 0;
        Bsg[time] = 0;
        
        //Aggregated capacity of dedicated collection for methanisation
        Kst[time] = 0;

        // Aggregated surplus (sortingpart surplus)        
        Usf[time] = 0;
        Usg[time] = 0;
        
        // Aggregated green waste in valorisation center 
        Bv[time] = 0;
        
        // Aggregated food waste in OMR residual Households
        Br[time] = 0;

        // Sum up data from all households
        for (int i = 0; i < numHouseholds; i++) {
            // Sum up waste production values
            Bpf[time] += myHouseholds[i].Bpf;
            Bpg[time] += myHouseholds[i].Bpg;

            // Sum up behavioral intention values
            αcf[time] += myHouseholds[i].αcf;
            αcg[time] += myHouseholds[i].αcg;
            αsf[time] += myHouseholds[i].αsf;
            αsg[time] += myHouseholds[i].αsg;

            // Sum up composting values
            Bcg[time] += myHouseholds[i].Bcg;
            Bcf[time] += myHouseholds[i].Bcf;

            // Sum up sorting values
            Bsf[time] += myHouseholds[i].Bsf;
            Bsg[time] += myHouseholds[i].Bsg;

            // Also collect other data points you need
            Ucf[time] += myHouseholds[i].Ucf;
            Ucg[time] += myHouseholds[i].Ucg;
            Usf[time] += myHouseholds[i].Usf;
            Usg[time] += myHouseholds[i].Usg;

            Kst[time] += myHouseholds[i].Kst;
            Kct[time] += myHouseholds[i].Kct;
        }

        // Calculate averages for the behavioral intentions
        if (numHouseholds > 0) {
            αcf[time] /= numHouseholds;
            αcg[time] /= numHouseholds;
            αsf[time] /= numHouseholds;
            αsg[time] /= numHouseholds;
        }

        // Calculate derived values
        Bv[time] = Bpg[time] - Bcg[time] - Bsg[time];
        Br[time] = Bpf[time] - Bcf[time] - Bsf[time];
        B[time] = Bpf[time] + Bpg[time];
        Bc_composted[time] = Bcf[time] + Bcg[time];
        Bs_sorted[time] = Bsf[time] + Bsg[time];
    }
}
