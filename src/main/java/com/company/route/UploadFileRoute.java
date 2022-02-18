package com.company.route;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.Multipart;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.RequestContext;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.company.common.HttpResponse;
import com.company.registry.UploadFileRegistry;
import akka.http.javadsl.model.headers.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;

public class UploadFileRoute {
    private final ActorRef<UploadFileRegistry.Request> actorRef;
    private final Scheduler scheduler;
    private final ActorContext context;

    public UploadFileRoute(ActorContext<?> context) {
        this.context = context;
        this.scheduler = context.getSystem().scheduler();
        this.actorRef = context.spawn(UploadFileRegistry.create(), "registry");
    }

    final HttpOrigin validOriginHeader = HttpOrigin.parse("http://localhost:3000");
    HttpOriginRange httpOriginRange = HttpOriginRange.create(validOriginHeader);
    AccessControlAllowOrigin accessControlAllowOrigin = AccessControlAllowOrigin.create(httpOriginRange);
    AccessControlAllowMethods accessControlAllowMethods = AccessControlAllowMethods.create(HttpMethods.GET, HttpMethods.POST);

    public Route route() {
        return extractRequestContext(context -> pathPrefix("image", () -> {
                return  entity(Unmarshaller.entityToMultipartFormData(), formData -> {
            return onSuccess(ask(formData, context), response -> {
                HttpResponse httpResponse = new HttpResponse();
                httpResponse.setData(response.link);
                httpResponse.setError(response.status.intValue());
                return complete(StatusCodes.OK, Arrays.asList(accessControlAllowOrigin, accessControlAllowMethods), httpResponse, Jackson.<HttpResponse>marshaller());
            });
        });}));
    }


    private CompletionStage<UploadFileRegistry.UploadResponse> ask(Multipart.FormData formData, RequestContext requestContext) {
        return AskPattern.ask(this.actorRef, (ActorRef<UploadFileRegistry.UploadResponse> replyTo)
                        -> new UploadFileRegistry.UploadRequest(requestContext, formData, replyTo)
                , Duration.ofSeconds(5), this.scheduler);
    }
}
