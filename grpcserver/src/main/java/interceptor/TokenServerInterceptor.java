package interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class TokenServerInterceptor implements ServerInterceptor {
    public static final String TOKEN_ENDPOINT="com.grpc.api.auth.AuthService/Authenticate";


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        if(TOKEN_ENDPOINT.equals(fullMethodName)){
            return serverCallHandler.startCall(serverCall,metadata);
        }

        return null;
    }
}
