package com.da.log;

//p283
public class ReplicatingState {
    private int nextIndex;
    private int matchIndex;
    private boolean replicating = false;
    private long lastReplicatedAt = 0;



    void replicateNow(){
        replicateAt(System.currentTimeMillis());
    }

    //测试用
    void replicateAt(long replicatedAt){
        ReplicatingState replicatingState = ensureReplicatingState();
        replicatingState.setReplicating(true);
        replicatingState.setLastReplicatedAt(replicatedAt);
    }

    void stopReplicating(){
        ensureReplicatingState().setReplicating(false);
    }

    boolean shouldReplicate(long readTimeout){
        ReplicatingState replicatingState = ensureReplicatingState();
        //没在复制或复制超时
        return !replicatingState.isReplicating() ||
                System.currentTimeMillis()-replicatingState.getLastReplicatedAt()>=readTimeout;
    }

    private ReplicatingState ensureReplicatingState() {
        return new ReplicatingState();
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    public boolean isReplicating() {
        return replicating;
    }

    public long getLastReplicatedAt() {
        return lastReplicatedAt;
    }

    public void setReplicating(boolean replicating) {
        this.replicating = replicating;
    }

    public void setLastReplicatedAt(long lastReplicatedAt) {
        this.lastReplicatedAt = lastReplicatedAt;
    }
}
