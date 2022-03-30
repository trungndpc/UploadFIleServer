package com.company.route;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.Multipart;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.RequestContext;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.company.common.CORS;
import com.company.common.HttpResponse;
import com.company.registry.UploadFileRegistry;

import java.time.Duration;
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

    public Route route() {
        return extractRequestContext(context -> concat(
                pathPrefix("image", () -> uploadFileRoute(context)),
                pathPrefix("draw-image", () -> post(() -> drawImage(context))))
        );
    }

    private Route drawImage(RequestContext context) {
        return entity(Unmarshaller.entityToString(), body ->
                onSuccess(askDrawRegistry(body), response -> {
                    HttpResponse httpResponse = new HttpResponse();
                    httpResponse.setData(response.link);
                    httpResponse.setError(response.status.intValue());
                    return complete(StatusCodes.OK, CORS.accessControlHeader(context),
                            httpResponse, Jackson.<HttpResponse>marshaller());
                })
        );
    }

    private Route uploadFileRoute(RequestContext context) {
        return entity(Unmarshaller.entityToMultipartFormData(), formData ->
                onSuccess(askUploadRegistry(formData, context), response -> {
                    HttpResponse httpResponse = new HttpResponse();
                    httpResponse.setData(response.link);
                    httpResponse.setError(response.status.intValue());
                    return complete(StatusCodes.OK, CORS.accessControlHeader(context),
                            httpResponse, Jackson.<HttpResponse>marshaller());
                })
        );
    }


    private CompletionStage<UploadFileRegistry.UploadResponse> askUploadRegistry(Multipart.FormData formData, RequestContext requestContext) {
        return AskPattern.ask(this.actorRef, (ActorRef<UploadFileRegistry.UploadResponse> replyTo)
                        -> new UploadFileRegistry.UploadRequest(requestContext, formData, replyTo)
                , Duration.ofSeconds(5), this.scheduler);
    }

    private CompletionStage<UploadFileRegistry.UploadResponse> askDrawRegistry(String body) {
        return null;
//        return AskPattern.ask(this.actorRef, (ActorRef<UploadFileRegistry.UploadResponse> replyTo)
//                        -> new UploadFileRegistry.UploadRequest(requestContext, formData, replyTo)
//                , Duration.ofSeconds(5), this.scheduler);
    }
}
