# CryptoCurrency
This project is based on Satoshi Nakamoto's White Paper and implemented in Java. It was started for learning purpose to get some knowledge on block chain and cryptocurrency environment.
This is a full node. Block, BlockChain, Transaction - inputs and outputs, UTXO, Miner and Wallet were pushed on the first commit.

Difficulty has not been implemented, so, for simplicity proof of work is a hash with remainder 5 zeroes and 
cryptographic (public key, private key, digital signature) verification is implemented with RSA protocol but not with Elliptic Curve Cryptography.

Get Started:

1) UserInterface : 
core.wallet.UserInterface.java is the entry point of the program. It is designed to act as a wallet as well as monitoring unit for blocks, blockchain and transactions. From UserInterface new Accounts aka addresses can be generated and transactions can be made.

2) Account : 
core.common.Account is address of a user. The address is the hash of the combination of a randomly generate sting 'Salt' and public key. Address is discussed more in More on Account/Address section.

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


More on Account/Address:

Along with the regular decryption key in RSA, a random string 'Salt' is generated when an account is created. And a value 'number of addresses' increments its value as new address is generated every time. Now these three parts 'decryptionKey', 'salt' and 'noOfAddresses' makes a proper private key in this project and needs to be kept secretly.
The salt and number of addresses are required to provide different addresses for each transaction. Now, the address is the combination of the salt, number of addresses and hash of the public key. Thus the number of addresses value provides different addresses for each transaction as this value will increment at every transaction.

Algorithm :

1) Hash the public key and say it previous hash (prevHash).
2) Encrypt the salt + number of addresses combination by public key.
3) Hash the combination of result of step 2 and prevHash and set this to prevHash.
4) Now hash again the result of step 3 to generate the address.

The address basically is a SHA-256 hash so, Base64 has been used to encode this hash to make more human readable.

Wallet add two more steps to above mentioned algorithm

5) check if hash or address generated in step 4 is present in transaction output. If present claim it.
6) Repeat steps 2, 3, 4 and 5 until number of addresses (See number of addresses is required to stop the loop. Otherwise, what is the criteria or when does searching for my coin stops?)

So, how does Miner recognize if the sender is authorize to spent the coin or not:

The scriptSig or script signature required is the hash generated in step 3. Sender when spending the coin put this string (scriptSig) in input section of transaction. Because only sender knows the string required to generate the address miner will hash this scriptSig and matches with that of utxo and if verified it is confirmed that the sender is authorize to spend the coin. 

This makes the wallet a Deterministic Wallet since all the addresses can be generated from the seed salt and public key. 
And notice that the old addresses can also be used to receive and claim coins.

Also notice that the only string required is the signature hash generated in step 3. I have used salt and number of address for this project. Anyone implementing a new wallet can implement there own logic to generate the step 3 result. Moreover, different wallet for different accounts can implement different ideas to get the step 3 result. For example I can simply multiply number of addresses with 100 and use this as a salt to public key and never use the mentioned randomly generated string.


More on Center/Server:

As center gets started it also starts its server and miner. Coinbase mining starts immediately as miner starts.
The default server port number is 29236. It is hardcoded. If it is not available center searches for other available ports.

More on Mining: 

1) Proof of Work is a 256 bit hash BigInteger whose remainder is five (5) zeroes ie. hash should be divisible by 10000 (for simplicity).

2) Nonce is a value from 0 to 1999999999 and it is also considered as difficulty.

3) Miner here if finds a block broadcasts it to other miners and waits for its immediate peers'* confirmation. If number of confirmed peers is greater than unconfirmed ones it adds the block into its blockchain. And if not it does not add.


Note: Immediate Peers mean those peers a center is directly connected to.


How to Deploy:

Deployment or testing can be done in localhost.
Steps: 
i) create jar . copy it on various locations.
ii) run the jar/s
iii) and enjoy