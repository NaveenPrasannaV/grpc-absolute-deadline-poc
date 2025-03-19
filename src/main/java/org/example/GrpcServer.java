package org.example;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.time.Instant;
import org.example.grpc.GreeterGrpc;
import org.example.grpc.HelloReply;
import org.example.grpc.HelloRequest;

public class GrpcServer {
  public static void main(String[] args) throws IOException, InterruptedException {
    Server server = ServerBuilder.forPort(50051)
        .addService(new GreeterServiceImpl()) // Added Service Implementation
        .intercept(new DeadlineInterceptor()) // Attached Deadline Interceptor
        .build()
        .start();

    System.out.println("gRPC Server started on port 50051");
    server.awaitTermination();
  }

  static class GreeterServiceImpl extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
      Metadata metadata = DeadlineInterceptor.METADATA_CONTEXT_KEY.get(Context.current());
      Metadata.Key<String> deadlineKey = Metadata.Key.of("absolute-deadline", Metadata.ASCII_STRING_MARSHALLER);

      //Get absolute deadline from metadata
      String receivedDeadline = (metadata != null) ? metadata.get(deadlineKey) : null;
      Instant serverTime = Instant.now();

      if (receivedDeadline != null) {
        Instant clientDeadline = Instant.parse(receivedDeadline);
        System.out.println("Received absolute deadline from client: " + receivedDeadline);
        System.out.println("Current server time: " + serverTime);

        if (serverTime.isAfter(clientDeadline)) {
          System.out.println("Request expired. Rejecting...");
          responseObserver.onError(Status.DEADLINE_EXCEEDED.asRuntimeException());
          return;
        }
      } else {
        System.out.println("No deadline received, proceeding normally...");
      }

      //Process and send response
      String message = "Hello, " + request.getName();
      HelloReply reply = HelloReply.newBuilder().setMessage(message).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }
}
