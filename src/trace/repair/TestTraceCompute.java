package trace.repair;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class TestTraceCompute {
    /** hard coded code params */
    int numDataBlocks = 6;
    int numParityBlocks = 3;
    int totalBlocks = 9;

    int t = totalBlocks-1; //same as l

    /** to access entries of the Helper table */
    HelperTable helperTable = new HelperTable();

    /** to access entries of the Recovery table */
    RecoveryTable recoveryTable = new RecoveryTable();

    /** to access entries of the Dual basis table */
    DualBasisTable dualBasisTable = new DualBasisTable();

    /** to store the trace bandwidths*/
    int[] traceBandwidth = new int[totalBlocks-1];

    /** to store elements from H table */
    List<List> traceElements =new ArrayList<>();

    /** to store elements from R table */
    List<List> repairElements =new ArrayList<>();


    /** to store traces from the helper nodes */
    HashMap<Integer, ArrayList<Boolean>> helperTraces = new HashMap<>();

    /** to store 't' column traces from the helper nodes */
    HashMap<Integer, ArrayList<Boolean>> columnTraces = new HashMap<>();

    /** to store 't' target traces used for recovery */
    boolean[] targetTraces = new boolean[t];

    /**
     * Function to return the bit at some position from a byte
     * @param b the input byte
     * @param position the position to get the bit from
     * @return the boolean bit at the specified position
     */
    boolean getBit(byte b, int position){

        return (1 == ((b >> position) & 1));
    }

    /**
     * Function to return all set bit positions of a number
     * @param inputNumber an integer input number
     * @return a list storing all the positions of the set bits
     */
    public List<Integer> getBitPositions(int inputNumber) {
        List<Integer> positions = new ArrayList<>();
        int index = 0; // start at bit index 0

        while (inputNumber != 0) { // If the number is 0, no bits are set

            // check if the bit at the current index 0 is set
            if ((inputNumber & 1) == 1)
                // it is, save this bit index to a List.
                positions.add(index);
            // advance to the next bit position to check
            inputNumber = inputNumber >> 1; // shift all bits one position to the right
            index = index + 1;              // we have to now look at the next index.
        }
        return positions;
    }

    /**
     * Function to calculate the log base 2 of a non-negative integer
     * @param N an integer input number
     * @return the log base 2 of the input number
     */
    public int log2(int N)
    {

        // calculate log2 N indirectly using log() method
        int result = (int)(Math.log(N) / Math.log(2));

        return result;
    }

    /*//Function to reverse the bits of a byte
    public byte reverseBitsOfByte(byte x) {
        int intSize = 8;
        byte y=0;
        for(int position=intSize-1; position>0; position--){
            y+=((x&1)<<position);
            x >>= 1;
        }
        return y;
    }*/

    /**
     * Function to return t-bit binary representation of the non-negative integer m
     * @param t an integer specifying the bits we want to represent the number with
     * @param m an integer input number
     * @return the t-bit binary representation of the input number
     */
    public boolean[] binaryRep(int t, int m){
        /*if((m < 0) || (m > Math.pow(q, t-1)))
            //System.out.println("Number not in range [0..(q^t-1)]"); */
        boolean[] bin = new boolean[t];
        Arrays.fill(bin, Boolean.FALSE);

      /*  for(int i = 0; i < bin.length; i++) {
            //System.out.println(bin[i]);
        } */

        while (m > 1) {
            int log = log2(m);
            int pos = (t - log)-1;
            if(pos < 0)
                pos = 0;
            bin[pos] = true;
            m = (int) (m - Math.pow(2, log));
        }
        if (m == 1)
            bin[t-1] = true;

       /* //System.out.println("After binaryRep, the binary array is");
        for(int i = 0; i < bin.length; i++) {
            //System.out.println(bin[i]);
        } */
        return bin;

    }

    /**
     * Function to retrieve the trace bandwidths into an 1D array
     * @param erasedIndex indexes of erased symbol in the code word
     */
    public void computeBw(int erasedIndex) throws IOException {

        int i = erasedIndex;
        int k = 0;
        //System.out.println("\n Computing bandwidth first..");
       // System.in.read();
        for (int j = 0; j < totalBlocks; j++) {
            if ((i != j)) {
                Object element = helperTable.getElement(j, i);
                String s = element.toString();
                String[] elements = s.split(",");
                ////System.out.println(Arrays.toString(elements));
                ////System.out.println(elements[0]);
                traceBandwidth[k] = Integer.parseInt(elements[0]);
                k++;
            }
        }
    }

    /**
     * Function to compute helper traces of the bytes to be sent from helper node
     * @param codeWord the byte array with a codeword
     * @param erasedIndex indexes of erased symbol in the code word
     */
    public void computeTraces(byte[] codeWord, int erasedIndex) throws IOException {

        int i = erasedIndex;
        int k = 0;

        for (int j = 0; j < totalBlocks; j++) {
            if ((i != j)) {
                Object element = helperTable.getElement(j,i);
                String s = element.toString();
                String[] elements = s.split(",");

                List columnValues = new ArrayList();
                ArrayList<Boolean> ar = new ArrayList<>();
                for (int l = 1; l <= traceBandwidth[k]; l++) {
                    columnValues.add(elements[l]);

                    String helperString = elements[l].toString();
                    //System.out.println("\n Helper table element is: "+helperString);

                    Integer helperInt = Integer.parseInt(helperString.trim());

                   /* Byte helperByte = helperInt.byteValue();
                    Byte reversedHelperByte = reverseBitsOfByte(helperByte);
                    Integer reversedHelperInt = reversedHelperByte.intValue(); */

                    //Find the set bit positions of the helper table element
                    List<Integer> positions = getBitPositions(helperInt);

                    //System.out.println("\n Non zero bits in the helper element are: \n");
                    boolean traceBitsXor = false;

                    for (Integer position : positions) {
                        //System.out.print("\t" + position);
                        //XOR all bits of the codeword at the set positions of H table element
                        traceBitsXor=traceBitsXor ^ getBit(codeWord[j], position); //check with Hoang
                    }

                    ar.add(traceBitsXor);
                    helperTraces.put(k, ar);
                }

                traceElements.add(columnValues);
                k++;
            }
        }



    }

    /**
     * Function to call the routines to compute bandwidth and helper traces
     * @param codeWord the byte array with a codeword
     * @param erasedIndex indexes of erased symbol in the code word
     */
    public void helperTrace(byte[] codeWord, int erasedIndex) throws IOException{

        computeBw(erasedIndex);
        int sumBandwidth = 0;

        //System.out.println("\n Bandwidths are:");
        for (int j : traceBandwidth) {
            //System.out.print("\t" + j);
            sumBandwidth=sumBandwidth + j;
        }
        //System.out.println("\n Total number of traces sent from helper nodes: "+sumBandwidth);
        //System.in.read();
        //System.out.println("\n Computing traces...");
        //System.in.read();

        computeTraces(codeWord, erasedIndex);

       /*
        //System.out.print("\n Helper Table Elements: \n");

        for (List traceElement : traceElements) {
            //System.out.print("\n");
            //System.out.print("\t" + traceElement);
        }
        //System.in.read();

        //System.out.print("\n Helper Traces from all helpers: \n");
        for(int i = 0; i < helperTraces.size(); i++) {
            //System.out.print("\n");
            //System.out.print("\t"+helperTraces.get(i));

        }
        //System.in.read(); */


    }


    /**
     * Perform trace repair from the traces computed and recover the lost code symbol byte
     * @param erasedIndex indexes of erased unit in the inputs array
     * @return the recovered code symbol byte
     */
    public byte traceRepair(int erasedIndex) throws IOException {

        //retrieve element 'm' from recovery table and create its binary representation with traceBandwidth[i] bits
        int i = erasedIndex;
        int k = 0;

        for (int j = 0; j < totalBlocks; j++) {
            if ((i != j)) {
                Object element = recoveryTable.getElement(j,i);
                String st = element.toString();
                String[] elements = st.split(",");

                List repairColumnValues = new ArrayList();
                ArrayList<Boolean> ar = new ArrayList<>();

                int traceBandwidth = Integer.parseInt(elements[0]);

                for (int s = 1; s <=t; s++) {
                    repairColumnValues.add(elements[s]);
                    String repairString = elements[s].toString();
                    //System.out.println("\n Repair element "+s+" is: " + repairString);
                    Integer repairInt = Integer.parseInt(repairString.trim());

                    //System.out.println("\n Trace bandwidth is: "+traceBandwidth);
                    boolean bin[] = binaryRep(traceBandwidth, repairInt);

                    //Store the binary rep as a Vector
                    Vector<Boolean> binVec = new Vector<>(bin.length);
                    //System.out.println("Binary rep of the repair element in bw bits is: ");

                    for (boolean value : bin) {
                        //System.out.println(value);
                        binVec.add(value);
                    }

                    //Get helper trace elements computed earlier into a Vector
                    ArrayList<Boolean> helperTraceArray = helperTraces.get(k);
                    Vector<Boolean> helperTraceVector = new Vector<>(helperTraceArray);

                    //Boolean array to store the bit-wise & of binRep and helperTrace
                    boolean[] res = new boolean[traceBandwidth];

                    //System.out.println("Computing column traces ... ");
                    for (int l = 0; l < traceBandwidth; l++) {
                        boolean a = Boolean.TRUE.equals(binVec.get(l));
                        boolean b = Boolean.TRUE.equals(helperTraceVector.get(l));
                        res[l] = a & b; //check with Hoang

                    }
                    //System.in.read();

                    //boolean to compute the XOR of all bits of res
                    boolean output = false;
                    for (boolean re : res) {
                        output^=re; //check with Hoang
                    }
                    //ArrayList to store the output bit
                    ar.add(output);
                    //Store this as the column trace of node k
                    columnTraces.put(k, ar);


                }
                repairElements.add(repairColumnValues);
                k++;
            }
        }

        /*

        //System.out.print("\n Repair Table Elements: \n");

        for (List repairElement : repairElements) {
            //System.out.print("\n");
            //System.out.print("\t" + repairElement);
        }
        //System.in.read();

        //System.out.print("\n Column Traces from all helpers: \n");
        for(int m = 0; m < columnTraces.size(); m++) {
            //System.out.print("\n");
            //System.out.print("\t"+ columnTraces.get(m));

        }
        //System.in.read();

        */

        //Construct the t target traces of c[j]
        //System.out.println("\n Construct the t target traces of c[j]..");
        //System.in.read();

        for(int s=0; s<t; s++){
            boolean RHS = false;
            for(int j=0; j<totalBlocks-1; j++) {

               // if ((i != j)) {
                    //RHS = false;
                    boolean colTraceBool=columnTraces.get(j).get(s);
                    //System.out.print("\n");
                    //System.out.print("\t Col Trace at " + j + "," + s + "is: " + colTraceBool);
                    //byte colTraceByte = (byte)(colTraceBool?1:0); //Covert boolean to byte
                    RHS ^= colTraceBool; //check with Hoang
                //}
                //System.out.print("\n");
                //System.out.print("\t Col XOR is:"+RHS);

            }
            ////System.out.println("");
            targetTraces[s] = RHS;
        }

        // Reconstruct c[j] and verify
        //System.out.println("\n Reconstruct c[j] to verify..");
        byte recoveredValue = (byte)0;

        Object element = dualBasisTable.getElement(i);
        String st = element.toString();
        ////System.out.println("Dual basis elements are: "+st);
        String[] elements = st.split(",");

        Integer[] dualBasisInt = new Integer[t];
        byte[] dualBasisByte = new byte[t];


        for(int m=0;m<elements.length; m++){
            String dualBasisString = elements[m].toString();
            dualBasisInt[m] = Integer.parseInt(dualBasisString.trim());
            dualBasisByte[m] = dualBasisInt[m].byteValue();
        }
        for(int s=0; s<t; s++){

            byte dualBByte = dualBasisByte[s]; //take the sth byte from dual basis array

            //  byte targetTraceByte = targetTraces[s]; //take the sth byte from targetTraces

            if(targetTraces[s]) {
                recoveredValue ^= dualBByte;
            }

        }
        return recoveredValue;

    }


    /**
     * Main function to iterate through all codewords stored in a file
     * Simulate all single node failure scenarios of each codeword
     * Perform trace repair to and recover the lost code symbol byte
    */
    public static void main(String[] args) throws IOException {

        TestTraceCompute tr = new TestTraceCompute();

        File myObj = new File("CodeWords.txt");
        Scanner myReader = new Scanner(myObj);
        int num=1;
        //start a timer
        long start = System.currentTimeMillis();

        while (myReader.hasNextLine()) {

            //Get a codeword from the codeword database
            String data = myReader.nextLine();

            //Perform string actions to get the format we want
            data = data.replace("[", "");
            data = data.replace("]", "");
            String[] elements = data.split(",");
            //System.out.println("Size of elements is: "+elements.length);

            Integer[] codeWordInt=new Integer[tr.totalBlocks];
            byte[] codeWordByte=new byte[tr.totalBlocks];

            //Copy the string elements into a byte array
            for(int i=0; i<9; i++){
                String codeSym = elements[i].toString();
                //System.out.println("\n Code Symbol "+ i +" is: " + codeSym);
                codeWordInt[i]=Integer.parseInt(codeSym.trim());
                codeWordByte[i]=codeWordInt[i].byteValue();

            }


            //Iterate through all erased node's indices to simulate all single node failure cases
            for(int symbol=0; symbol<9;symbol++) {
                int erasedIndex=symbol;
                //System.out.println("\n Erased index is: "+erasedIndex);

                //Call function to compute helper traces
                //System.out.println("\n Calling helper trace function... ");
                tr.helperTrace(codeWordByte, erasedIndex);

                //Call function to perform trace repair
                //System.out.println("\n Calling function to perform trace repair... ");
                //System.in.read();
                byte recoveredValue=tr.traceRepair(erasedIndex);

                //System.out.print("\n Target traces computed: \n");
                for (int i=0; i < tr.targetTraces.length; i++) {
                    ////System.out.print("\n");
                    //System.out.print("\t" + tr.targetTraces[i]);

                }

                //System.out.print("\n Recovered value is: ");
                //System.out.print("" + recoveredValue);

                if (recoveredValue == codeWordByte[erasedIndex]) {
                    //System.out.println("CODEWORD : "+num+" is: "+data);
                    ////System.out.println("The codeword used was: " + st +"location :"+elementNum);
                    //System.out.println("\n The erased index was: "+erasedIndex);
                    //System.out.println("\n Recovery success!!");
                }
                else {
                    //System.out.println("\n Recovery failure!!");
                    break;
                }
                //System.in.read();
            }
            num++;


          if (num>=1001)
                break;
        }
        
        //finish the timer
        long finish = System.currentTimeMillis();
        long elapsedInSec =(long) ((finish - start)/1000.0);

        System.out.println("Average recovery time taken for 1001 codewords: "+(elapsedInSec/1001.0));
        System.out.println("Average time for taken for recovering single index failure: "+((elapsedInSec/1001.0)/9.0));

     
    }

}