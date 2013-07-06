

Things done to ensure correctness. 
1) Manually traced the code to ensure new code is logically equivalent. 
2) Ran the correctness checker script provided by the TA (Elliot) on the message board to get 100% passing for all cases in 1%, 95% adn 99%. 
3) Ensured there is no race condition, and ran my code multple times to detect any corner cases. 

Things done to improve performace. 
1) Add / Remove conflicts should not be add the root. Hence the root is written only if its content is modified. 
2) Random number generation is outside the atomic block. Its done using compare and set. 
3) Add and Remove checks if the key is contained before executing any further. This improves performance. 

Performance observed at my end. 
1) 95% and 99% are easily better than the CoarseLock Single thread. 
2) 0% read performance is consistently 80% or more of CoarseLock Single thread. However, the numbers are a bit flaky, and need multiple runs to some up with a good average. 



