package org.example;

import io.grpc.*;
import java.time.Instant;

public class DeadlineInterceptor implements ServerInterceptor {
  static final Context.Key<Metadata> METADATA_CONTEXT_KEY = Context.key("metadata-key");

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

    // Store metadata inside Context
    Context ctx = Context.current().withValue(METADATA_CONTEXT_KEY, headers);

    System.out.println("Intercepted absolute deadline: " + headers.get(Metadata.Key.of("absolute-deadline", Metadata.ASCII_STRING_MARSHALLER)));

    return Contexts.interceptCall(ctx, call, headers, next);
  }
}
