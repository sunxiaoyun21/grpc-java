import com.grpc.api.auth.AuthServiceGrpc;
import interceptor.HeaderClientInterceptor;
import io.grpc.*;
import io.grpc.examples.nameserver.Ip;
import io.grpc.examples.nameserver.Name;
import io.grpc.examples.nameserver.NameServiceGrpc;
import io.grpc.stub.MetadataUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class NameClient {
    private  static final String DEFAULT_HOST="localhost";
    private static final int DEFAULT_PORT=8088;
    private ManagedChannel managedChannel;//用于通信
    private NameServiceGrpc.NameServiceBlockingStub nameServiceBlockingStub;//客户端需要远程调用服务，在得到stub后，只需要调用stub的相应服务即可

    public  NameClient(String host,int port){
        //channel要设置成明文传输，即usePlainText设置为true
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build());
    }

    public NameClient(ManagedChannel managedChannel) {
        this.managedChannel=managedChannel;
        HashMap<String,String> headerMap=new HashMap<String, String>();
        headerMap.put(HeaderClientInterceptor.TOKEN_KEY,"123456");
        //生成含拦截器的channel
        Channel channel= ClientInterceptors.intercept(managedChannel,new HeaderClientInterceptor(headerMap));
        //this.nameServiceBlockingStub=NameServiceGrpc.newBlockingStub(managedChannel);

        Metadata.Key<String> ATTCHED_HEADER = Metadata.Key.of("attached_header",Metadata.ASCII_STRING_MARSHALLER);
        Metadata metadata=new Metadata();
        metadata.put(ATTCHED_HEADER,"attched");
        this.nameServiceBlockingStub= MetadataUtils.attachHeaders(NameServiceGrpc.newBlockingStub(channel),metadata);



    }
    public  void shutdown() throws InterruptedException {
        managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    public  String getIpByName(String n){
        Name name=Name.newBuilder().setName(n).build();
        Ip ip=nameServiceBlockingStub.getIpByName(name);
        return ip.getIp();
    }

    public AuthServiceGrpc.AuthServiceBlockingStub authService(){
        return AuthServiceGrpc.newBlockingStub(managedChannel);
    }
    public static void main(String[] args) {
        NameClient nameClient=new NameClient(DEFAULT_HOST,DEFAULT_PORT);

         //System.out.println("测试一下");
        for (String arg:args){
            String res=nameClient.getIpByName(arg);
            System.out.println("get result from server: " + res + " as param is " + arg);
        }
    }
}
