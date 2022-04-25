# Spendor-RAFT
## COMP90020 Group Project
### Project Structure                 
    ├── docs                              # Documentation
    ├── src                               # Source code 
        └── main
            └── java
                └── com
                    └── da
                        ├── common
                        ├── entity
                        ├── log           # Log replication
                        ├── node          # Leader election 
                        ├── proto
                        ├── raft          # core RAFT algorithm
                        ├── rpc           # RPC communication
                        └── App.java      
        └── test                          
    ├── target                     
    ├── pom.xml                  
    ├── LICENSE
    └── README.md

### Libraries
- **gRPC https://grpc.io/docs/languages/java/quickstart/** for RPC communication
- **RocksDB http://rocksdb.org/** for persistent key-value storage

#### 

### TODOs
- RPC
- Leader election
- Log replication
- KV store client (command line interface)


## Docs
node/Node.java定义了每个节点的行为接口。

raft/Consensus.java定义了共识算法的核心逻辑。

相对应的实现为**RaftNode**和**RaftConsensus**，这两个类为Raft算法最核心的实现。

### Leader Election


