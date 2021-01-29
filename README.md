# Code-Compression-for-Embedded-Systems

This project implement both code compression and decompression for Embedded Systems.

Assume that the dictionary can have eight entries (index 3 bits) and the eight entries are selected based on frequency (the most frequent instruction should have index 000). 

You are allowed to use only the following seven possible formats for compression. 

1. Format of the Run Length Encoding (RLE):    

000   Run Length Encoding (2 bits)

2. Format of bitmask-based compression – starting location is counted from left/MSB

001 Starting Location (5 bits) Bitmask (4 bits) Dictionary Index (3 bits)

3. Format of the 1 bit Mismatch – mismatch location is counted from left/MSB

010 Mismatch Location (5 bits) Dictionary Index (3 bits)

4. Format of the 2 bit consecutive mismatches – starting location is counted from left/MSB

011 Starting Location (5 bits) Dictionary Index (3 bits)

5. Format of the 2 bit mismatches anywhere – Mismatch locations (ML) are counted from left/MSB

100 1st ML from left (5 bits) 2nd ML from left (5 bits) Dictionary Index (3 bits)

6. Format of the Direct Matching

101 Dictionary Index (3 bits)

7. Format of the Original Binaries

110 Original Binary (32 bits)

Command Line and Input/Output Formats: The simulator should be executed with the following command line. Please use parameters “1” and “2” to indicate compression, and decompression, respectively.

> ./SIM 1 (or java SIM 1) for compression

> ./SIM 2 (or java SIM 2) for decompression
