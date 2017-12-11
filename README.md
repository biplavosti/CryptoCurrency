# CryptoCurrency
This project is based on Satoshi Nakamoto's White Paper and implemented in Java. It was started for learning purpose to get some knowledge on block chain and cryptocurrency environment.
This is a full node. Block, BlockChain, Transaction - inputs and outputs, UTXO, Miner and Wallet were pushed on the first commit.

Difficulty has not been implemented, so, for simplicity proof of work is a hash with remainder 5 zeroes and 
cryptographic (public key, private key, digital signature) verification is implemented with RSA protocol but not with Elliptic Curve Cryptography.

Get Started:

1) UserInterface : 
core.wallet.UserInterface.java is the entry point of the program. It is designed to act as a wallet as well as monitoring unit for blocks, blockchain and transactions. From UserInterface new Accounts aka addresses can be generated and transactions can be made.

2) Account : 
core.common.Account is address of a user. An address is public key of the account holder formatted in simple way. The format is described below.
In this project Public Key Encryption is implemented with RSA Encryption method. The public key consists of a encryption key and product of two primes let us call them encryptionKey and primeProduct respectively.
Now the address of the account is : number of digits in encryptionKey concatenated with encrytionKey and again concatenated with the primeProduct.
Let me show with an example: 
Let prime numbers be 7 and 13. The prime product will be 7*13 = 91
encryption key is chosen in such way that it is not a common factor of (7-1) = 6 and (13-1) = 12.
so encryptionKey is 5 which is not the factor of 6 or 12.
so the number of digits in encryptionKey is 1.
now the address created with the above formula is 1591. Now if the encryption was 17 for example, the address would have been 21791.

core.common.CryptoService.java is the helper class that does this kind of address generation from public key and its reverse public key from an address.
It also has other functions such as encryption and decryption messages ie. signature.

core.common.PublicKey.java and core.common.PrivateKey.java is responsible for public and private keys respectively.

3) Center: 
core.common.Center.java acts as networking hub for both wallet and miner. The function of this Center is to broadcast transaction generated from its wallet and blocks generated from its miner or relay transactions and blocks that have arrived in this center to other centers.
It receives transactions, broadcast them to other centers, accumulate them for itself and pass them to the miner to get mined in block. When its miner mines a block it broadcast to other miners through the center. All the networking connections is handled through this center. It consists of two inner class Server and Client. Server is a thread that starts when the center is started. It continuously receives messages from other nodes and carry out the respective task by creating new thread called ClientHandler. Next Client class is used to communicate with other center server.
It stores all the information needed to operate the center such its server ip address and port number, list of accounts or address in its wallet accounts of the wallet and save them into a file called 'center.ser'.
It has its peer list in core.common.PeerPool.java and stores the ip address and port number of its peer in 'peerpool.ser' file.
It also verifies transactions and blocks received from other and put them in blockchain if verified.

4) Transaction Pool : 
core.TransactionPool.java is the pool of transaction that are mined into blocks and are a part of blockchain. It is saved in 'transactions.ser' file.
Unspent transactions are separated out as core.common.UTXO.java and stored as list in core.common.UTXOPool.java and later save it to the file called 'utxo.ser'.

5) Transaction, Block, BlockChain, Miner : 
core.Miner.java collects core.common.Transaction.java and mine a core.Block.java. This block, if verified, are broadcasted to other miners and put into core.BlockChain.java.
The Blockchain is saved in file 'blochchain.ser'.


More on Center/Server:
As center get started it also starts its server and miner. Coinbase mining starting immediately as miner starts.
The default server port number is 29236. It is hardcoded. If it is not available center searches for other available ports.

More on Mining: 

1) Proof of Work is a 256 bit hash BigInteger whose remainder is five (5) zeroes ie hash should be divisible by 10000 (for simplicity).

2) Nonce is a value from 0 to 1999999999 and it is also considered as difficulty.

3) Miner here if finds a block broadcasts it to other miners and waits for its immediate peers* confirmation. If number of confirmed peers is greater than unconfirmed ones it adds the block into its blockchain. And if not it does not add.


Note: Immediate Peers mean those peers a center is directly connected to.


How to Deploy:

Deployment or testing can be done in localhost.
Steps: 
i) create jar . copy it on various locations.
ii) run the jar/s
iii) and enjoy