
import java.util.Arrays;

/**
 * @author shuet
 */
public class CollectionTerritory {

    Territory myTerre;
    Individual[] myIndividuals;
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
    double b_pf; // baseline food waste production per capita (which varies for each collection territory). per inhabitant/year in tonnes   
    double b_pg; // baseline green waste production per capita (which varies for each collection territory). per inhabitant/year in tonnes            

    double αv; // volume of green waste sent to the valorisation centre 

    double r; // the annual growth rate of the population for each collection territory
    int sizePop; // size of population for each collection territory

    double duraImplemCompo; // linear function for home composter capacity development 
    double mc; // inflexion point of the practical sigmoid curve of home composting
    double duraImplemCollect; // linear function for dedicated collection capacity development 
    double ms; // inflexion point of the practical sigmoid curve of sorting for dedicated collection
    double mpg; // inflexion point of the green waste reduction sigmoid curve
    double αpg_target; // the ABP's target reduction for green waste to be achieved by the target year

    double[] P; // the change in population size of each collection territory at year t
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

    // public CollectionTerritory(Individual mt, int idd) {
    // myIndividual = mt;
    //  ident = idd;
    //  }
    public void iterate(int year) {
        LinearHomeComposter[year] = linear(year, duraImplemCompo);
        LinearDedicatedCollection[year] = linear(year, duraImplemCollect); // logistic sorting capacity: duration = 7 
        if (myTerre.useSocialDynamics) {
            // sigmoideLogCompostLocal[year] = sigmoide(year, duraImplemCompo); // logistic composting capacity
            // logistic composting capacity: duration = 7

            sigmoide_mcf[year] = sigmoide(year + timeBeforeInit_αcf_initial, mc); // Practical composting behaviour: should I add +timeBeforeInit_αcf_initial to mc?

            sigmoide_mcg[year] = sigmoide(year + timeBeforeInit_αcg_initial, mc); // Practical composting behaviour: Should I add timeBeforeInit_αcg_initial to mc?

            // sigmoideLogCollecte[year] = sigmoide(year, duraImplemCollect); // Logistic collection capacity
            sigmoide_msf[year] = sigmoide(year + timeBeforeInit_αsf_initial, ms); // Practical sorting behaviour
            sigmoide_msg[year] = sigmoide(year + timeBeforeInit_αsg_initial, ms); // Practical sorting behaviour
            // System.err.println(year+" "+ms+" timebefore "+timeBeforeInit_αsg_initial+" sig "+sigmoide(year + timeBeforeInit_αsg_initial, ms));
            sigmoide_mpg[year] = sigmoide(year, mpg); // reduction green waste adoption 
        }
        for (int i = 0; i < sizePop; i++) {
            myIndividuals[i].computeWaste(year, αpf_target, myTerre.sigmoideABP);
            myIndividuals[i].computeBehavioralIntentions(year);
            myIndividuals[i].compost(year);
            myIndividuals[i].sortingFoodAndGreenWaste(year);
            myIndividuals[i].putInValCenter(year);
            myIndividuals[i].putInBlackBin(year);
        }
        computeCollectionTerritoryIndicator(year);
    }

    
      public void computeProducedBioWaste(int y) {
        P[y] = P[y - 1] * (1 + r); // Population size at time t
       
        R[y] = αpf_target * myTerre.sigmoideABP[y]; // rate of reduction of food waste at t as a function of objGaspi at term
        // G[y] = myTerre.einit * P[y]; // einit=volume in tons of food waste wasted in 2018 per year and per inhabitant, so total wasted by the population
        // G[y] = myTerre.einit * P[y - 1];
        ABP[y] = αpg_target * myTerre.sigmoideABP[y]; // Amount of food waste to be removed due to reduction of food waste
        
        // Bv[y] = (b_pg - (b_pg * sigmoide_mpg[y] * αpg_target)) * P[y]; // Quantity of green biowaste produced by inhabitants
        Bpg[y] = b_pg * (1 -  ABP[y]) * P[y];
        // Bv[y] = (b_pg - (b_pg * sigmoide_mpg[y] * αpg_target)) * P[y - 1];        
        // System.err.println(αpg_target+" "+sigmoide_mpg[y]);
        // double e = myTerre.einit/BaInit; // the percentage of edible part
        Bpf[y] = b_pf * (1 -  ABP[y] * myTerre.einit) * P[y]; // 𝒃_𝒇^𝒑 (𝟏−𝒐_𝒇^𝒑 𝒁(𝒕,𝒎_𝒇^𝒑) 𝒆) 𝑷(𝒕) it was (y-1) I have changed the parameterisation of the model // Quantity of food biowaste produced by inhabitants taking into account food waste
        // System.err.println(Bpf[y]+" "+b_pf+" "+P[y]+" "+ABP[y]+" "+R[y]+" "+G[y]+" "+myTerre.objGaspi+" "+myTerre.einit+" "+myTerre.sigmoideABP[y]);
        B[y] = Bpg[y] + Bpf[y]; // Quantity of biowaste produced 
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
                // System.err.println(alpha_base + " " + ti + " " + timeBeforeInit + " " + sigmoideValue);
            }
        }
        return timeBeforeInit;
    }
