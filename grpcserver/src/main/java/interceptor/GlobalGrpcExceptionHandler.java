package interceptor;

import io.grpc.*;
import io.grpc.ServerInterceptor;

public class GlobalGrpcExceptionHandler implements ServerInterceptor{
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        ServerCall.Listener<ReqT> call=serverCallHandler.startCall(serverCall,metadata);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(call) {
            @Override
            public void onHalfClose() {

                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    serverCall.close(Status.INTERNAL.withCause(e).withDescription("error message"),new Metadata());

                }
            }
        };

    }
}
