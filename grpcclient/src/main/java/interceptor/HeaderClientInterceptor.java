package interceptor;

import com.grpc.api.auth.AuthServiceGrpc;
import com.grpc.api.auth.AuthServiceProto;
import io.grpc.*;

import java.util.Map;
import java.util.logging.Logger;

public class HeaderClientInterceptor implements ClientInterceptor {

    Logger logger=Logger.getLogger(HeaderClientInterceptor.class.getName());
     public static final String INTERCEPTOR_NAME="HeaderClientInterceptor";
     public static final String USER_ID_KEY="user_id";
     public static final String TOKEN_KEY="token";
     public String token;
     private Map<String,String> headerMap;

     public HeaderClientInterceptor(Map<String,String> headerMap){
         this.headerMap=headerMap;
     }


    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, final Channel channel) {
        //创建client
        final ClientCall<ReqT, RespT> clientCall =channel.newCall(methodDescriptor,callOptions);
        return new ForwardingClientCall<ReqT, RespT>() {
            @Override
            protected ClientCall<ReqT, RespT> delegate() {
                return clientCall;
            }

            @Override
            public void start(Listener<RespT> respTListener,Metadata headers){
                //拦截器，此处可以对header参数进行填充
                for (String key:headerMap.keySet()){
                    Metadata.Key<String> tokenHeader = Metadata.Key.of(key,Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(tokenHeader,headerMap.get(key));
                }

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(respTListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                      Metadata.Key<String> tokenHeader =Metadata.Key.of(USER_ID_KEY,Metadata.ASCII_STRING_MARSHALLER);
                      String userId=headers.get(tokenHeader);
                      logger.info("收到userId:" +userId);
                    }
                },headers);

                AuthServiceGrpc.AuthServiceBlockingStub authService=AuthServiceGrpc.newBlockingStub(channel);
                AuthServiceProto.Result result=authService.authenticate(AuthServiceProto.Credit.newBuilder().setUsername("java").setPassword("123456").build());
                if(result.getSuccess()){
                    token=result.getToken();
                    logger.info("token" +token);
                }
            }

        };

    }
}
