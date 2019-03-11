import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.grpc.api.auth.AuthServiceGrpc;
import com.grpc.api.auth.AuthServiceProto;
import io.grpc.stub.StreamObserver;

import java.io.UnsupportedEncodingException;

public class AuthServiceImpl implements AuthServiceGrpc.AuthService {
    @Override
    public void authenticate(AuthServiceProto.Credit credit, StreamObserver<AuthServiceProto.Result> responseObserver) {
        System.out.println(credit.getUsername());
        System.out.println(credit.getPassword());
        String userName=credit.getUsername();
        String password = credit.getPassword();

        if("grpc".equalsIgnoreCase(userName) && "123456".equals(password)){
            AuthServiceProto.Result result=AuthServiceProto.Result.newBuilder().setSuccess(true).setMsg("ok").setToken(generateToken(userName)).build();
            responseObserver.onNext(result);

        }else {
            AuthServiceProto.Result result = AuthServiceProto.Result.newBuilder().setSuccess(false).setMsg("not authenticated").build();
            responseObserver.onNext(result);
        }
        responseObserver.onCompleted();
    }

    private String generateToken(String username) {
        Algorithm algorithm;

        try {
            algorithm = Algorithm.HMAC256("SECRETs");
        } catch (UnsupportedEncodingException e) {
            throw  new RuntimeException(e);
        }

        return JWT.create()
                .withIssuer("gsafety")
                .withSubject("grpc")
                .withClaim("name", username)
                .sign(algorithm);
    }
}
