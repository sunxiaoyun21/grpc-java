import io.grpc.examples.nameserver.Ip;
import io.grpc.examples.nameserver.Name;
import io.grpc.examples.nameserver.NameServiceGrpc;
import io.grpc.examples.nameserver.Result;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class NameServiceImplBaseImpl implements NameServiceGrpc.NameService {

    private Map<String,String> map =new HashMap<String, String>();
    private Logger logger=Logger.getLogger(NameServiceImplBaseImpl.class.getName());
    public  NameServiceImplBaseImpl(){
        map.put("ali","125.216.256.350");
        map.put("alsion","158.256.369");
    }
    @Override
    public void getIpByName(Name request, StreamObserver<Ip> responseObserver) {
        Ip ip = Ip.newBuilder().setIp(getName(request.getName())).build();
        //向客户端返回结果
        responseObserver.onNext(ip);
        //告诉客户端，调用以完成
        responseObserver.onCompleted();
    }

    @Override
    public void simpleHello(Name request, StreamObserver<Result> responseObserver) {
        Result result = Result.newBuilder().setId(1)
                .setEmail("test@izhaohu.com")
                .setName(request.getName())
                .build();

        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }


    private String getName(String name) {
        String ip = map.get(name);
        if(ip==null){
            return "0.00.000";
        }
        return ip;
    }
}
