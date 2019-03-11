package interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.grpc.*;


import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

public class ServerInterceptor implements io.grpc.ServerInterceptor {

    private Logger logger = Logger.getLogger(ServerInterceptor.class.getName());
    public static final String USER_ID_KEY = "user_id";
    public static final String TOKEN_KEY = "token";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Metadata.Key<String> token=Metadata.Key.of(TOKEN_KEY,Metadata.ASCII_STRING_MARSHALLER);
        final Metadata.Key<String> userIdKey = Metadata.Key.of(USER_ID_KEY,Metadata.ASCII_STRING_MARSHALLER);

        String tokenStr=metadata.get(token);

        if (!checkoutToken(tokenStr)) {
            serverCall.close(Status.PERMISSION_DENIED, metadata);
            //?
            serverCall.close(Status.DATA_LOSS,metadata);
        }
        //判断请求头是否合法
        if(tokenStr==null || tokenStr.length()==0){
            serverCall.close(Status.DATA_LOSS,metadata);
        }

        final String userId = "1000";

        ServerCall<ReqT, RespT> result = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall){
            @Override
            public void sendHeaders(Metadata headers) {
                //给客户端返回头信息
                headers.put(userIdKey,userId);
                super.sendHeaders(headers);
            }
        };
        return serverCallHandler.startCall(result,metadata);

    }

    public boolean checkoutToken(String token){
        Algorithm algorithm;
        try {
            algorithm=Algorithm.HMAC256("SECRETS");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
        JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer("gsafety").build();
        try {
            jwtVerifier.verify(token);
        }catch (JWTVerificationException e){
            return false;
        }

        return true;
    }
}
