package org.example;

import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import org.example.grpc.GreeterGrpc;
import org.example.grpc.HelloReply;
import org.example.grpc.HelloRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class GrpcClient {
  public static void main(String[] args) {
    // Create a gRPC channel
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build();

    // Set absolute deadline: Modify here (5 min ahead OR 10 min back)
    Instant deadlineInstant = Instant.now().plus(5, ChronoUnit.MINUTES);  // Change to `.minus()` for -10 min
    String deadlineString = deadlineInstant.toString();

    // Attach deadline to metadata
    Metadata metadata = new Metadata();
    Metadata.Key<String> deadlineKey = Metadata.Key.of("absolute-deadline", Metadata.ASCII_STRING_MARSHALLER);
    metadata.put(deadlineKey, deadlineString);

    // Attach metadata interceptor
    GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));

    // Send request
    HelloRequest request = HelloRequest.newBuilder().setName("Test User").build();
    try {
      HelloReply response = blockingStub.sayHello(request);
      System.out.println("Response from Server: " + response.getMessage());
    } catch (StatusRuntimeException e) {
      System.out.println("RPC failed: " + e.getStatus());
    } finally {
      channel.shutdown();
    }
  }
}
