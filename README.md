# Spendor-RAFT
## COMP90020 Group Project
### Project Structure                 
    ├── docs                              # Documentation
    ├── src                               # Source code 
        └── main
            └── java
                └── com
                    └── da
                        ├── log           # Log replication
                        ├── node          # Leader election 
                        ├── rpc           # RPC communication
                        └── App.java      # App entry
        └── test                          # Unit tests
    ├── target                     
    ├── pom.xml                  
    ├── LICENSE
    └── README.md

### TODOs
- Leader election
- Log replication
- KV store client (command line interface)