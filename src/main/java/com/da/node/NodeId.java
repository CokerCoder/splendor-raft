package com.da.node;
import java.io.Serializable;
import java.util.Objects;


//用字符串作为服务器成员的标识（映射表的关键字）
public class NodeId implements Serializable {
    private final String value;

    public NodeId(String value) {
        this.value = value;
    }

    public static NodeId of(String value){
        return new NodeId(value);
    }

    public boolean equals(Object o){
        if(this==o)return true;
        if(!(o instanceof NodeId)) return false;
        NodeId id = (NodeId) o;
        return Objects.equals(value, id.value);
    }

    public String getValue(){
        return value;
    }

    public int hashCode(){
        return Objects.hash(value);
    }

    public String toString(){
        return this.value;
    }
}
