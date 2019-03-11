
import com.grpc.api.auth.AuthServiceGrpc;
import interceptor.ServerInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.examples.nameserver.Name;
import io.grpc.examples.nameserver.NameServiceGrpc;

import java.io.IOException;
import java.util.logging.Logger;

public class NameServer {
    private Logger logger=Logger.getLogger(NameServer.class.getName());
    private  static final int DEFAULF_PORT=8088;
    private int port;//服务端口号
    private Server server;

    public NameServer(int port) {
        this(port, ServerBuilder.forPort(port));
    }

    public NameServer(int port, ServerBuilder<?> serverBuilder){
      this.port=port;
      //将拦截器注册到server端
      // 拦截 ServerInterceptor
      server= serverBuilder.addService(ServerInterceptors.intercept(NameServiceGrpc.bindService(new  NameServiceImplBaseImpl()),new ServerInterceptor()))
              .addService(ServerInterceptors.intercept(AuthServiceGrpc.bindService(new AuthServiceImpl())))
              .build();
      //server = serverBuilder.addService(NameServiceGrpc.bindService(new  NameServiceImplBaseImpl())).build();
    }

    private void start() throws IOException{
        server.start();
        logger.info("Server has start lsitening on "+port);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public  void run(){
                NameServer.this.stop();
            }
        });
    }

    private  void stop(){
        if(server !=null){
            server.shutdown();
        }
    }

    private void  blockUntilShutdown() throws InterruptedException {
        if (server!=null){
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NameServer nameServer;
        if (args.length>0){
            nameServer=new NameServer(Integer.parseInt(args[0]));
        }else {
            nameServer=new NameServer(DEFAULF_PORT);
        }

        //启动服务器并接收客户端请求
        nameServer.start();
        //让server阻塞到程序退出为止
        nameServer.blockUntilShutdown();
    }
}
