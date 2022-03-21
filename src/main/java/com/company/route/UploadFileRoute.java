package com.company.route;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.Host;
import akka.http.javadsl.server.RequestContext;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.company.common.HttpResponse;
import com.company.registry.UploadFileRegistry;
import akka.http.javadsl.model.headers.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    final HttpOrigin validOriginHeader2 = HttpOrigin.parse("https://admin.insee.udev.com.vn");
    HttpOriginRange httpOriginRange2 = HttpOriginRange.create(validOriginHeader2);
    AccessControlAllowOrigin accessControlAllowOrigin2 = AccessControlAllowOrigin.create(httpOriginRange2);

    final HttpOrigin validOriginHeader3 = HttpOrigin.parse("https://admin-nhathau.insee.udev.com.vn");
    HttpOriginRange httpOriginRange3 = HttpOriginRange.create(validOriginHeader3);
    AccessControlAllowOrigin accessControlAllowOrigin3 = AccessControlAllowOrigin.create(httpOriginRange3);


    final HttpOrigin validOriginHeader4 = HttpOrigin.parse("https://dev-admin-nhathau.insee.udev.com.vn");
    HttpOriginRange httpOriginRange4 = HttpOriginRange.create(validOriginHeader4);
    AccessControlAllowOrigin accessControlAllowOrigin4 = AccessControlAllowOrigin.create(httpOriginRange4);


    AccessControlAllowMethods accessControlAllowMethods = AccessControlAllowMethods.create(HttpMethods.GET, HttpMethods.POST);

    public Route route() {
        return extractRequestContext(context -> pathPrefix("image", () -> {
                return  entity(Unmarshaller.entityToMultipartFormData(), formData -> {
            return onSuccess(ask(formData, context), response -> {
                HttpRequest request = context.getRequest();
                List<HttpHeader> cors = new ArrayList<>();
                Optional<HttpHeader> referer = request.getHeader("referer");
                if (referer.isPresent()) {
                    String value = referer.get().value();
                    if (value.startsWith("https://admin-nhathau.insee.udev.com.vn")) {
                        cors.add(accessControlAllowOrigin3);
                    }
                    if (value.startsWith("https://admin.insee.udev.com.vn")) {
                        cors.add(accessControlAllowOrigin2);
                    }

                    if (value.startsWith("https://dev-admin-nhathau.insee.udev.com.vn")) {
                        cors.add(accessControlAllowOrigin4);
                    }

                    if (value.contains("localhost")) {
                        cors.add(accessControlAllowOrigin);
                    }
                    cors.add(accessControlAllowMethods);
                }
                HttpResponse httpResponse = new HttpResponse();
                httpResponse.setData(response.link);
                httpResponse.setError(response.status.intValue());
                return complete(StatusCodes.OK, cors, httpResponse, Jackson.<HttpResponse>marshaller());
            });
        });}));
    }


    private CompletionStage<UploadFileRegistry.UploadResponse> ask(Multipart.FormData formData, RequestContext requestContext) {
        return AskPattern.ask(this.actorRef, (ActorRef<UploadFileRegistry.UploadResponse> replyTo)
                        -> new UploadFileRegistry.UploadRequest(requestContext, formData, replyTo)
                , Duration.ofSeconds(5), this.scheduler);
    }
}
