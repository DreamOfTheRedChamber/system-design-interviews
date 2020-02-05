
# Latency numbers
## CPU
* Mutex lock/unlock 25 ns
* Compress 1K bytes with Zippy 3,000 ns

## Cache
* L1 cache reference 0.5 ns
* L2 cache reference 7 ns
* Main memory reference 100 ns
* Read from distributed cache 
* Read 1 MB sequentially from memory 250,000 ns

## Disk
* Disk seek 10,000,000 ns
* Read 1 MB sequentially from disk 20,000,000 ns

## Network
* Send 2K bytes over 1 Gbps network 20,000 ns
* Round trip within same datacenter 500,000 ns
* Send package CA->Netherlands->CA: 100ms
	* Using New York as an example: https://wondernetwork.com/pings/
		- New York to Washington: 10ms
		- New York to Paris: 75ms
		- New York to Tokyo: 210ms
		- New York to Barcelona: 103ms

