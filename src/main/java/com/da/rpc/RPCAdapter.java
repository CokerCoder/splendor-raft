package com.da.rpc;

import com.da.entity.*;
import com.da.node.Node;


/**
 * 最终RPC服务转换成的适配器接口，RPC service暂时没有实现
 */
public interface RPCAdapter {

    // 开始监听端口
    void listenOn(int port);

    // destination 类型待定（需要address和port)
    // RPC 需调用 Node的 XXXResult handleXXX(XXXRpc)
    RequestVoteResult requestVoteRPC(RequestVoteRpc request, Node destination);

    AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request, Node destination);

    // 关闭所有连接
    void close();

}