// Do i need to move this part to the individual? 

    public void init(int sizeData, double[] params, int refYear) { // to give a value for the empty table
        yearRef = refYear;
        collectionTerritoryName = (int) params[0]; // numerical identifier of the sub-territory
        duraImplemCompo = params[1]; // inflexion point of the sigmoid curve
        duraImplemCollect = params[2]; // inflexion point of the sigmoid curve
        mc = params[3]; // inflexion point of the sigmoid curve
        ms = params[4]; // inflexion point of the sigmoid curve
        b_pf = params[5]; // Quantity of biowaste produced per inhabitant
        b_pg = params[6]; // proportion of green waste in b
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
        sizePop = (int) params[19]; // population size of sub-territory
        r = params[20]; // population size of sub-territory
        mpg = params[21]; // tiActionsAvoidanceGreenWaste
        αpg_target = params[22]; // rateAvoidanceGreenWasteHorizon2024        
        αpf_target = params[23];
        // Calculating time before the simulation starts for each alpha value (calculating time process)
        timeBeforeInit_αcf_initial = calculateTimeBeforeInit(αcf_initial, mc);
        timeBeforeInit_αcg_initial = calculateTimeBeforeInit(αcg_initial, mc);
        timeBeforeInit_αsf_initial = calculateTimeBeforeInit(αsf_initial, ms);
        timeBeforeInit_αsg_initial = calculateTimeBeforeInit(αsg_initial, ms);
        // System.err.println("ms "+ms);

        P = new double[sizeData]; // sizeData = number of years of simulation + 1 (for the initial state)
        Arrays.fill(P, 0.0);
        P[0] = sizePop;
        R = new double[sizeData];
        Arrays.fill(R, 0.0);
        //ABP = new double[sizeData];
        //Arrays.fill(ABP, 0.0);
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
        // Fv_bis = new double[sizeData];
        // Arrays.fill(Fv_bis, 0.0);

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
        myIndividuals = new Individual[sizePop] ;
        for (int i = 0; i < sizePop; i++) {
            myIndividuals[i] = new Individual(sizeData, b_pf, b_pg, this);
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
        //System.err.print("year "+year+" "+" Kst "+Kst+" KA "+KA+" nb hab desservi ") ;
        double nbHabDesservi = Math.min(P[year], (double) Kst[year] / (39.0 / 1000.0)); // 39 kg [converti en tonnes] correspond à quantité article 5 arrêté du 7 juillet 2021 (base calcul pour qté détournée par habitant desservi par la collecte de dechets alimentaires)  
        //System.err.print(nbHabDesservi) ;
        propPopDesserviCollDA[year] = nbHabDesservi / P[year];
        if (nbHabDesservi > 0) {
            nbKgCollectHabDesservi[year] = (Bsf[year] * 1000.0) / nbHabDesservi;
        }
        nbKgOMRHab[year] = (Br[year] * 1000.0) / P[year];
        tauxReductionDechetVert[year] = (Bv[year] - Bv[0]) / Bv[0]; // this evolution perecentage of green waste in dechetre, negative value means reduction

    }

    public void printTrajectory(int year) {
        System.out.print(ident + ";");
        System.out.print(P[year] + ";");
        System.out.print(Bpf[year] + ";");
        System.out.print(Bpg[year] + ";");
        System.out.print(αcf[year] + ";");
        System.out.print(αcg[year] + ";");
        System.out.print(Bcf[year] + ";");
        System.out.print(Ucf[year] + ";");
        //System.err.println("je viens d'écrire SLA") ;
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
/*
    public void computeCollectionTerritoryIndicator(int time) {
        for (int i = 0; i < sizePop; i++) {
            Bpf[time] = Bpf[time] + myIndividuals[i].Bpf;
            Bpg[time] = Bpg[time] + myIndividuals[i].Bpg;
            Bcg[time] = Bcg[time] + myIndividuals[i].Bcg_composted;
            Bcf[time] = Bcf[time] + myIndividuals[i].Bcf_composted;
            Bsf[time] = Bsf[time] + myIndividuals[i].Bsf_sorted;
            Bsg[time] = Bsg[time] + myIndividuals[i].Bsg_sorted;
            Bv[time] = Bpg[time] - Bcg[time] - Bsg[time];
            Br[time] = Bpf[time] - Bcf[time] - Bsf[time];
            B[time] = Bpf[time] + Bpg[time];

        }
    }
*/
    
    
    public void computeCollectionTerritoryIndicator(int time) {
    // Reset aggregated values to zero before summing

    
    Bpf[time] = 0;
    Bpg[time] = 0;
   
    αcf[time] = 0;
    αcg[time] = 0;
    
    Bcf[time] = 0;
    Bcg[time] = 0;
    
    Kct [time] = 0;
    
    Ucf[time] = 0;
    Ucg[time] = 0;
    
    αsf[time] = 0;
    αsg[time] = 0;
    
    Bsf[time] = 0;
    Bsg[time] = 0;
    
    Kst [time] = 0;
      
    Usf[time] = 0;
    Usg[time] = 0;
    
    Bv[time] = 0;
    Br[time] = 0;
    
    // Other arrays reset here...
    
    for (int i = 0; i < sizePop; i++) {
        // Sum up waste production values
        Bpf[time] += myIndividuals[i].Bpf;
        Bpg[time] += myIndividuals[i].Bpg;
        
        // Sum up behavioral intention values
        αcf[time] += myIndividuals[i].αcf;
        αcg[time] += myIndividuals[i].αcg;
        αsf[time] += myIndividuals[i].αsf;
        αsg[time] += myIndividuals[i].αsg;
        
        // Sum up composting values
        Bcg[time] += myIndividuals[i].Bcg_composted;
        Bcf[time] += myIndividuals[i].Bcf_composted;
        
        // Sum up sorting values
        Bsf[time] += myIndividuals[i].Bsf_sorted;
        Bsg[time] += myIndividuals[i].Bsg_sorted;
        
        // Also collect other data points you need
        Ucf[time] += myIndividuals[i].Ucf;
        Ucg[time] += myIndividuals[i].Ucg;
        Usf[time] += myIndividuals[i].Usf;  // <-- Added missing Usf
        Usg[time] += myIndividuals[i].Usg;  // <-- Added missing Us
        
        
        Kst[time] += myIndividuals[i].Kst;  // <-- Added missing Us
        Kct[time] += myIndividuals[i].Kct;
        
        
    }
    
    // Calculate averages for the behavioral intentions
    if (sizePop > 0) {
        αcf[time] /= sizePop;
        αcg[time] /= sizePop;
        αsf[time] /= sizePop;
        αsg[time] /= sizePop;
    }
    
    // Calculate derived values
    Bv[time] = Bpg[time] - Bcg[time] - Bsg[time];
    Br[time] = Bpf[time] - Bcf[time] - Bsf[time];
    B[time] = Bpf[time] + Bpg[time];
    
    
    

    
}
    
    
}
