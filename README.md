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
                        ├── rpc           # RPC communication
                        └── App.java      # App entry
        └── test                          # Unit tests
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