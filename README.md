# TraceRepair
Implementation of the trace repair recovery algorithm for RS(9,6) erasure code as a standalone java application

This program implements the following:
+ Helper trace computation at each helper node (Uses: Helper Table)
+ Columnn and target trace computation at the recovery worker (Uses: Recovery Table)
+ Recovers the lost byte in a code word (Uses: DualBasis Table)

The program runs through a codeword database containing 1001 codewords and simulates all single failures in each code word. 
